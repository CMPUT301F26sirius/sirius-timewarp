package com.example.timewarp.feature.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// TEMP: true to test the UI without Firebase. Set to false before committing.
private const val USE_FAKE_DATA = true

private val fakeProfile = Profile(
    userId = "fake-user",
    firstName = "John",
    lastName = "Doe",
    email = "johndoe@gmail.com",
    phoneNumber = "5870000000"
)

sealed interface UpdateProfileState {
    data object Loading: UpdateProfileState
    data class Content (
        val firstName: String,
        val lastName: String,
        val email: String,
        val phoneNumber: String,
        val isSaving: Boolean = false,
        val saveError: String? = null,
        val isSaved: Boolean = false
    ) : UpdateProfileState {
        val canSave: Boolean
            get() = firstName.isNotBlank() && lastName.isNotBlank() &&
                    email.isNotBlank() && !isSaving
    }
    data class Error (val message: String) : UpdateProfileState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Loading)
    val state: StateFlow<UpdateProfileState> = _state.asStateFlow()

    // Profile as loaded, so saving keeps fields the form doesn't edit
    private var originalProfile: Profile? = null

    init {
        load()
    }

    // Loads the signed-in user's profile once. Not observed, so live
    // updates don't overwrite what the user is typing.
    fun load() {
        viewModelScope.launch {
            _state.value = UpdateProfileState.Loading

            if (USE_FAKE_DATA) {
                delay(500)  // So the loading state is visible
                showProfile(fakeProfile)
                return@launch
            }

            try {
                val userProfile = observeCurrentUser()
                    .flatMapLatest { user ->
                        if (user == null) flowOf(null) else observeUserProfile(user.userId)
                    }
                    .first()

                val profile = userProfile?.profile
                if (profile == null) {
                    _state.value = UpdateProfileState.Error("Profile not found.")
                    return@launch
                }

                originalProfile = profile
                _state.value = UpdateProfileState.Content(
                    firstName = profile.firstName,
                    lastName = profile.lastName,
                    email = profile.email,
                    phoneNumber = profile.phoneNumber
                )
            } catch (error: Exception) {
                _state.value = UpdateProfileState.Error(
                    error.message ?: "Unable to load profile."
                )
            }
        }
    }

    private fun showProfile(profile: Profile) {
        originalProfile = profile
        _state.value = UpdateProfileState.Content(
            firstName = profile.firstName,
            lastName = profile.lastName,
            email = profile.email,
            phoneNumber = profile.phoneNumber
        )
    }

    // Form events
    fun onFirstNameChange(value: String) = updateContent { copy(firstName = value) }
    fun onLastNameChange(value: String) = updateContent { copy(lastName = value) }
    fun onEmailChange(value: String) = updateContent { copy(email = value) }
    fun onPhoneNumberChange(value: String) = updateContent { copy(phoneNumber = value) }

    fun save() {
        val current = _state.value as? UpdateProfileState.Content ?: return
        val profile = originalProfile ?: return
        if (!current.canSave) return

        updateContent { copy(isSaving = true, saveError = null) }

        viewModelScope.launch {

            try {
                val updated = profile.copy(
                    firstName = current.firstName.trim(),
                    lastName = current.lastName.trim(),
                    email = current.email.trim(),
                    phoneNumber = current.phoneNumber.trim()
                )

                if (USE_FAKE_DATA) {
                    delay(1000)  // Pretend to save, so "Saving..." is visible
                    Log.d("UpdateProfile", "Fake save: $updated")
                } else {
                    profileRepository.editProfile(userId = profile.userId, profile = updated)
                }

                updateContent { copy(isSaving = false, isSaved = true) }
            } catch (error: Exception) {
                updateContent {
                    copy(isSaving = false, saveError = error.message ?: "Unable to save profile.")
                }
            }

          /*  try {
                profileRepository.editProfile(
                    userId = profile.userId,
                    profile = profile.copy(
                        firstName = current.firstName.trim(),
                        lastName = current.lastName.trim(),
                        email = current.email.trim(),
                        phoneNumber = current.phoneNumber.trim()
                    )
                )
                updateContent { copy(isSaving = false, isSaved = true) }
            } catch (error: Exception) {
                updateContent {
                    copy(isSaving = false, saveError = error.message ?: "Unable to save profile.")
                }
            }

           */
        }
    }

    // Only modifies state while the form is showing
    private fun updateContent(transform: UpdateProfileState.Content.() -> UpdateProfileState.Content) {
        _state.update { (it as? UpdateProfileState.Content)?.transform() ?: it }
    }
}