package com.example.timewarp.feature.lottery.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.data.repository.FirebaseReservationRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.startup.StartupScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Responsible for describing the one main button the user can press on this lottery.
sealed interface LotteryAction {
    data object JoinLottery : LotteryAction
    data object JoinWaitlist : LotteryAction
    data class LeaveWaitlist(val reservationId: String) : LotteryAction
    data class ViewReservation(val reservationId: String) : LotteryAction
    data object None : LotteryAction
}

/**
 * Responsible for holding what one campsite row shows.
 *
 * @property campsiteId ID of the campsite
 * @property name Campsite name
 * @property address Campsite address
 */
data class LotteryCampsite(
    val campsiteId: String,
    val name: String,
    val address: String,
)

// State of the UI
sealed interface LotteryDetailsState {

    data object Loading : LotteryDetailsState

    // Responsible for holding the loaded lottery
    data class Content(
        val title: String,
        val registrationText: String,
        val bookingText: String,
        val winnerCount: Int,
        val waitlistText: String,
        val campsites: List<LotteryCampsite>,
        val action: LotteryAction,
        val isOwner: Boolean,
        val isWorking: Boolean = false,
        val actionError: String? = null,
    ) : LotteryDetailsState

    data class Error(val message: String) : LotteryDetailsState
}


