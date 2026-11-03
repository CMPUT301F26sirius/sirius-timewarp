package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [MessageRepository].
 *
 * Stores and retrieves messages from a `message` collection.
 */
@Singleton
class FakeMessageRepository @Inject constructor() : MessageRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCorrespondance(
        fromUserId: String,
        toUserId: String
    ): MutableList<Message>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getOutgoingMessages(userId: String): MutableList<Message>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getIncomingMessages(userId: String): MutableList<Message>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getMessages(): MutableList<Message>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun sendMessage(message: Message) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getMessage(messageId: String): Message? {
        TODO("Not yet implemented")
    }
}
