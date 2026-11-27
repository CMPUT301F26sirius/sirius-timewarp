package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and retrieving [Message] objects.
 */
interface MessageRepository {
    /**
     * @param fromUserId identifier of the sender.
     * @param toUserId identifier of the recipient.
     * @return messages exchanged by the users, or `null` when no collection result is available.
     */
    suspend fun observeCorrespondence(fromUserId: String, toUserId: String): Flow<List<Message>?>

    /**
     * @param userId identifier of the sender.
     * @return A list of the latest messages where userId is the recipient or sender.
     * with another unique user. i.e. all other users should only show up once.
     */
    suspend fun observeLatestMessages(userId: String): Flow<List<Message>?>

    /**
     * @return a flow emitting all messages, possibly `null` when no collection result is available.
     */
    suspend fun observeAllMessages(): Flow<List<Message>?>

    /**
     * @param fromUserId identifier of the sender.
     * @return messages sent by the user, or `null` when no collection result is available.
     */
    suspend fun observeOutgoingMessages(fromUserId: String): Flow<List<Message>?>

    /**
     * @param toUserId identifier of the recipient.
     * @return messages received by the user, or `null` when no collection result is available.
     */
    suspend fun observeIncomingMessages(toUserId: String): Flow<List<Message>?>

    /**
     * @param message message to send.
     */
    suspend fun sendMessage(message: Message)

    /**
     * @param messageId identifier of the message to retrieve.
     * @return the matching message, or `null` when it does not exist.
     */
    suspend fun getMessage(messageId: String): Message?
}