package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [AuthRepository].
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    /**
     * Flow for acquiring current user's ID
     *
     * @return callbackFlow for the user's ID.
     */
    override val currentUserId: Flow<String?> = callbackFlow {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun getUserId(): String? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun signIn(): String {
        TODO("Not yet implemented")
    }
}