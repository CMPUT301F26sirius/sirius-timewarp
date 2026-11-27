package com.example.timewarp.feature.notifications

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface NotificationsState {
    data object Idle : NotificationsState

    data class Content(
        val notifications: List<Notification>,
    ): NotificationsState
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Idle)
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    private fun getUsersSentNotifications(userID: String): List<Notification> {

    }
}
