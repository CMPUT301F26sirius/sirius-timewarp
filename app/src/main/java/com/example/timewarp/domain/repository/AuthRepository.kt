package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * [AuthRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface AuthRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    val currentUserId: Flow<String?>

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @return userId
     */
    fun getUserId(): String?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @return userId
     */
    suspend fun signIn(): String
}