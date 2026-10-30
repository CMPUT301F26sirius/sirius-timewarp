package com.example.timewarp.domain.repository

interface AuthRepository {
    fun getUserId(): String?

    suspend fun signIn(): String
}