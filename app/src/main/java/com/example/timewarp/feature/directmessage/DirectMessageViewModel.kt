package com.example.timewarp.feature.directmessage

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface DirectMessageState {
    data object Idle : DirectMessageState
    data class Conversation(val targetUserId: String) : DirectMessageState
}

@HiltViewModel
class DirectMessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<DirectMessageState>(DirectMessageState.Idle)
    val state: StateFlow<DirectMessageState> = _state.asStateFlow()

    fun openConversation(targetUserId: String) {
        _state.value = DirectMessageState.Conversation(targetUserId)
    }
}
