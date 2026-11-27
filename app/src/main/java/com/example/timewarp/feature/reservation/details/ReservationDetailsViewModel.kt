package com.example.timewarp.feature.reservation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.CancelReservationUseCase
import com.example.timewarp.domain.usecase.DeclineReservationUseCase
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.RejectReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* Loads one reservation for the Manage reservation screen and works out which actions the viewer gets:
* the entrant can cancel a booking (US 01.07.01), or book or decline a win (US 01.04.03);
* the campsite owner or an admin can reject it (US 02.08.01, US 03.11.01).
* Outstanding: a won reservation without a chosen campsite shows "Unknown campsite".
*/

/** What the Manage reservation screen shows. */
sealed interface ReservationDetailsState {
    data object Loading : ReservationDetailsState

    /**
     * The reservation and the actions the current user can take on it.
     *
     * @param campsite the reserved campsite, or null if none is chosen yet.
     * @param canCancel the entrant can cancel their booked stay.
     * @param canMakeReservation the entrant won and can still book it.
     * @param canDecline the entrant won and can turn it down.
     * @param canReject the campsite owner or an admin can reject it.
     */
    data class Content(
        val reservation: Reservation,
        val campsite: Campsite?,
        val canCancel: Boolean,
        val canMakeReservation: Boolean = false,
        val canDecline: Boolean = false,
        val canReject: Boolean = false
    ) : ReservationDetailsState

    data class Cancelled(val reservationId: String) : ReservationDetailsState

    /** The entrant declined it, so the screen goes back. */
    data object Declined : ReservationDetailsState

    /** The owner or admin rejected it, so the screen goes back. */
    data object Rejected : ReservationDetailsState

    data class Error(val message: String) : ReservationDetailsState
}

/** ViewModel for the Manage reservation screen. */
@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val cancelReservation: CancelReservationUseCase,
    private val declineReservation: DeclineReservationUseCase,
    private val rejectReservation: RejectReservationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationDetailsState>(ReservationDetailsState.Loading)
    val state: StateFlow<ReservationDetailsState> = _state.asStateFlow()

    /** Loads the reservation with [reservationId], its campsite, and what the current user can do. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = ReservationDetailsState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = ReservationDetailsState.Error("Reservation not found.")
                    return@launch
                }

                val campsite = loadCampsite(reservation.campsiteId)
                val user = observeCurrentUser().first()

                // The entrant this reservation belongs to
                var isEntrant = false
                if (user != null && reservation.userId == user.userId) {
                    isEntrant = true
                }

                // The campsite's owner, or an admin who can do what owners can (US 03.11.01)
                var canManage = false
                if (user != null && !isEntrant) {
                    if (user.userType == UserType.Admin) {
                        canManage = true
                    } else if (ownsReservationCampsite(user.userId, reservation, campsite)) {
                        canManage = true
                    }
                }

                // A won (Pending) reservation can be booked or declined by its entrant
                val wonByViewer = isEntrant && declineReservation.canDecline(reservation)

                _state.value = ReservationDetailsState.Content(
                    reservation = reservation,
                    campsite = campsite,
                    canCancel = isEntrant && cancelReservation.canCancel(reservation),
                    canMakeReservation = wonByViewer,
                    canDecline = wonByViewer,
                    canReject = canManage && rejectReservation.canReject(reservation)
                )
            } catch (error: Exception) {
                _state.value = ReservationDetailsState.Error(
                    error.message ?: "Unable to load reservation."
                )
            }
        }
    }

    /** Cancels the reservation with [reservationId]. */
    fun cancel(reservationId: String) {
        viewModelScope.launch {
            try {
                val cancelled = cancelReservation(reservationId)
                if (cancelled) {
                    _state.value = ReservationDetailsState.Cancelled(reservationId)
                } else {
                    _state.value = ReservationDetailsState.Error("This reservation can't be cancelled.")
                }
            } catch (error: Exception) {
                _state.value = ReservationDetailsState.Error(
                    error.message ?: "Unable to cancel reservation."
                )
            }
        }
    }

    /** Declines the won reservation with [reservationId] (US 01.04.03). */
    fun decline(reservationId: String) {
        viewModelScope.launch {
            try {
                val declined = declineReservation(reservationId)
                if (declined) {
                    _state.value = ReservationDetailsState.Declined
                } else {
                    _state.value = ReservationDetailsState.Error("This reservation can't be declined.")
                }
            } catch (error: Exception) {
                _state.value = ReservationDetailsState.Error(
                    error.message ?: "Unable to decline reservation."
                )
            }
        }
    }

    /** Rejects the reservation with [reservationId] and notifies its entrant (US 02.08.01). */
    fun reject(reservationId: String) {
        // Reuse the loaded campsite for the notification text
        var campsite: Campsite? = null
        val current = _state.value
        if (current is ReservationDetailsState.Content) {
            campsite = current.campsite
        }

        viewModelScope.launch {
            try {
                val rejected = rejectReservation(reservationId, campsite)
                if (rejected) {
                    _state.value = ReservationDetailsState.Rejected
                } else {
                    _state.value = ReservationDetailsState.Error("This reservation can't be rejected.")
                }
            } catch (error: Exception) {
                _state.value = ReservationDetailsState.Error(
                    error.message ?: "Unable to reject reservation."
                )
            }
        }
    }

    // Reads the campsite once; null if the reservation has no campsite id
    private suspend fun loadCampsite(campsiteId: String): Campsite? {
        if (campsiteId.isEmpty()) {
            return null
        }
        return campsiteRepository.observeCampsite(campsiteId).first()
    }

    // True if the user owns the reservation's campsite, or any campsite in its lottery if none is chosen yet
    private suspend fun ownsReservationCampsite(
        userId: String,
        reservation: Reservation,
        campsite: Campsite?
    ): Boolean {
        if (campsite != null) {
            return campsite.ownerUserId == userId
        }

        // Waitlisted and won reservations only know their lottery
        if (reservation.lotteryId.isEmpty()) {
            return false
        }
        val lottery = lotteryRepository.getLottery(reservation.lotteryId)
        if (lottery == null) {
            return false
        }

        for (campsiteId in lottery.campsiteIds) {
            val lotteryCampsite = campsiteRepository.getCampsite(campsiteId)
            if (lotteryCampsite != null && lotteryCampsite.ownerUserId == userId) {
                return true
            }
        }
        return false
    }
}