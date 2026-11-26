package com.example.timewarp.feature.reservation.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ConfirmReservationUseCase
import com.example.timewarp.domain.usecase.FindBookableDaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * State and logic for the Make reservation screen: loads the won reservation, lets the winner choose
 * one of the lottery's campsites and its dates, and confirms them (US 01.05.01, US 01.04.01).
 * Available dates come from the chosen campsite's owner availability minus nights other people reserved.
 * Outstanding: dates aren't re-checked on Confirm, so two winners on the screen at the same time could pick the same nights;
 * Confirm isn't limited to the lottery's booking period yet.
 */

/** What the Make reservation screen shows: loading, the reservation, confirmed, or an error. */
sealed interface MakeReservationState {
    data object Loading : MakeReservationState

    /**
     * Everything the Make reservation screen shows.
     *
     * @param campsite the chosen campsite, or null if there is none to choose.
     * @param campsites the campsites the winner can choose from (the lottery's campsites).
     * @param stayWindow the chosen campsite's open dates from today on, or null if the owner set none.
     * @param bookableDays days that can be picked at the chosen campsite.
     * @param checkIn picked check-in day, or null.
     * @param checkOut picked check-out day, or null.
     */
    data class Content(
        val reservation: Reservation,
        val campsite: Campsite?,
        val canConfirm: Boolean,
        val campsites: List<Campsite> = emptyList(),
        val stayWindow: Period? = null,
        val bookableDays: List<Int> = emptyList(),
        val checkIn: Int? = null,
        val checkOut: Int? = null,
        val rulesAcknowledged: Boolean = false
    ) : MakeReservationState {

        /** True when the lottery has more than one campsite, so the winner picks one. */
        val canChooseCampsite: Boolean
            get() = campsites.size > 1

        /** True when the winner has to pick dates before confirming. */
        val needsDates: Boolean
            get() = stayWindow != null

        /** True when dates are picked and every night is free. */
        val datesAreValid: Boolean
            get() {
                if (checkIn == null || checkOut == null) {
                    return false
                }
                return FindBookableDaysUseCase.isRangeFree(checkIn, checkOut, bookableDays)
            }


        val canSubmit: Boolean
            get() = canConfirm &&
                    campsite != null &&
                    rulesAcknowledged &&
                    (!needsDates || datesAreValid)

        /** Price of the picked nights at the chosen campsite, or null until valid dates are picked. */
        val totalCost: Double?
            get() {
                val chosen = campsite
                val start = checkIn
                val end = checkOut
                // Nothing to price yet
                if (chosen == null || start == null || end == null) {
                    return null
                }
                // Zero nights or a booked night in the range
                if (!datesAreValid) {
                    return null
                }
                val nights = end - start
                return nights * chosen.pricing.costPerNight
            }

    }

    data class Confirmed(val reservationId: String) : MakeReservationState
    data class Error(val message: String) : MakeReservationState
}

