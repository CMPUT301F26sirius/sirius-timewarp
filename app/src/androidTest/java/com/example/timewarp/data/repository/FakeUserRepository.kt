package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [UserRepository].
 *
 * Stores and retrieves users from the a `users` collection.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsiteOwners(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getAdmins(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getEntrants(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getUsers(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createUser(user: User) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getUser(userId: String): User? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        TODO("Not yet implemented")
    }
}