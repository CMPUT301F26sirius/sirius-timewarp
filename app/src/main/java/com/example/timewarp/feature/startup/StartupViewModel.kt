package com.example.timewarp.feature.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.feature.registration.RegistrationState
import com.example.timewarp.feature.registration.RegistrationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * [StartupState] State of the ui
 */
sealed interface StartupState {
    data object Loading : StartupState
    data object Registration : StartupState
    data object ProfileSetup : StartupState
    data object Browse : StartupState
    data class Error(val message: String) : StartupState
}

/**
 * [StartupViewModel] for the [StartupState].
 *
 * Checks auth state when displayed and sets state accordingly so screen can navigate.
 */
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

    /**
     * Checks the users auth status and updates the state accordingly.
     */
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