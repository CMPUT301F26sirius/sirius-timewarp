package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * [UserRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface UserRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @return owners - a collection of user objects, who are admins.
     */
    suspend fun getAdmins(): MutableList<User>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @return owners - a collection of user objects, who are owners.
     */
    suspend fun getCampsiteOwners(): MutableList<User>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @return entrants - a collection of user objects, who are entrants.
     */
    suspend fun getEntrants(): MutableList<User>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @reutrn users - a collection of user objects.
     */
    suspend fun getUsers(): MutableList<User>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @param user [String] - user to create. Inserts user into collection.
     */
    suspend fun createUser(user: User)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @param userId [String] - userId to get.
     * @return user - user object for specified user.
     */
    suspend fun getUser(userId: String): User?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     *
     * @param userId [String] - userId to observe.
     * @return userFlow - user object for observed user.
     */
    fun observeUser(userId: String): Flow<User?>
}