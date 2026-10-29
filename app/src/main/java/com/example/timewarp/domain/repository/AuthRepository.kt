package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.ExampleModel

interface AuthRepository {
    fun getCurrentUserId(): String?

    suspend fun signInAnonymously(): String
}