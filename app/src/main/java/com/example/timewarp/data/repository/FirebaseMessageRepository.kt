package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.filter
import kotlin.text.groupBy
import kotlin.text.orEmpty

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
    private val messages = firestore.collection("message")

    /**
     * @param fromUserId identifier of one recipient
     * @param toUserId identifier of another recipient.
     * @return Observes messages between the two users in the `message` Firestore collection
     *
     * Both ways
     */
    override suspend fun observeCorrespondence(fromUserId: String, toUserId: String): Flow<List<Message>?> {
        return combine(
            observeCorrespondenceDirection(fromUserId, toUserId),
            observeCorrespondenceDirection(toUserId, fromUserId),
        ) { sentBySender, sentByTarget ->
            (sentBySender.orEmpty() + sentByTarget.orEmpty())
                .sortedBy { it.timestamp }
        }
    }

    /**
     * @param userId identifier of the sender.
     * @return Flow. Observes the latest message from each conversation partner, newest first.
     * from the `message` Firestore collection
     */
    override suspend fun observeLatestMessages(userId: String): Flow<List<Message>?> {
        return combine(
            observeOutgoingMessages(userId),
            observeIncomingMessages(userId),
        ) { outgoing, incoming ->
            (outgoing.orEmpty() + incoming.orEmpty())
                .filter { it.fromUserId == userId || it.toUserId == userId }
                .groupBy { message ->
                    if (message.fromUserId == userId) message.toUserId else message.fromUserId
                }
                .filterKeys { it.isNotBlank() && it != userId }
                .values
                .mapNotNull { messagesForUser ->
                    messagesForUser.maxByOrNull { it.timestamp }
                }
                .sortedByDescending { it.timestamp }
        }
    }

    /**
     * @param fromUserId identifier of the sender.
     * @return messages sent by the user, or `null` when no collection result is available.
     * from the `message` Firestore collection
     */
    override suspend fun observeOutgoingMessages(fromUserId: String): Flow<List<Message>?> {
        return observeMessagesFor("fromUserId", fromUserId)
    }

    /**
     * @param toUserId identifier of the recipient.
     * @return messages received by the user, or `null` when no collection result is available.
     * from the `message` Firestore collection
     */
    override suspend fun observeIncomingMessages(toUserId: String): Flow<List<Message>?> {
        return observeMessagesFor("toUserId", toUserId)
    }

    /**
     * @param message message to send.
     *
     * Saves [message] as a new Firestore document.
     */
    override suspend fun sendMessage(message: Message) {
        messages.add(message).await()
    }

    /**
     * @param messageId identifier of the message to retrieve.
     * @return the matching message, or `null` when it does not exist.
     *
     * Retrieves the message document from Firestore with [messageId], if it exists.
     */
    override suspend fun getMessage(messageId: String): Message? {
        val snapshot = messages.document(messageId).get().await()
        return snapshot.takeIf { it.exists() }?.toObject(Message::class.java)
    }

    /**
     * @param field the field (fromUserId or toUserId to match)
     * @param userId to match against
     * @return flow of messages that match query in firebase
     *
     * Gets a flow for the matching userId
     */
    private fun observeMessagesFor(field: String, userId: String): Flow<List<Message>?> {
        return observeMessages(messages.whereEqualTo(field, userId))
    }

    /**
     * @param fromUserId identifier of fromUserId
     * @param toUserId identifier of toUserId
     * @return Observes messages between the two users in the `message` Firestore collection
     *
     * One way, exact
     */
    private fun observeCorrespondenceDirection(fromUserId: String, toUserId: String): Flow<List<Message>?> {
        return observeMessages(
            messages
                .whereEqualTo("fromUserId", fromUserId)
                .whereEqualTo("toUserId", toUserId)
        )
    }

    /**
     * @param query to perform on messages
     * @return A callback flow for the query
     *
     * Reusable for different queries
     */
    private fun observeMessages(query: Query): Flow<List<Message>?> = callbackFlow {
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val items = snapshot?.documents
                ?.mapNotNull { it.toObject(Message::class.java) }
                .orEmpty()
                .sortedBy { it.timestamp }
            trySend(items)
        }

        awaitClose { listener.remove() }
    }
}
