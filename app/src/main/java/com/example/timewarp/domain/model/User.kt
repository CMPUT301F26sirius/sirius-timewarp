package com.example.timewarp.domain.model

/**
 * [User] data class
 *
 * Application user account.
 *
 * @property userId - Unique identifier for the user.
 * @property userType - Role that determines the user's permissions and screens.
 * @property notificationsEnabled - Whether notifications are enabled for the user.
 */
data class User (
    val userId: String = "",
    val userType: UserType = UserType.Entrant,
    val notificationsEnabled: Boolean = true,
)