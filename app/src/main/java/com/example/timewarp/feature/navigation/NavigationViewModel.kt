package com.example.timewarp.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface NavigationState {
    data object Idle : NavigationState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val state: StateFlow<NavigationState> = _state.asStateFlow()

    val currentUser: Flow<User?> = authRepository.currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                userRepository.observeUser(userId)
            }
        }

    val navigationItems: StateFlow<List<Screen>> = currentUser
        .map { user ->
            if (user == null) {
                emptyList<Screen>()
            } else {
                when (user.userType) {
                    UserType.Entrant -> listOf(
                        Screen.Browse,
                        Screen.Lotteries,
                        Screen.Reservations,
                        Screen.Alerts,
                        Screen.Account,
                    )

                    UserType.CampsiteOwner -> listOf(
                        Screen.Browse,
                        Screen.Lotteries,
                        Screen.Reservations,
                        Screen.Alerts,
                        Screen.Account,
                    )

                    UserType.Admin -> listOf(
                        Screen.Browse,
                        Screen.Lotteries,
                        Screen.Reservations,
                        Screen.Alerts,
                        Screen.Account,
                        Screen.Moderate,
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
}