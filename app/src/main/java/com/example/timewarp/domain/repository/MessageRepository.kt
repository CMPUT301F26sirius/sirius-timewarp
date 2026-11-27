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
     * @param fromUserId identifier of the sender.
     * @return A pair with the profile of the toUserId, and latest message between you and them
     * This is so we can display a list of people you are messaging, along with the last message
     * e.g. their_pfp | you: message or their_pfp | them: message
     */
    suspend fun observeSummaries(fromUserId: String): Flow<List<Pair<Profile, Message>>?>

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