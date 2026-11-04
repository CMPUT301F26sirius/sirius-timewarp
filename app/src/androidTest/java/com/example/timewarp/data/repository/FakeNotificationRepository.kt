package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [NotificationRepository].
 *
 * Stores notifications in memory instead of Firestore.
 **/
@Singleton
class FakeNotificationRepository @Inject constructor() : NotificationRepository {
    private val notifications = mutableListOf<Notification>()

    /**
     * Returns notifications sent from [fromUserId] to [toUserId].
     **/
    override suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Notification>? {
        val result = mutableListOf<Notification>()
        for (notification in notifications) {
            if (notification.fromUserId == fromUserId && notification.toUserId == toUserId) {
                result.add(notification)
            }
        }
        return result
    }

    /**
     * Returns notifications sent by [userId].
     **/
    override suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>? {
        val result = mutableListOf<Notification>()
        for (notification in notifications) {
            if (notification.fromUserId == userId) {
                result.add(notification)
            }
        }
        return result
    }

    /**
     * Returns notifications received by [userId].
     **/
    override suspend fun getIncomingNotifications(userId: String): MutableList<Notification>? {
        val result = mutableListOf<Notification>()
        for (notification in notifications) {
            if (notification.toUserId == userId) {
                result.add(notification)
            }
        }
        return result
    }


    /*
     * Returns every stored notification.
     **/
    override suspend fun getNotifications(): MutableList<Notification>? {
        return notifications.toMutableList()
    }

    /**
     * Stores a notification.
     *
     * @param notification notification to store.
     **/
    override suspend fun sendNotification(notification: Notification) {
        notifications.add(notification)
    }

    /**
     * Notifications have no ID field yet, so this always returns `null`.
     **/
    override suspend fun getNotification(notificationId: String): Notification? {
        return null
    }

    /**
     * Removes all notifications from the fake collection.
     **/
    fun clear() {
        notifications.clear()
    }
}