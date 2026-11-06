package com.example.timewarp.feature.reservation.make

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ConfirmReservationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Loads a won reservation and confirms it (US 01.05.01).
 * Outstanding: picking dates on a calendar (US 01.04.01) isn't wired in yet.
 */

sealed interface MakeReservationState {
    data object Loading : MakeReservationState
    data class Content(
        val reservation: Reservation,
        val campsite: Campsite?,
        val canConfirm: Boolean
    ) : MakeReservationState
    data class Confirmed(val reservationId: String) : MakeReservationState
    data class Error(val message: String) : MakeReservationState
}

/** ViewModel for the Make reservation screen. */
@HiltViewModel
class MakeReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository,
    private val confirmReservation: ConfirmReservationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<MakeReservationState>(MakeReservationState.Loading)
    val state: StateFlow<MakeReservationState> = _state.asStateFlow()

    /** Loads the reservation with [reservationId] and its campsite. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = MakeReservationState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = MakeReservationState.Error("Reservation not found.")
                    return@launch
                }

                // No campsite id means there is nothing to look up
                var campsite: Campsite? = null
                if (reservation.campsiteId.isNotEmpty()) {
                    campsite = campsiteRepository.getCampsite(reservation.campsiteId)
                }

                _state.value = MakeReservationState.Content(
                    reservation = reservation,
                    campsite = campsite,
                    canConfirm = confirmReservation.canConfirm(reservation)
                )
            } catch (error: Exception) {
                _state.value = MakeReservationState.Error(
                    error.message ?: "Unable to load reservation."
                )
            }
        }
    }

    /** Confirms the reservation with [reservationId] and notifies the entrant. */
    fun confirm(reservationId: String) {
        // Reuse the loaded campsite for the notification text
        var campsite: Campsite? = null
        val current = _state.value
        if (current is MakeReservationState.Content) {
            campsite = current.campsite
        }

        viewModelScope.launch {
            try {
                val confirmed = confirmReservation(reservationId, campsite)
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
}