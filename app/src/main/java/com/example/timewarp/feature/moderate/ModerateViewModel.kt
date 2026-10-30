package com.example.timewarp.feature.moderate

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ModerateState {
    data object Idle : ModerateState
}

@HiltViewModel
class ModerateViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ModerateState>(ModerateState.Idle)
    val state: StateFlow<ModerateState> = _state.asStateFlow()


}