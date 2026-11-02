package com.example.timewarp.feature.lottery.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

sealed interface CreateLotteryState {
    data object Loading : CreateLotteryState
    data class Content(
        val campsites: List<Campsite>,
        val currentUserProfile: Profile?,
        val currentUser: User?
    ) : CreateLotteryState
    data class Error(val message: String) : CreateLotteryState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<CreateLotteryState>(
        CreateLotteryState.Loading
    )
    val state: StateFlow<CreateLotteryState> = _state.asStateFlow()

    init {
        lotteryCreation()
    }

    private fun lotteryCreation() {
        viewModelScope.launch {
            _state.value = CreateLotteryState.Loading

            observeCurrentUser()
                .flatMapLatest { currentUser ->
                    if (currentUser == null) {
                        flowOf<CreateLotteryState>(
                            // User ID not found, set to Error
                            CreateLotteryState.Error("Unable to fetch user")
                        )
                    } else {
                        // Get filtered campsites and user's profile to set to content
                        val ownedCampsites = getUsersOwnedCampsites(currentUser.userId).orEmpty()
                        observeUserProfile(currentUser.userId).map { currentUserProfile ->
                            CreateLotteryState.Content(
                                campsites = ownedCampsites,
                                currentUser = currentUser,
                                currentUserProfile = currentUserProfile?.profile,
                            )
                        }
                    }
                }
                // Campsites not found, set to Error
                .catch { error ->
                    _state.value = CreateLotteryState.Error(
                        error.message ?: "Unable to fetch campsite lottery details"
                    )
                }
                .collect { _state.value = it }
        }
    }

    // Get all campsites, filter where owner ID is same as current user's ID
    private suspend fun getUsersOwnedCampsites(userId: String): List<Campsite>? {
        return campsiteRepository.getCampsites()?.filter { it.ownerUserId == userId }
    }
}