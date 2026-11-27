package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
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
