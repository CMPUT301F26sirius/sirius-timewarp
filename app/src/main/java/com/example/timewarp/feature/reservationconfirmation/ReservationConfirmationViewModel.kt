package com.example.timewarp.feature.reservationconfirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.feature.startup.StartupState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.uuid.Uuid

sealed interface ReservationConfirmationState {
    data object Loading : ReservationConfirmationState
    data object  Empty : ReservationConfirmationState
    data class Success(val reservation: Reservation) : ReservationConfirmationState
    data class Error(val message: String): ReservationConfirmationState
}

@HiltViewModel
class ReservationConfirmationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
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