package com.example.timewarp.feature.reservation.cancellation

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* Loads a cancelled reservation for the Cancellation registered screen (US 01.07.01).
*/
sealed interface CancellationState {
    data object Loading : CancellationState
    data class Content(val reservation: Reservation, val campsite: Campsite?) : CancellationState
    data class Error(val message: String) : CancellationState
}

/** ViewModel for the Cancellation registered screen. */
@HiltViewModel
class CancellationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val campsiteRepository: CampsiteRepository
) : ViewModel() {
    private val _state = MutableStateFlow<CancellationState>(CancellationState.Loading)
    val state: StateFlow<CancellationState> = _state.asStateFlow()

    /** Loads the cancelled reservation with [reservationId] and its campsite. */
    fun load(reservationId: String) {
        viewModelScope.launch {
            _state.value = CancellationState.Loading
            try {
                val reservation = reservationRepository.getReservation(reservationId)
                if (reservation == null) {
                    _state.value = CancellationState.Error("Reservation not found.")
                    return@launch
                }

                var campsite: Campsite? = null
                if (reservation.campsiteId.isNotEmpty()) {
                    campsite = campsiteRepository.observeCampsite(reservation.campsiteId).first()
                }

                _state.value = CancellationState.Content(reservation, campsite)
            } catch (error: Exception) {
                _state.value = CancellationState.Error(
                    error.message ?: "Unable to load cancellation."
                )
            }
        }
    }
}