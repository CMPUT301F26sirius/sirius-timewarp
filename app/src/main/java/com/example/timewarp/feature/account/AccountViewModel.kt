package com.example.timewarp.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AccountState {
    data object Idle : AccountState
    data class Error(val message: String) : AccountState
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountState>(AccountState.Idle)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    val currentUser = observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isUpdatingNotifications = MutableStateFlow(false)
    val isUpdatingNotifications: StateFlow<Boolean> = _isUpdatingNotifications.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        val user = currentUser.value
        if (user == null) {
            _state.value = AccountState.Error("Unable to find the signed-in user")
            return
        }

        viewModelScope.launch {
            _isUpdatingNotifications.value = true
            try {
                userRepository.updateNotificationsEnabled(user.userId, enabled)
                _state.value = AccountState.Idle
            } catch (e: Exception) {
                _state.value = AccountState.Error("Unable to update notification settings")
            } finally {
                _isUpdatingNotifications.value = false
            }
        }
    }
}