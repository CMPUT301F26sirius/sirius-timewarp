package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Message

/**
 * Repository for storing and retrieving [Message] objects.
 */
interface MessageRepository {
    /**
     * @param fromUserId identifier of the sender.
     * @param toUserId identifier of the recipient.
     * @return messages exchanged by the users, or `null` when no collection result is available.
     */
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Message>?

    /**
     * @param userId identifier of the sender.
     * @return messages sent by the user, or `null` when no collection result is available.
     */
    suspend fun getOutgoingMessages(userId: String): MutableList<Message>?

    /**
     * @param userId identifier of the recipient.
     * @return messages received by the user, or `null` when no collection result is available.
     */
    suspend fun getIncomingMessages(userId: String): MutableList<Message>?

    /**
     * @return all messages, or `null` when no collection result is available.
     */
    suspend fun getMessages(): MutableList<Message>?

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