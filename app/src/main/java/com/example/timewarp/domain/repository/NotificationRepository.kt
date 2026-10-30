package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Notification

interface NotificationRepository {
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Notification>?

    suspend fun getOutgoingNotifications(userId: String): MutableList<Notification>?

    suspend fun getIncomingNotifications(userId: String): MutableList<Notification>?

    suspend fun getNotifications(): MutableList<Notification>?

    suspend fun sendNotification(notification: Notification)

    suspend fun getNotification(notificationId: String): Notification?
}