package com.example.timewarp.domain.model

/**
 * [User] data class
 *
 * Application user account.
 *
 * @property userId - Unique identifier for the user.
 * @property userType - Role that determines the user's permissions and screens.
 */
data class User (
    val userId: String = "",
    val userType: UserType = UserType.Entrant,
)