package com.example.timewarp.feature.directmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.ImageRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.feature.registration.RegistrationState
import com.example.timewarp.feature.registration.RegistrationViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [DirectMessageState] State of the ui
 */
sealed interface DirectMessageState {
    data object Loading : DirectMessageState
    data class Content(
        val currentUserId: String,
        val targetUserId: String,
        val profile: Profile?,
        val profileImage: Image?,
        val messages: List<Message>,
        val draftMessage: String = "",
        val isSending: Boolean = false,
        val sendError: String? = null,
    ) : DirectMessageState
    data class Error(val message: String) : DirectMessageState
}

/**
 * [DirectMessageViewModel] for the [DirectMessageScreen].
 *
 * Displays a list of messages sent between the currently authorized user, and the
 * target user.
 */
@HiltViewModel
class DirectMessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val imageRepository: ImageRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<DirectMessageState>(DirectMessageState.Loading)
    val state: StateFlow<DirectMessageState> = _state.asStateFlow()

    private var activeTargetUserId: String? = null
    private var conversationJob: Job? = null

    /**
     * Sets the UI state by attempting to open a conversation with the target user
     *
     * @param targetUserId - ID of the target user we want to message
     */
    fun openConversation(targetUserId: String) {
        // Error if no target
        if (targetUserId.isBlank()) {
            activeTargetUserId = null
            conversationJob?.cancel()
            _state.value = DirectMessageState.Error("A user is required to open this conversation.")
            return
        }
        if (activeTargetUserId == targetUserId && conversationJob?.isActive == true) return

        // Set active target and stop current job
        activeTargetUserId = targetUserId
        conversationJob?.cancel()
        _state.value = DirectMessageState.Loading

        // Launch new job
        conversationJob = viewModelScope.launch {
            try {
                // Get the required info for the coversation
                val currentUser = observeCurrentUser().first()
                    ?: throw IllegalStateException("Sign in to view direct messages.")
                val profile = observeUserProfile(targetUserId).first()?.profile
                val image = profile?.imageId
                    ?.takeIf { it.isNotBlank() }
                    ?.let { imageRepository.getImage(it) }
                val conversation = messageRepository.observeCorrespondence(
                    fromUserId = currentUser.userId,
                    toUserId = targetUserId,
                )

                // Collect info into state
                conversation.collect { messages ->
                    val loadedMessages = messages
                        ?: throw IllegalStateException("Unable to load this conversation.")
                    val previous = _state.value as? DirectMessageState.Content
                    _state.value = DirectMessageState.Content(
                        currentUserId = currentUser.userId,
                        targetUserId = targetUserId,
                        profile = profile,
                        profileImage = image,
                        messages = loadedMessages.sortedBy { it.timestamp },
                        draftMessage = previous
                            ?.takeIf { it.targetUserId == targetUserId }
                            ?.draftMessage
                            .orEmpty(),
                        isSending = previous
                            ?.takeIf { it.targetUserId == targetUserId }
                            ?.isSending ?: false,
                        sendError = previous
                            ?.takeIf { it.targetUserId == targetUserId }
                            ?.sendError,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = DirectMessageState.Error(
                    error.message ?: "Unable to load this conversation."
                )
            }
        }
    }

    // Listen for changes to the typed message and update state
    fun onDraftMessageChange(value: String) {
        _state.update { current ->
            (current as? DirectMessageState.Content)
                ?.takeUnless { it.isSending }
                ?.copy(draftMessage = value, sendError = null)
                ?: current
        }
    }

    // Send message via repository on click send message
    fun sendMessage() {
        val current = _state.value as? DirectMessageState.Content ?: return
        val text = current.draftMessage.trim()
        if (text.isEmpty() || current.isSending) return

        _state.value = current.copy(isSending = true, sendError = null)
        viewModelScope.launch {
            try {
                messageRepository.sendMessage(
                    Message(
                        timestamp = (System.currentTimeMillis() / 1_000L).toInt(),
                        message = text,
                        fromUserId = current.currentUserId,
                        toUserId = current.targetUserId,
                    )
                )
                updateConversation(current) {
                    copy(draftMessage = "", isSending = false, sendError = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                updateConversation(current) {
                    copy(isSending = false, sendError = error.message ?: "Unable to send this message.")
                }
            }
        }
    }

    // Update the state of the conversation
    private fun updateConversation(
        original: DirectMessageState.Content,
        transform: DirectMessageState.Content.() -> DirectMessageState.Content,
    ) {
        _state.update { current ->
            if (
                current is DirectMessageState.Content &&
                current.currentUserId == original.currentUserId &&
                current.targetUserId == original.targetUserId
            ) {
                current.transform()
            } else {
                current
            }
        }
    }
}
