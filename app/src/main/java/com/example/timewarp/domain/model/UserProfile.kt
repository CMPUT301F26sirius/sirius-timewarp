package com.example.timewarp.domain.model

/**
 * [UserProfile] data class
 *
 * Combined user account and profile data for combined queries.
 *
 * @property user - User account information, when available.
 * @property profile - Personal profile information, when available.
 */
data class UserProfile(
    val user: User?,
    val profile: Profile?
)