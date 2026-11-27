package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [MessageRepository].
 *
 * Stores and retrieves messages from a `messages` collection, in memory.
 */
@Singleton
class FakeMessageRepository @Inject constructor() : MessageRepository {
    private val messages = MutableStateFlow<Map<String, Message>>(emptyMap())
    private var nextMessageId = 0

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeCorrespondence(fromUserId: String, toUserId: String): Flow<List<Message>?> = messages.map { stored ->
        stored.values
            .filter { message ->
                (message.fromUserId == fromUserId && message.toUserId == toUserId) ||
                    (message.fromUserId == toUserId && message.toUserId == fromUserId)
            }
            .sortedBy { it.timestamp }
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeLatestMessages(userId: String): Flow<List<Message>?> =
        messages.map { stored ->
            stored.values
                .filter { it.fromUserId == userId || it.toUserId == userId }
                .groupBy { message ->
                    if (message.fromUserId == userId) message.toUserId else message.fromUserId
                }
                .filterKeys { it.isNotBlank() && it != userId }
                .values
                .mapNotNull { it.maxByOrNull(Message::timestamp) }
                .sortedByDescending(Message::timestamp)
        }

    /**
     * Observes every stored message, ordered by timestamp.
     */
    override suspend fun observeAllMessages(): Flow<List<Message>?> =
        messages.map { stored ->
            stored.values.sortedBy(Message::timestamp)
        }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeOutgoingMessages(fromUserId: String): Flow<List<Message>?> =
        messages.map { stored ->
            stored.values
                .filter { it.fromUserId == fromUserId }
                .sortedBy(Message::timestamp)
        }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeIncomingMessages(toUserId: String): Flow<List<Message>?> =
        messages.map { stored ->
            stored.values
                .filter { it.toUserId == toUserId }
                .sortedBy(Message::timestamp)
        }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun sendMessage(message: Message) {
        addMessage(message)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getMessage(messageId: String): Message? = messages.value[messageId]

    /**
     * NEEDS_DOCUMENTATION
     */
    fun addMessage(message: Message) {
        val messageId = "fake-message-${nextMessageId++}"
        messages.value = messages.value + (messageId to message)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    fun clear() {
        messages.value = emptyMap()
        nextMessageId = 0
    }
}
