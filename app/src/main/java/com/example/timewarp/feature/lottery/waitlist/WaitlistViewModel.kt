package com.example.timewarp.feature.lottery.waitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.data.repository.FirebaseReservationRepository
import com.example.timewarp.domain.model.Lottery
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
import com.example.timewarp.feature.lotteries.LotteryTab
import com.example.timewarp.feature.lottery.details.LotteryDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [WaitlistState] State of the ui
 */
sealed interface WaitlistState {

    data object Loading : WaitlistState

    data class Content(
        val lotteryId: String,
        val reservations: List<Reservation>
    ): WaitlistState

    data class Error(val message: String): WaitlistState

}



/**
 * [WaitlistViewModel] for the [WaitlistScreen].
 *
 * @param reservationRepository reservation Repo to pull reservations from
 * @param observeCurrentUser Gets current user to get owned Lotteries
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaitlistViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<WaitlistState>(WaitlistState.Loading)
    val state: StateFlow<WaitlistState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    // Observes the supplied lottery ID. Called by screen when changed.
    fun observe(lotteryId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            if (_state.value !is WaitlistState.Content) _state.value = WaitlistState.Loading

            observeCurrentUser()
                .catch { _state.value = WaitlistState.Error("Unable to load lottery") }
                .collect { user ->
                    if (user == null) {
                        _state.value = WaitlistState.Error("Unable to load user")
                    } else {
                        try {
                            _state.value = loadContent(lotteryId, user)
                        } catch (e: Exception) {
                            _state.value = WaitlistState.Error("Unable to load lottery")
                        }
                    }
                }
        }
    }

    private suspend fun loadContent(lotteryId: String, user: User): WaitlistState {

        val reservations = getWaitlistForLottery(lotteryId)

        return WaitlistState.Content(
            lotteryId = lotteryId,
            reservations = reservations
        )
    }

    /**
     * Gets Waitlist entries for a given lotteryID
     *
     * @param lotteryId ID of lottery to get waitlisted reservations for
     */
    private suspend fun getWaitlistForLottery(lotteryId: String): List<Reservation> {
        val reservations = reservationRepository.getReservations()
        return reservations?.filter {it.lotteryId == lotteryId && it.reservationState == ReservationState.Waitlisted } ?: emptyList()
    }
}