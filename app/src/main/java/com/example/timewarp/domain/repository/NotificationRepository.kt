package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Notification

/**
 * Repository for storing and retrieving [Notification] objects.
 */
interface NotificationRepository {
    /**
     * @param fromUserId identifier of the sender.
     * @param toUserId identifier of the recipient.
     * @return notifications exchanged by the users, or `null` when no collection result is available.
     */
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Notification>?

    /**
     * @param userId identifier of the sender.
     * @return notifications sent by the user, or `null` when no collection result is available.
     */
    suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>?

    /**
     * @param userId identifier of the recipient.
     * @return notifications received by the user, or `null` when no collection result is available.
     */
    suspend fun getIncomingNotifications(userId: String): MutableList<Notification>?

    /**
     * @return all notifications, or `null` when no collection result is available.
     */
    suspend fun getNotifications(): MutableList<Notification>?

    /**
     * @param notification notification to send.
     */
    suspend fun sendNotification(notification: Notification)

    /**
     * @param notificationId identifier of the notification to retrieve.
     * @return the matching notification, or `null` when it does not exist.
     */
    suspend fun getNotification(notificationId: String): Notification?
}