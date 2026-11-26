package com.example.timewarp.feature.alerts.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.domain.usecase.DeleteUserProfileUseCase
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProfile(
    val userId: String,
    val name: String,
    val email: String,
    val userType: UserType,
    val profile: Profile?
)

data class AdminProfilesState(
    val items: List<AdminProfile> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isAdmin: Boolean = false,
    val currentUserId: String? = null
)

// This ViewModel will load every user profile for the admin Profiles tab
@HiltViewModel
class AdminProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val deleteUserProfileUseCase: DeleteUserProfileUseCase
) : ViewModel() {

    // State
    private val _state = MutableStateFlow(AdminProfilesState())
    val state: StateFlow<AdminProfilesState> = _state.asStateFlow()

    init {
        loadProfiles()
    }

    // This function will load all the users and their profiles
    fun loadProfiles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val currentUser = observeCurrentUserUseCase().first()
                val profileById = profileRepository.getProfiles()
                    .orEmpty()
                    .associateBy { it.userId }
                val items = userRepository.getUsers().orEmpty()
                    .map {
                        user -> val profile = profileById[user.userId]
                        AdminProfile(
                            userId = user.userId,
                            name = profile?.let {
                                "${it.firstName} ${it.lastName}".trim()
                            }.orEmpty(),
                            email = profile?.email.orEmpty(),
                            userType = user.userType,
                            profile = profile
                        )
                    }

                _state.update {
                    it.copy(
                        items = items,
                        isLoading = false,
                        currentUserId = currentUser?.userId,
                        isAdmin = currentUser?.userType == UserType.Admin
                    )
                }
            } catch (
                e: Exception
            ) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Could Not Load Any Profiles"
                    )
                }
            }
        }
    }

    fun onDeleteProfile(userId: String){
        val current = state.value
        if(!current.isAdmin || userId == current.currentUserId)
            return
        viewModelScope.launch {
            try {
                deleteUserProfileUseCase(userId)
                loadProfiles()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Could Not Remove Profile") }
            }
        }
    }
}