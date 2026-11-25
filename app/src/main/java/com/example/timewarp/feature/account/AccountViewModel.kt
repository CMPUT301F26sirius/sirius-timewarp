package com.example.timewarp.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.navigation.Screen
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
    data object Deleted: AccountState
    data class Error(val message: String) : AccountState
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reservationRepository: ReservationRepository,
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountState>(AccountState.Idle)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    val currentUser = observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isUpdatingNotifications = MutableStateFlow(false)

    val isUpdatingNotifications: StateFlow<Boolean> = _isUpdatingNotifications.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)

    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

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


    fun deleteAccount() {
        val user = currentUser.value
        if (user == null) {
            _state.value = AccountState.Error("Unable to find a signed-in user.")
            return
        }

        if (_isDeleting.value) {      // Prevents double taps
            return
        }

        viewModelScope.launch {
            _isDeleting.value = true

            try {
                val reservations = reservationRepository.getReservations().orEmpty()
                for (reservation in reservations){
                    val isActive = reservation.reservationState == ReservationState.Waitlisted ||
                            reservation.reservationState == ReservationState.Pending ||
                            reservation.reservationState == ReservationState.Reserved

                    if (reservation.userId == user.userId && isActive){
                        reservationRepository.deleteReservation(reservation.reservationId)
                    }
                }

                val oldProfile = profileRepository.getProfile(user.userId)
                profileRepository.editProfile(
                    user.userId,
                    Profile(userId = user.userId, firstName = "No user found")
                )

                try {
                    authRepository.deleteAccount()
                } catch (e: Exception) {
                    if (oldProfile != null) {
                        profileRepository.editProfile(user.userId, oldProfile)
                    }
                    throw e
                }

                _state.value = AccountState.Deleted

            } catch (e: Exception) {
                _state.value = AccountState.Error("Uncable to delete your account. Please try again.")
            } finally {
                _isDeleting.value = false
            }

        }

    }

}