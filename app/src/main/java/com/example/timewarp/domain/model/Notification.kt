package com.example.timewarp.domain.model

/**
 * [Notification] data class
 *
 * Notification delivered from one user to another.
 *
 * @property timestamp - Time that the notification was sent
 * @property notificationId - Unique identifier for the notification.
 * @property notification - Notification message text.
 * @property fromUserId - UUID of the sending user.
 * @property toUserId - UUID of the receiving user.
 */
class Notification (
    val timestamp: Int = 0,
    val notificationId: String = "",
    val notification: String = "",
    val fromUserId: String = "",
    val toUserId: String = ""
)