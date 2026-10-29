package com.example.timewarp.domain.repository

interface AuthRepository {
    fun getCurrentUserId(): String?

    suspend fun signIn(): String
}