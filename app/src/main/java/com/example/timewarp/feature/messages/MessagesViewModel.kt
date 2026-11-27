package com.example.timewarp.feature.messages

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface MessagesState {
    data object Idle : MessagesState
}

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<MessagesState>(MessagesState.Idle)
    val state: StateFlow<MessagesState> = _state.asStateFlow()
}
