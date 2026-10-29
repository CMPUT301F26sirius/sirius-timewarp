package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [AuthRepository].
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    /**
     * Returns the ID of the currently signed-in Firebase user, and `null` when
     * no user is authenticated.
     *
     * @return the current user's ID, and `null` if no user is signed in.
     */
    override fun getCurrentUserId(): String? {
        return "0"
    }

    /**
     * Signs in anonymously and returns the resulting user ID.
     *
     * @return the signed-in Firebase user's ID.
     */
    override suspend fun signIn(): String {
        return "0"
    }
}