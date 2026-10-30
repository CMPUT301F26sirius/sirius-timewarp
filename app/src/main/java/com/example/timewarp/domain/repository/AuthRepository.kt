package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<String?>

    fun getUserId(): String?

    suspend fun signIn(): String
}