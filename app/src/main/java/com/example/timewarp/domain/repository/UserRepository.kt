package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.ExampleModel
import com.example.timewarp.domain.model.User

interface UserRepository {
    suspend fun createUser(user: User)

    suspend fun getUser(userId: String): User?
}