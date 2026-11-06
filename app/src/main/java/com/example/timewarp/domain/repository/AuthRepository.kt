package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository for authentication state and sign-in operations.
 */
interface AuthRepository {
    /**
     * @return a flow containing the current user identifier, or `null` when signed out.
     */
    val currentUserId: Flow<String?>

    /**
     * @return the current user identifier, or `null` when signed out.
     */
    fun getUserId(): String?

    /**
     * @return the signed-in user identifier.
     */
    suspend fun signIn(): String
}