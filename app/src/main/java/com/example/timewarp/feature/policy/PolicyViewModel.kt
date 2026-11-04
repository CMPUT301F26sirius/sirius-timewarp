package com.example.timewarp.feature.policy

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PolicyState {
    data object Idle : PolicyState
}

@HiltViewModel
class PolicyViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<PolicyState>(PolicyState.Idle)
    val state: StateFlow<PolicyState> = _state.asStateFlow()
}