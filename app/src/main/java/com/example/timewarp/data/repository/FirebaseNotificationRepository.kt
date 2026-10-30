package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [NotificationRepository].
 *
 * Stores and retrieves notifications from Firestore.
 *
 * @property firestore Firestore instance used to access notifications.
 */
@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {
    override suspend fun getCorrespondance(
        fromUserId: String,
        toUserId: String
    ): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    override suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    override suspend fun getIncomingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    override suspend fun getNotifications(): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    override suspend fun sendNotification(notification: Notification) {
        TODO("Not yet implemented")
    }

    override suspend fun getNotification(notificationId: String): Notification? {
        TODO("Not yet implemented")
    }
}
