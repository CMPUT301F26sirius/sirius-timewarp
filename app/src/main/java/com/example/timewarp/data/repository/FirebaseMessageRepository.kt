package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [MessageRepository].
 *
 * Stores and retrieves messages from the Firestore `message` collection.
 *
 * @property firestore Firestore instance used to access messages.
 */
@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {
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
