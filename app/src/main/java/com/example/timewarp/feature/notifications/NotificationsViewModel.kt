package com.example.timewarp.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface NotificationsState {
    data object Idle : NotificationsState

    data class Content(
        val incomingNotifications: MutableList<Notification>?,
        val outgoingNotifications: MutableList<Notification>?,
        val allNotifications: MutableList<Notification>?,
        val userType: UserType,
        val tabs: List<NotificationsTab> = listOf(NotificationsTab.Incoming),
        val usersNames: Map<String, String> = emptyMap()
    ): NotificationsState

    data class Error(val message: String): NotificationsState
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<NotificationsState>(NotificationsState.Idle)
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    fun observe() {

        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            if (_state.value !is NotificationsState.Content) _state.value = NotificationsState.Idle

            observeCurrentUser()
                .catch { _state.value = NotificationsState.Error("Unable to load Notifications") }
                .collect { user ->
                    if (user == null) {
                        _state.value = NotificationsState.Error("Unable to load user")
                    } else {
                        try {
                            _state.value = linkNames(loadContent(user))
                        } catch (e: Exception) {
                            _state.value = NotificationsState.Error("Unable to load Notifications")
                        }
                    }
                }
        }
    }

    private suspend fun loadContent(user: User): NotificationsState  {
        val userId = user.userId
        val type = user.userType
        if (type == UserType.Entrant) {
            return NotificationsState.Content(
                incomingNotifications = getUsersReceivedNotifications(userId),
                outgoingNotifications = null,
                allNotifications = null,
                userType = type,
                tabs = NotificationsTab.tabsFor(type)
            )
        } else if (type == UserType.CampsiteOwner) {
            return NotificationsState.Content(
                incomingNotifications = getUsersReceivedNotifications(userId),
                outgoingNotifications = getUsersSentNotifications(userId),
                allNotifications = null,
                userType = type,
                tabs = NotificationsTab.tabsFor(type)
            )
        } else if (type == UserType.Admin) {
            return NotificationsState.Content(
                incomingNotifications = getUsersReceivedNotifications(userId),
                outgoingNotifications = getUsersSentNotifications(userId),
                allNotifications = getAllNotifications(),
                userType = type,
                tabs = NotificationsTab.tabsFor(type)
            )
        } else {
            return NotificationsState.Error("Unable to load Notifications")
        }
    }

    private suspend fun getUsersReceivedNotifications(userId: String): MutableList<Notification>? {
        return notificationRepository.getIncomingNotifications(userId)
    }

    private suspend fun getUsersSentNotifications(userID: String): MutableList<Notification>? {
        return notificationRepository.getOutgoingNotifications(userID)
    }

    private suspend fun getAllNotifications(): MutableList<Notification>? {
        return notificationRepository.getNotifications()
    }

    private suspend fun getNameForUser(userId: String): String {
        val profile = observeUserProfile(userId)
            .catch { emit(null) }
            .first()
            ?.profile
        val name = profile?.let { "${it.firstName} ${it.lastName}".trim() }
        return name?.takeIf { it.isNotBlank() } ?: "(no name)"
    }

    private suspend fun linkNames(state: NotificationsState): NotificationsState {
        if (state !is NotificationsState.Content) return state

        val notification = state.incomingNotifications.orEmpty() + state.incomingNotifications.orEmpty() + state.allNotifications.orEmpty()
        val userIds = notification.flatMap { listOf(it.fromUserId, it.toUserId) }.distinct()
        return state.copy(usersNames = userIds.associateWith { getNameForUser(it) })
    }
}