/**
 * Responsible for loading one lottery. Seeing what it can do, joining or leaving
 *
 *@param campsiteRepository Campsite information (names, addresses, owners)
 *@param lotteryRepository Lottery information
 *@param observeCurrentUser Signed-in user
 *@param reservationRepository Reservations, used to find and change the user's entry
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LotteryDetailsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val reservationRepository: ReservationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<LotteryDetailsState>(LotteryDetailsState.Loading)
    val state: StateFlow<LotteryDetailsState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    // Kept after loading so join() and reloads know which lottery, campsite and user to use
    private var lotteryId: String = ""
    private var loadedLottery: Lottery? = null
    private var userId: String = ""

    /**
     * Responsible for observing lottery.
     *
     * @param lotteryId Lottery ID
     */
    fun observe(lotteryId: String) {
        this.lotteryId = lotteryId

        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            // Only show "Loading..." the first time; on reload, keep the old screen up.
            if (_state.value !is LotteryDetailsState.Content) _state.value = LotteryDetailsState.Loading

            observeCurrentUser()
                .catch { _state.value = LotteryDetailsState.Error("Unable to load lottery") }
                .collect { user ->
                    if (user == null) {
                        _state.value = LotteryDetailsState.Error("Sign in to view this lottery")
                    } else {
                        try {
                            _state.value = loadContent(lotteryId, user)
                        } catch (e: Exception) {
                            // Failed fetch
                            _state.value = LotteryDetailsState.Error("Unable to load lottery")
                        }
                    }
                }
        }
    }

    /**
     * Responsible for fetching the lottery, its campsites and the user's entry,
     * and turning them into what the screen shows.
     *
     * @param lotteryId ID of the lottery to load
     * @param user The signed-in user
     */
    private suspend fun loadContent(lotteryId: String, user: User): LotteryDetailsState {
        val lottery = lotteryRepository.getLottery(lotteryId)
            ?: return LotteryDetailsState.Error("Lottery not found")

        // Every reservation at one of this lottery's campsites
        val lotteryReservations = reservationRepository.getReservations().orEmpty()
            .filter { it.campsiteId in lottery.campsiteIds }

        // The user's own entry, if they have one that is still active
        var myReservation: Reservation? = null
        for (reservation in lotteryReservations) {
            val isActive = reservation.reservationState == ReservationState.Waitlisted ||
                    reservation.reservationState == ReservationState.Pending ||
                    reservation.reservationState == ReservationState.Reserved

            if (reservation.userId == user.userId && isActive) {
                myReservation = reservation
                break
            }
        }

        val waitlistSize = lotteryReservations.count {
            it.reservationState == ReservationState.Waitlisted
        }

        // Load each campsite, and check whether the user owns any of them
        val campsites = mutableListOf<LotteryCampsite>()
        var isOwner = false
        for (campsiteId in lottery.campsiteIds) {
            val campsite = campsiteRepository.getCampsite(campsiteId) ?: continue
            campsites.add(
                LotteryCampsite(
                    campsiteId = campsiteId,
                    name = campsite.name,
                    address = campsite.location.address,
                )
            )
            if (campsite.ownerUserId == user.userId) isOwner = true
        }

        val title = if (campsites.isEmpty()) "Lottery" else "${campsites[0].name} Lottery"

        loadedLottery = lottery
        userId = user.userId

        return LotteryDetailsState.Content(
            title = title,
            registrationText = periodText(lottery.registrationPeriod),
            bookingText = periodText(lottery.bookingPeriod),
            winnerCount = lottery.winnerCount,
            waitlistText = "$waitlistSize / ${lottery.waitlistCapacity}",
            campsites = campsites,
            action = actionFor(lottery, myReservation, waitlistSize),
            isOwner = isOwner,
        )
    }

    /**
     * Responsible for joining the lottery or its waitlist, creates a Waitlisted reservation for
     * the user at the lottery's first campsite.
     */
    fun join() {
        val lottery = loadedLottery ?: return
        if (lottery.campsiteIds.isEmpty()) return

        runAction {
            val reservation = Reservation(
                // Random unique ID
                reservationId = UUID.randomUUID().toString(),
                reservationState = ReservationState.Waitlisted,
                timestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
                campsiteId = lottery.campsiteIds[0],
                userId = userId,
            )
            reservationRepository.createReservation(reservation)
        }
    }

    fun leaveWaitlist(reservationId: String) {
        runAction {
            reservationRepository.updateReservationState(reservationId, ReservationState.Canceled)
        }
    }

    private fun runAction(block: suspend () -> Unit) {
        val current = _state.value as? LotteryDetailsState.Content ?: return
        if (current.isWorking) return // ignore double taps

        _state.value = current.copy(isWorking = true, actionError = null)

        viewModelScope.launch {
            try {
                block()
                observe(lotteryId)
            } catch (e: Exception) {
                _state.update {
                    (it as? LotteryDetailsState.Content)
                        ?.copy(isWorking = false, actionError = "Something went wrong. Try again.")
                        ?: it
                }
            }
        }
    }

    /**
     * Responsible for deciding which button the user sees.
     *
     * @param lottery The lottery being shown
     * @param myReservation The user's active entry, or null
     * @param waitlistSize How many people are on the waitlist
     */
    private fun actionFor(lottery: Lottery, myReservation: Reservation?, waitlistSize: Int): LotteryAction {
        // Already entered: waitlisted can leave, won or booked can view their reservation
        if (myReservation != null) {
            return if (myReservation.reservationState == ReservationState.Waitlisted) {
                LotteryAction.LeaveWaitlist(myReservation.reservationId)
            } else {
                LotteryAction.ViewReservation(myReservation.reservationId)
            }
        }

        if (lottery.campsiteIds.isEmpty() || waitlistSize >= lottery.waitlistCapacity) {
            return LotteryAction.None
        }

        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()).toInt()
        val registration = lottery.registrationPeriod
        val booking = lottery.bookingPeriod

        return when {
            today >= registration.start && today <= registration.end -> LotteryAction.JoinLottery
            today > registration.end && today <= booking.end -> LotteryAction.JoinWaitlist
            else -> LotteryAction.None
        }
    }

    // Responsible for formatting a period as start - end
    private fun periodText(period: Period): String {
        if (period.start <= 0 && period.end <= 0) return "Not set yet"
        return "${dayText(period.start)} - ${dayText(period.end)}"
    }

    // Responsible for formatting one day number as a date.
    private fun dayText(day: Int): String {
        val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC") // day numbers are UTC days
        return format.format(Date(TimeUnit.DAYS.toMillis(day.toLong())))
    }

}