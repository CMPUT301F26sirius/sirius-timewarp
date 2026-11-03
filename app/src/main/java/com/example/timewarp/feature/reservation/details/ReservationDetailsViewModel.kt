package com.example.timewarp.feature.reservation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.CancelReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


/*
* Loads one reservation for the Manage reservation screen and cancels it (US 01.07.01).
* Outstanding: check-in/check-out dates aren't stored on Reservation yet.
*/

sealed interface ReservationDetailsState {
    data object Loading : ReservationDetailsState
    data class Content(
        val reservation: Reservation,
        val campsite: Campsite?,
        val canCancel: Boolean
    ) : ReservationDetailsState
    data class Cancelled(val reservationId: String) : ReservationDetailsState
    data class Error(val message: String) : ReservationDetailsState
}

/** ViewModel for the Manage reservation screen. */
@HiltViewModel
class ReservationDetailsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository,
    private val cancelReservation: CancelReservationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationDetailsState>(ReservationDetailsState.Loading)
    val state: StateFlow<ReservationDetailsState> = _state.asStateFlow()

    /** Loads the reservation with [reservationId] and its campsite. */
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
                _state.value = ReservationDetailsState.Content(
                    reservation = reservation,
                    campsite = campsite,
                    canCancel = cancelReservation.canCancel(reservation)
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

    // Reads the campsite once; null if the reservation has no campsite id
    private suspend fun loadCampsite(campsiteId: String): Campsite? {
        if (campsiteId.isEmpty()) {
            return null
        }
        return campsiteRepository.observeCampsite(campsiteId).first()
    }
}