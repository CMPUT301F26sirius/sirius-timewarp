package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [NotificationRepository].
 *
 * Stores and retrieves notifications from the Firestore `notification` collection.
 *
 * @property firestore Firestore instance used to access notifications.
 **/
@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {
    /**
     * NEEDS_DOCUMENTATION
     **/
    override suspend fun getCorrespondance(
        fromUserId: String,
        toUserId: String
    ): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     **/
    override suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     **/
    override suspend fun getIncomingNotifications(userId: String): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     **/
    override suspend fun getNotifications(): MutableList<Notification>? {
        TODO("Not yet implemented")
    }

    /**
     * Saves a notification to the Firestore `notification` collection,
     * using its notificationId as the document name.
     *
     * @param notification notification to save.
     **/
    override suspend fun sendNotification(notification: Notification) {
        if (notification.notificationId.isEmpty()) {
            firestore.collection("notification")
                .add(notification)
                .await()
            return
        }

        firestore.collection("notification")
            .document(notification.notificationId)
            .set(notification)
            .await()
    }

    /**
     * NEEDS_DOCUMENTATION
     **/
    override suspend fun getNotification(notificationId: String): Notification? {
        TODO("Not yet implemented")
    }
}
