package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User

interface UserRepository {
    suspend fun getAdmins(): MutableList<User>?

    suspend fun getCampsiteOwners(): MutableList<User>?

    suspend fun getEntrants(): MutableList<User>?

    suspend fun getUsers(): MutableList<User>?

    suspend fun createUser(user: User)

    suspend fun getUser(userId: String): User?
}