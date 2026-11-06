package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and observing [User] accounts.
 */
interface UserRepository {
    /**
     * @return administrator accounts, or `null` when no collection result is available.
     */
    suspend fun getAdmins(): MutableList<User>?

    /**
     * @return campsite-owner accounts, or `null` when no collection result is available.
     */
    suspend fun getCampsiteOwners(): MutableList<User>?

    /**
     * @return entrant accounts, or `null` when no collection result is available.
     */
    suspend fun getEntrants(): MutableList<User>?

    /**
     * @return all user accounts, or `null` when no collection result is available.
     */
    suspend fun getUsers(): MutableList<User>?

    /**
     * @param user user account to create.
     */
    suspend fun createUser(user: User)

    /**
     * Updates whether notifications are enabled for a user.
     *
     * @param userId identifier of the user to update.
     * @param enabled whether notifications should be enabled.
     */
    suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean)

    /**
     * @param userId identifier of the user to retrieve.
     * @return the matching user, or `null` when it does not exist.
     */
    suspend fun getUser(userId: String): User?

    /**
     * @param userId identifier of the user to observe.
     * @return a flow emitting the user, or `null` when it is unavailable.
     */
    fun observeUser(userId: String): Flow<User?>
}