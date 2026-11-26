package com.example.timewarp.feature.reservation.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.CampsiteRepository
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
 * State and logic for the Make reservation screen: loads the won reservation, works out
 * which dates can be booked, and confirms with the picked dates (US 01.05.01, US 01.04.01).
 * Available dates come from the owner's campsite availability minus nights other people reserved.
 * Outstanding: dates aren't re-checked on Confirm, so two winners on the screen at the same time could pick the same nights;
 * Confirm isn't limited to the lottery's booking period yet.
 */

/** What the Make reservation screen shows: loading, the reservation, confirmed, or an error. */
sealed interface MakeReservationState {
    data object Loading : MakeReservationState

    /**
     * Everything the Make reservation screen shows.
     *
     * @param stayWindow the owner's open dates from today on, or null if the owner set none.
     * @param bookableDays days that can be picked.
     * @param checkIn picked check-in day, or null.
     * @param checkOut picked check-out day, or null.
     */
    data class Content(
        val reservation: Reservation,
        val campsite: Campsite?,
        val canConfirm: Boolean,
        val stayWindow: Period? = null,
        val bookableDays: List<Int> = emptyList(),
        val checkIn: Int? = null,
        val checkOut: Int? = null,
        val rulesAcknowledged: Boolean = false
    ) : MakeReservationState {

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
    }

    data class Confirmed(val reservationId: String) : MakeReservationState
    data class Error(val message: String) : MakeReservationState
}

/** ViewModel for the Make reservation screen. */
@HiltViewModel
class MakeReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository,
    private val confirmReservation: ConfirmReservationUseCase,
    private val findBookableDays: FindBookableDaysUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<MakeReservationState>(MakeReservationState.Loading)
    val state: StateFlow<MakeReservationState> = _state.asStateFlow()

    /** Loads the reservation with [reservationId], its campsite, and the days it can book. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = MakeReservationState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = MakeReservationState.Error("Reservation not found.")
                    return@launch
                }

                var campsite: Campsite? = null
                var stayWindow: Period? = null
                var bookableDays: List<Int> = emptyList()

                // No campsite id means there is nothing to look up
                if (reservation.campsiteId.isNotEmpty()) {
                    campsite = campsiteRepository.getCampsite(reservation.campsiteId)

                    // Owner's dates and weekdays, or no limits if the campsite couldn't be loaded
                    var ownerAvailability = Availability()
                    if (campsite != null) {
                        ownerAvailability = campsite.availability
                    }

                    stayWindow = findStayWindow(ownerAvailability)

                    // Work out the free days only when there are dates to show
                    if (stayWindow != null) {
                        bookableDays = findBookableDays(
                            stayWindow = stayWindow,
                            ownerAvailability = ownerAvailability,
                            campsiteReservations = loadCampsiteReservations(reservation.campsiteId),
                            reservationId = reservation.reservationId
                        )
                    }
                }

                _state.value = MakeReservationState.Content(
                    reservation = reservation,
                    campsite = campsite,
                    canConfirm = confirmReservation.canConfirm(reservation),
                    stayWindow = stayWindow,
                    bookableDays = bookableDays
                )
            } catch (error: Exception) {
                _state.value = MakeReservationState.Error(
                    error.message ?: "Unable to load reservation."
                )
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

    // All reservations at this campsite, used to find booked nights
    private suspend fun loadCampsiteReservations(campsiteId: String): List<Reservation> {
        val atCampsite = mutableListOf<Reservation>()
        for (reservation in reservationRepository.getReservations().orEmpty()) {
            if (reservation.campsiteId == campsiteId) {
                atCampsite.add(reservation)
            }
        }
        return atCampsite
    }
}