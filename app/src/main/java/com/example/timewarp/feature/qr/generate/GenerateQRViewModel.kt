package com.example.timewarp.feature.qr.generate

import androidx.lifecycle.ViewModel
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.feature.registration.RegistrationState
import com.example.timewarp.feature.registration.RegistrationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [GenerateQRState] State of the ui
 */
sealed interface GenerateQRState {
    data object Idle : GenerateQRState
}

/**
 * [GenerateQRViewModel] for the [GenerateQRScreen].
 *
 * Displays a QR code, link, copy and share options for the passed url.
 */
@HiltViewModel
class GenerateQRViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<GenerateQRState>(GenerateQRState.Idle)
    val state: StateFlow<GenerateQRState> = _state.asStateFlow()
}