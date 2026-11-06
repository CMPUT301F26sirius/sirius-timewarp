package com.example.timewarp.feature.reservation.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* Loads a confirmed reservation for the Reservation confirmed screen (US 01.05.02).
*/

sealed interface ReservationConfirmationState {
    data object Loading : ReservationConfirmationState
    data class Content(val reservation: Reservation, val campsite: Campsite?) : ReservationConfirmationState
    data class Error(val message: String) : ReservationConfirmationState
}

/** ViewModel for the Reservation confirmed screen. */
@HiltViewModel
class ReservationConfirmationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationConfirmationState>(ReservationConfirmationState.Loading)
    val state: StateFlow<ReservationConfirmationState> = _state.asStateFlow()

    /** Loads the confirmed reservation with [reservationId] and its campsite. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = ReservationConfirmationState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = ReservationConfirmationState.Error("Reservation not found.")
                    return@launch
                }

                // No campsite id means there is nothing to look up
                var campsite: Campsite? = null
                if (reservation.campsiteId.isNotEmpty()) {
                    campsite = campsiteRepository.getCampsite(reservation.campsiteId)
                }

                _state.value = ReservationConfirmationState.Content(reservation, campsite)
            } catch (error: Exception) {
                _state.value = ReservationConfirmationState.Error(
                    error.message ?: "Unable to load reservation."
                )
            }
        }
    }
}