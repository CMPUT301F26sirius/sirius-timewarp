package com.example.timewarp.feature.reservation.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Eric_Reservation
import com.example.timewarp.domain.repository.Eric_ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReservationConfirmationState {
    data object Loading : ReservationConfirmationState
    data object  Empty : ReservationConfirmationState
    data class Success(val reservation: Eric_Reservation) : ReservationConfirmationState
    data class Error(val message: String): ReservationConfirmationState
}

@HiltViewModel
class ReservationConfirmationViewModel @Inject constructor(
    private val reservationRepository: Eric_ReservationRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationConfirmationState>(
        ReservationConfirmationState.Loading
    )
    val state = _state.asStateFlow()

    init {
        loadConfirmation()
    }

    private fun loadConfirmation() {
        viewModelScope.launch {
            try {
                val idArg = savedStateHandle.get<String>("reservationId")

                if (idArg == null) {
                    _state.value = ReservationConfirmationState.Error(
                        "No Reservation ID"
                    )
                    return@launch
                }

                val reservation = reservationRepository.getReservation(idArg)

                _state.value = if (reservation != null) {
                    ReservationConfirmationState.Success(reservation)
                } else {
                    ReservationConfirmationState.Empty
                }
            } catch (e: Exception) {
                _state.value = ReservationConfirmationState.Error(
                    e.message ?: "Unable to load your reservation"
                )
            }
        }
    }
}