/** ViewModel for the Make reservation screen. */
@HiltViewModel
class MakeReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val confirmReservation: ConfirmReservationUseCase,
    private val findBookableDays: FindBookableDaysUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<MakeReservationState>(MakeReservationState.Loading)
    val state: StateFlow<MakeReservationState> = _state.asStateFlow()

    private var allReservations: List<Reservation> = emptyList()

    /** Loads the reservation with [reservationId], the campsites it can choose from, and their free days. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = MakeReservationState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = MakeReservationState.Error("Reservation not found.")
                    return@launch
                }

                allReservations = reservationRepository.getReservations().orEmpty()
                val campsites = loadCampsiteChoices(reservation)

                // Start on the campsite already saved on the reservation, if it's one of the choices
                var chosen: Campsite? = null
                for (campsite in campsites) {
                    if (campsite.campsiteId == reservation.campsiteId) {
                        chosen = campsite
                    }
                }
                // Otherwise start on the first campsite
                if (chosen == null && campsites.isNotEmpty()) {
                    chosen = campsites[0]
                }

                _state.value = buildContent(reservation, campsites, chosen)
            } catch (error: Exception) {
                _state.value = MakeReservationState.Error(
                    error.message ?: "Unable to load reservation."
                )
            }
        }
    }

    /** Switches to the campsite with [campsiteId] and clears picked dates, since they were for the old campsite. */
    fun selectCampsite(campsiteId: String) {
        val current = _state.value
        if (current !is MakeReservationState.Content) {
            return
        }

        for (campsite in current.campsites) {
            if (campsite.campsiteId == campsiteId) {
                // New state has no checkIn or checkOut, so the old dates are cleared
                _state.value = buildContent(current.reservation, current.campsites, campsite)
                return
            }
        }
    }

    /** Stores the check-in and check-out days picked in the calendar. */
    fun selectDates(checkIn: Int, checkOut: Int) {
        val current = _state.value
        if (current is MakeReservationState.Content) {
            _state.value = current.copy(checkIn = checkIn, checkOut = checkOut)
        }
    }


    fun setRulesAcknowledged(acknowledged: Boolean) {
        val current = _state.value as? MakeReservationState.Content ?: return
        _state.value = current.copy(rulesAcknowledged = acknowledged)
    }

    /** Confirms the reservation with [reservationId], saving the picked dates, and notifies the entrant. */
    fun confirm(reservationId: String) {
        val current = _state.value as? MakeReservationState.Content ?: return
        if (!current.canSubmit || current.reservation.reservationId != reservationId) return

        val campsite = current.campsite
        val checkIn = current.checkIn.takeIf { current.needsDates }
        val checkOut = current.checkOut.takeIf { current.needsDates }


        viewModelScope.launch {
            try {
                val confirmed = confirmReservation(reservationId, campsite, checkIn, checkOut)
                if (confirmed) {
                    _state.value = MakeReservationState.Confirmed(reservationId)
                } else {
                    _state.value = MakeReservationState.Error("This reservation can't be confirmed.")
                }
            } catch (error: Exception) {
                _state.value = MakeReservationState.Error(
                    error.message ?: "Unable to confirm reservation."
                )
            }
        }
    }

    // Builds the screen state for the chosen campsite: its open dates and which of them are still free
    private fun buildContent(
        reservation: Reservation,
        campsites: List<Campsite>,
        chosen: Campsite?
    ): MakeReservationState.Content {
        var stayWindow: Period? = null
        var bookableDays: List<Int> = emptyList()

        if (chosen != null) {
            stayWindow = findStayWindow(chosen.availability)

            // Work out the free days only when there are dates to show
            if (stayWindow != null) {
                bookableDays = findBookableDays(
                    stayWindow = stayWindow,
                    ownerAvailability = chosen.availability,
                    campsiteReservations = reservationsAt(chosen.campsiteId),
                    reservationId = reservation.reservationId
                )
            }
        }

        return MakeReservationState.Content(
            reservation = reservation,
            campsite = chosen,
            canConfirm = confirmReservation.canConfirm(reservation),
            campsites = campsites,
            stayWindow = stayWindow,
            bookableDays = bookableDays
        )
    }

    // The campsites the winner can choose from: the lottery's campsites, or the one saved on an older reservation
    private suspend fun loadCampsiteChoices(reservation: Reservation): List<Campsite> {
        val campsites = mutableListOf<Campsite>()

        if (reservation.lotteryId.isNotEmpty()) {
            val lottery = lotteryRepository.getLottery(reservation.lotteryId)
            if (lottery != null) {
                for (campsiteId in lottery.campsiteIds) {
                    val campsite = campsiteRepository.getCampsite(campsiteId)
                    // Skip campsites that no longer exist
                    if (campsite != null) {
                        campsites.add(campsite)
                    }
                }
            }
        }

        // Reservations made before join saved lotteryId only know their campsite
        if (campsites.isEmpty() && reservation.campsiteId.isNotEmpty()) {
            val campsite = campsiteRepository.getCampsite(reservation.campsiteId)
            if (campsite != null) {
                campsites.add(campsite)
            }
        }
        return campsites
    }

    // The days to show: the owner's open dates, starting no earlier than today
    private fun findStayWindow(ownerAvailability: Availability): Period? {
        val ownerStart = DayFormatter.fromDateString(ownerAvailability.startDate)
        val ownerEnd = DayFormatter.fromDateString(ownerAvailability.endDate)

        // Owner set no dates, or they don't make sense
        if (ownerStart == null || ownerEnd == null || ownerEnd < ownerStart) {
            return null
        }

        // Past days can't be booked, so start no earlier than today
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        if (ownerStart < today) {
            return Period(start = today, end = ownerEnd)
        }
        return Period(start = ownerStart, end = ownerEnd)
    }

    // Reservations at this campsite, used to find booked nights
    private fun reservationsAt(campsiteId: String): List<Reservation> {
        val atCampsite = mutableListOf<Reservation>()
        for (reservation in allReservations) {
            if (reservation.campsiteId == campsiteId) {
                atCampsite.add(reservation)
            }
        }
        return atCampsite
    }
}