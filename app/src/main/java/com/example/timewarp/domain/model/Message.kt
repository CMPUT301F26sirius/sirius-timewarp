package com.example.timewarp.domain.model

/**
 * [Message] data class
 *
 * Message exchanged between two users.
 *
 * @property message - Message text.
 * @property fromUserId - UUID of the sending user.
 * @property toUserId - UUID of the receiving user.
 */
data class Message (
    val message: String = "",
    val fromUserId: String = "",
    val toUserId: String = ""
)