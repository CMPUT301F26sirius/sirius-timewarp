package com.example.timewarp.feature.reservationconfirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.timewarp.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReservationConfirmationState(
    val confirmation: ReservationConfirmation? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReservationConfirmationViewModel @Inject constructor (
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReservationConfirmationState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            reservationRepository.getReservations().collect { reservations ->
                val confirmation = reservations.firstOrNull()?.let { ReservationConfirmation(it) }
                _uiState.update { it.copy(confirmation = confirmation, isLoading = false) }
            }
        }
    }
}