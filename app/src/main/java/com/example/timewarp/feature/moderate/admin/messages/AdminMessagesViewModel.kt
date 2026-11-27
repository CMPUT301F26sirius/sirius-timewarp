package com.example.timewarp.feature.moderate.admin.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Message details displayed in the admin message log.
 */
data class AdminMessageItem(
    val message: String,
    val sender: String,
    val recipient: String
)

/**
 * [AdminMessagesState] State of the ui
 */
data class AdminMessagesState(
    val items: List<AdminMessageItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Loads all messages and resolves their sender and recipient labels.
 *
 * Message changes are observed through the repository's all-messages flow.
 *
 * @property messageRepository observes all messages.
 * @property profileRepository retrieves profile names for message participants.
 */
@HiltViewModel
class AdminMessagesViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AdminMessagesState())
    val state: StateFlow<AdminMessagesState> = _state.asStateFlow()
    private var observationJob: Job? = null

    init {
        loadMessages()
    }

    /**
     * Starts observing all messages.
     *
     * Any previous observation is canceled before the new one starts.
     */
    fun loadMessages() {
        observationJob?.cancel()
        observationJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val profilesById = profileRepository.getProfiles()
                    .orEmpty()
                    .associateBy { it.userId }

                val allMessagesFlow = messageRepository.observeAllMessages()

                fun userLabel(userId: String): String =
                    profilesById[userId]
                        ?.let { "${it.firstName} ${it.lastName}".trim() }
                        ?.takeIf { it.isNotBlank() }
                        ?: userId.takeIf { it.isNotBlank() }
                        ?: "Unknown user"

                allMessagesFlow.collect { messages ->
                    val allMessages = messages
                        ?: throw IllegalStateException("Unable to load messages.")
                    val items = allMessages
                        .sortedByDescending(Message::timestamp)
                        .map { message ->
                            AdminMessageItem(
                                message = message.message,
                                sender = userLabel(message.fromUserId),
                                recipient = userLabel(message.toUserId)
                            )
                        }
                    _state.update { it.copy(items = items, isLoading = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Could Not Load Messages"
                    )
                }
            }
        }
    }
}
