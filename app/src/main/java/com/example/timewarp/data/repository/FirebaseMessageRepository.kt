package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
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
    override suspend fun observeCorrespondence(fromUserId: String, toUserId: String): Flow<List<Message>?> {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeSummaries(fromUserId: String): Flow<List<Pair<Profile, Message>>?> {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeOutgoingMessages(fromUserId: String): Flow<List<Message>?> {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun observeIncomingMessages(toUserId: String): Flow<List<Message>?> {
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
    override suspend fun getMessage(messageId: String): Message {
        TODO("Not yet implemented")
    }
}
