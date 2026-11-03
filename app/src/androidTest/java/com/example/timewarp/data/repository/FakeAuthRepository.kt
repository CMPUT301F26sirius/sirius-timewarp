package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [AuthRepository].
 *
 * Keeps authentication state in memory so instrumented tests can control whether
 * the app starts with an authenticated user.
 */
@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {
    private val currentUserIdState = MutableStateFlow<String?>(null)

    /**
     * Flow containing the currently authenticated fake user's ID.
     *
     * @return a state flow containing the current user ID, or `null` when signed out.
     */
    override val currentUserId: Flow<String?> = currentUserIdState

    /**
     * Returns the currently authenticated fake user's ID.
     *
     * @return the current user ID, or `null` when signed out.
     */
    override fun getUserId(): String? {
        return currentUserIdState.value
    }

    /**
     * Signs in the fake user and returns its ID.
     *
     * If a user is already authenticated, that user's ID is reused.
     *
     * @return the authenticated fake user's ID.
     */
    override suspend fun signIn(): String {
        val userId = currentUserIdState.value ?: "fake-user-id".also {
            currentUserIdState.value = it
        }
        return userId
    }

    /**
     * Sets the authenticated user used by the next app launch.
     *
     * @param userId the user ID to authenticate, or `null` to sign out.
     */
    fun setCurrentUserId(userId: String?) {
        currentUserIdState.value = userId
    }
}