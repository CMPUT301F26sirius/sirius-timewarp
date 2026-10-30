package com.example.timewarp.feature.reservations

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ReservationsState {
    data object Idle : ReservationsState
}

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationsState>(ReservationsState.Idle)
    val state: StateFlow<ReservationsState> = _state.asStateFlow()


}