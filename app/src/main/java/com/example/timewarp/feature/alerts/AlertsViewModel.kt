package com.example.timewarp.feature.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * This interface shows the UI state for the alerts screen
 */
sealed interface AlertsState {
    data object Idle : AlertsState
}

/**
 * This is the viewmodel for the alerts feature that shows the screen state and checks if the
 * current user is an admin
 *
 * @property notificationRepository for notification data
 * @property messageRepository for message data
 */
@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val messageRepository: MessageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AlertsState>(AlertsState.Idle)
    val state: StateFlow<AlertsState> = _state.asStateFlow()

}