package com.example.timewarp.feature.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

sealed interface StartupState {
    data object Loading : StartupState
    data object Registration : StartupState
    data object ProfileSetup : StartupState
    data object Browse : StartupState
    data class Error(val message: String) : StartupState
}

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow<StartupState>(StartupState.Loading)
    val state = _state.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch() {
            delay(1000L.milliseconds)

            try {
                val userId = authRepository.getUserId()

                if (userId == null) {
                    _state.value = StartupState.Registration
                    return@launch
                }

                val user = userRepository.getUser(userId)

                _state.value = if (user != null) {
                    StartupState.Browse
                } else {
                    StartupState.ProfileSetup
                }
            } catch (e: Exception) {
                _state.value = StartupState.Error(
                    e.message ?: "Unable to check your session."
                )
            }
        }
    }
}