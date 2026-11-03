package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [NotificationRepository].
 *
 * Stores and retrieves notifications from a `notification` collection.
 */
@Singleton
class FakeNotificationRepository @Inject constructor() : NotificationRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCorrespondance(
        fromUserId: String,
        toUserId: String
    ): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getIncomingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getNotifications(): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun sendNotification(notification: Notification) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getNotification(notificationId: String): Notification? {
        TODO("Not yet implemented")
    }
}
