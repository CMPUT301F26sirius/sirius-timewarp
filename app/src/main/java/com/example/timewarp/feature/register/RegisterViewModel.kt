package com.example.timewarp.feature.example

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RegisterState(
    val example: String = "",
)

@HiltViewModel
class RegisterViewModel @Inject constructor () : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState = _uiState.asStateFlow()
}