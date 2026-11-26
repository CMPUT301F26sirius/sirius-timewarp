package com.example.timewarp.feature.alerts.view.sent.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SentNotificationsState {

    data object  Loading : SentNotificationsState

    data class Content(
        val userId: String,
        val notifications: List<Notification>
    ): SentNotificationsState

    data class Error(val message: String): SentNotificationsState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ViewSentNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<SentNotificationsState>(SentNotificationsState.Loading)
    val state: StateFlow<SentNotificationsState> = _state.asStateFlow()

    private var observationJob: Job? = null

    fun observe(userId: String) {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            if (_state.value !is SentNotificationsState.Content) _state.value = SentNotificationsState.Loading

            observeCurrentUser()
                .collect { user ->
                    if (user == null) {
                        _state.value = SentNotificationsState.Error("Unable to load user")
                    } else {
                        try {
                            _state.value = loadContent(userId)
                        } catch (e: Exception) {
                            _state.value = SentNotificationsState.Error("Unable to load notifications")
                        }
                    }
                }
        }
    }

    /**
     * Get notification sent by given user
     *
     * @param userId ID of user to get notifications sent by.
     */
    private  suspend fun loadContent(userId: String): SentNotificationsState {
        val notifications = notificationRepository.getOutgoingNotifications(userId)

        return SentNotificationsState.Content(
            userId = userId,
            notifications = notifications?.toList().orEmpty()
        )
    }
}