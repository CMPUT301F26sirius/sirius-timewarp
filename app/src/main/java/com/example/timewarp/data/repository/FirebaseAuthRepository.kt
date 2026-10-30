package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [AuthRepository].
 *
 * Anonymous authentication is reused when a user is already signed in so
 * callers receive a stable Firebase user ID for the current session.
 * Guarantees persistence between sessions, and ensures that the user
 * doesn't need a password.
 *
 * @property auth Firebase Authentication instance used to manage the current user.
 */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {
    override val currentUserId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Returns the ID of the currently signed-in Firebase user, and `null` when
     * no user is authenticated.
     *
     * @return the current Firebase user's ID, and `null` if no user is signed in.
     */
    override fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Signs in anonymously and returns the resulting Firebase user ID.
     *
     * If a Firebase user is already signed in, no additional sign-in request
     * is made and the existing user's ID is returned.
     *
     * @return the signed-in Firebase user's ID.
     * @throws IllegalStateException if Firebase reports a successful sign-in
     * without returning a user.
     */
    override suspend fun signIn(): String {
        val existingUser = auth.currentUser
        if (existingUser != null) {
            // Reuse the current session instead of creating another user.
            return existingUser.uid
        }

        return auth.signInAnonymously()
            .await()
            .user
            ?.uid
            // Firebase should always return a user after a successful sign-in.
            ?: error("Anonymous sign-in did not return a user.")
    }
}