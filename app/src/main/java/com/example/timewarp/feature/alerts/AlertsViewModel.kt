package com.example.timewarp.feature.alerts

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AlertsState {
    data object Idle : AlertsState
}

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val messageRepository: MessageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AlertsState>(AlertsState.Idle)
    val state: StateFlow<AlertsState> = _state.asStateFlow()


}