package com.example.timewarp.feature.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * [RegistrationState] State of the ui
 */
sealed interface RegistrationState {
    data object Idle : RegistrationState
    data object Loading : RegistrationState
    data class Success(val user: User) : RegistrationState
    data class Error(val message: String) : RegistrationState
}

/**
 * [RegistrationViewModel] for the [RegistrationScreen].
 *
 * Coordinates registration from the registration screen using the auth repository
 * for fetching an anonymous user id.
 *
 * Once a user id is obtained, user type is pushed to the user repository.
 */
@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    /**
     * Lets a user register as the selected UserType, and modify their user document
     * accordingly.
     *
     * @param userType [UserType] - Enum specifying the type of user, that the unauthenticated
     * user wants to register as.
     */
    fun register(userType: UserType) {
        if (_state.value == RegistrationState.Loading) return

        viewModelScope.launch {
            _state.value = RegistrationState.Loading

            try {
                val userId = authRepository.signIn()

                val existingUser = userRepository.getUser(userId)

                val user = existingUser ?: User(
                    userId = userId,
                    userType = userType,
                ).also {
                    userRepository.createUser(it)
                }

                _state.value = RegistrationState.Success(user)
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(
                    e.message ?: "Registration failed. Please try again."
                )
            }
        }
    }
}