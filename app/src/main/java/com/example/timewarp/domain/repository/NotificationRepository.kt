package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Notification

/**
 * [NotificationRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface NotificationRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Notification>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getIncomingNotifications(userId: String): MutableList<Notification>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getNotifications(): MutableList<Notification>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun sendNotification(notification: Notification)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getNotification(notificationId: String): Notification?
}