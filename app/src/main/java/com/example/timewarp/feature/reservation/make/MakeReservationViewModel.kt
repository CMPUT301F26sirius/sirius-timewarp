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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * State and logic for the Make reservation screen: loads the won reservation, works out
 * which dates can be booked, and confirms with the picked dates (US 01.05.01, US 01.04.01).
 * Outstanding: without a saved lotteryId, the lottery is found by campsite (ambiguous if lotteries overlap);
 * dates aren't re-checked on Confirm, so two winners on the screen at the same time could pick the same nights.
 */

/** What the Make reservation screen shows: loading, the reservation, confirmed, or an error. */

sealed interface MakeReservationState {
    data object Loading : MakeReservationState

    /**
     * Everything the Make reservation screen shows.
     *
     * @param stayWindow days the winner may stay in, or null if no dates are set anywhere.
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
        val checkOut: Int? = null
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

                    stayWindow = findStayWindow(ownerAvailability, findBookingPeriod(reservation))

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

    /** Confirms the reservation with [reservationId], saving the picked dates, and notifies the entrant. */
    fun confirm(reservationId: String) {
        var campsite: Campsite? = null
        var checkIn: Int? = null
        var checkOut: Int? = null

        val current = _state.value
        if (current is MakeReservationState.Content) {
            // Reuse the loaded campsite for the notification text
            campsite = current.campsite

            if (current.needsDates) {
                // Don't confirm without valid dates; the button should already be off
                if (!current.datesAreValid) {
                    return
                }
                checkIn = current.checkIn
                checkOut = current.checkOut
            }
        }

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

    // Picks the days to show: the owner's open dates, or the lottery's booking period if the owner set none
    private fun findStayWindow(ownerAvailability: Availability, lotteryPeriod: Period?): Period? {
        var window: Period? = null

        val ownerStart = DayFormatter.fromDateString(ownerAvailability.startDate)
        val ownerEnd = DayFormatter.fromDateString(ownerAvailability.endDate)
        if (ownerStart != null && ownerEnd != null && ownerEnd >= ownerStart) {
            window = Period(start = ownerStart, end = ownerEnd)
        } else if (lotteryPeriod != null) {
            window = lotteryPeriod
        }

        // No dates set anywhere
        if (window == null) {
            return null
        }

        // Past days can't be booked, so start no earlier than today
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        if (window.start < today) {
            return Period(start = today, end = window.end)
        }
        return window
    }

    // Finds the lottery's booking period: by lotteryId if saved, otherwise the lottery that lists this campsite
    private suspend fun findBookingPeriod(reservation: Reservation): Period? {
        if (reservation.lotteryId.isNotEmpty()) {
            val lottery = lotteryRepository.getLottery(reservation.lotteryId)
            if (lottery != null) {
                return usablePeriod(lottery.bookingPeriod)
            }
        }

        val lotteries = lotteryRepository.observeCampsiteLotteries(reservation.campsiteId).first()
        if (lotteries.isEmpty()) {
            return null
        }
        return usablePeriod(lotteries[0].bookingPeriod)
    }

    // A period of 0 to 0 means it was never set
    private fun usablePeriod(period: Period): Period? {
        if (period.start <= 0 && period.end <= 0) {
            return null
        }
        return period
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