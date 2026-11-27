package com.example.timewarp.feature.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ImageRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.directmessage.DirectMessageScreen
import com.example.timewarp.feature.directmessage.DirectMessageState
import com.example.timewarp.feature.directmessage.DirectMessageViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * [MessageConversation] To store the parsed data
 */
data class MessageConversation(
    val targetUserId: String,
    val profile: Profile?,
    val profileImage: Image?,
    val latestMessage: Message,
)

/**
 * [MessagesState] State of the ui
 */
sealed interface MessagesState {
    data object Loading : MessagesState
    data class Content(
        val currentUserId: String,
        val conversations: List<MessageConversation>,
    ) : MessagesState
    data class Error(val message: String) : MessagesState
}

/**
 * [MessagesViewModel] for the [MessagesScreen].
 *
 * Lists direct message cards for users you have previous conversations with, listed
 * by most recent conversation.
 *
 * When a card is clicked, it takes you to the direct message screen.
 */
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val imageRepository: ImageRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeUserProfile: ObserveUserProfileUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<MessagesState>(MessagesState.Loading)
    val state: StateFlow<MessagesState> = _state.asStateFlow()

    private var observationJob: Job? = null
    private val infoCache = mutableMapOf<String, Pair<Profile?, Image?>>()

    init {
        load()
    }

    /**
     * On initialization, starts an observation job for trying to
     * observe the current user, in order to observe the latest message exchanges.
     */
    fun load() {
        observationJob?.cancel()
        _state.value = MessagesState.Loading
        observationJob = viewModelScope.launch {
            var observedUserId: String? = null
            try {
                observeCurrentUser().collectLatest { user ->
                    if (user == null) {
                        _state.value = MessagesState.Error("Sign in to view your messages.")
                        return@collectLatest
                    }

                    // Clear cache if current user changed
                    if (observedUserId != user.userId) {
                        infoCache.clear()
                        observedUserId = user.userId
                    }

                    // Observe latest messages
                    messageRepository.observeLatestMessages(user.userId).collectLatest { messages ->
                        val latestMessages = messages
                            ?: throw IllegalStateException("Unable to load your conversations.")

                        // Get conversations
                        val conversations = mutableListOf<MessageConversation>()
                        for (message in latestMessages) {
                            val targetUserId = if (message.fromUserId == user.userId) { message.toUserId  } else { message.fromUserId }

                            // Get profile and image
                            val (profile, image) = infoCache[targetUserId] ?: run {
                                val targetProfile = observeUserProfile(targetUserId).first()?.profile
                                val targetImage = targetProfile?.imageId
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { imageRepository.getImage(it) }
                                (targetProfile to targetImage).also {
                                    infoCache[targetUserId] = it
                                }
                            }

                            // Add conversation
                            conversations.add(
                                MessageConversation(
                                    targetUserId = targetUserId,
                                    profile = profile,
                                    profileImage = image,
                                    latestMessage = message,
                                )
                            )
                        }

                        // Set state
                        _state.value = MessagesState.Content(
                            currentUserId = user.userId,
                            conversations = conversations,
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.value = MessagesState.Error(
                    error.message ?: "Unable to load your messages."
                )
            }
        }
    }
}
