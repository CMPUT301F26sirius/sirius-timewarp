package com.example.timewarp.feature.profile.update

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

private val TEST_USER_ID: String? = null // set to "test-user" during testing
/*
Main Responsibility: Every state the update profile screen can be in
 */
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
/*
Main responsibility: View model for the update profile screen. Load the signed-in users profile,
                     into an editable form. Tracks changes, validates changes, and saves results

Parameters: profileRepository: Saves the edited profile
            observeCurrentUser: Provides the signed=in user, or null if not signed in
            observeUserProfile: Provides the profile for given UserID
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase
) : ViewModel() {
    // Current Screen state
    private val _state = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Loading)
    val state: StateFlow<UpdateProfileState> = _state.asStateFlow()

    // Profile as loaded, so saving keeps fields the form doesn't edit
    private var originalProfile: Profile? = null

    init {
        load()
    }


    /*
    Main Responsibility: Reads the profile. Sets state when loaded, or when no user is signed in,
                         or loading fails. If the user has no profile, the form starts blank and
                         saving creates one.
     */
    fun load() {
        viewModelScope.launch {
            _state.value = UpdateProfileState.Loading

            try {
                val userId = TEST_USER_ID ?: observeCurrentUser().first()?.userId
                if (userId == null) {
                    _state.value = UpdateProfileState.Error("You need to be signed in to update your profile.")
                    return@launch
                }

                val profile = if (TEST_USER_ID != null) {
                    profileRepository.getProfile(userId)
                } else {
                    observeUserProfile(userId).first()?.profile
                }

                // No profile yet: start blank, and saving creates it
                showProfile(profile ?: Profile(userId = userId))
            } catch (error: Exception) {
                _state.value = UpdateProfileState.Error(
                    error.message ?: "Unable to load profile."
                )
            }
        }
    }
    /*
    Main Responsibility: Fills form with loaded profile

    Parameters: profile: Profile, whose values are the form's starting values.
     */
    private fun showProfile(profile: Profile) {
        originalProfile = profile
        _state.value = UpdateProfileState.Content(
            firstName = profile.firstName,
            lastName = profile.lastName,
            email = profile.email,
            phoneNumber = profile.phoneNumber
        )
    }

    // Form events which update each field
    fun onFirstNameChange(value: String) = updateContent { copy(firstName = value) }
    fun onLastNameChange(value: String) = updateContent { copy(lastName = value) }
    fun onEmailChange(value: String) = updateContent { copy(email = value) }
    fun onPhoneNumberChange(value: String) = updateContent { copy(phoneNumber = value) }

    /*
    Main Responsibility: Trim each field (in case of whitespaces), copies values into profile.
                         While saving sends message that it's saving. Sets profile when saving
                         succeeds. On failure sends error so user can try again.
     */
    fun save() {
        val current = _state.value as? UpdateProfileState.Content ?: return
        val profile = originalProfile ?: return
        if (!current.canSave) return

        updateContent { copy(isSaving = true, saveError = null) }

        viewModelScope.launch {

          try {
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

        }
    }

    // Only modifies state while the form is showing
    private fun updateContent(transform: UpdateProfileState.Content.() -> UpdateProfileState.Content) {
        _state.update { (it as? UpdateProfileState.Content)?.transform() ?: it }
    }
}