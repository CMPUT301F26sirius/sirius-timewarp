package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [UserRepository].
 *
 * Stores and retrieves users from an in-memory users collection.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    private val usersState = MutableStateFlow<Map<String, User>>(emptyMap())

    /**
     * Returns all users registered as campsite owners.
     *
     * @return campsite owners in the fake collection.
     */
    override suspend fun getCampsiteOwners(): MutableList<User>? {
        return usersState.value.values
            .filter { it.userType == UserType.CampsiteOwner }
            .toMutableList()
    }

    /**
     * Returns all users registered as administrators.
     *
     * @return administrators in the fake collection.
     */
    override suspend fun getAdmins(): MutableList<User>? {
        return usersState.value.values
            .filter { it.userType == UserType.Admin }
            .toMutableList()
    }

    /**
     * Returns all users registered as entrants.
     *
     * @return entrants in the fake collection.
     */
    override suspend fun getEntrants(): MutableList<User>? {
        return usersState.value.values
            .filter { it.userType == UserType.Entrant }
            .toMutableList()
    }

    /**
     * Returns every user in the fake collection.
     *
     * @return all stored users.
     */
    override suspend fun getUsers(): MutableList<User>? {
        return usersState.value.values.toMutableList()
    }

    /**
     * Creates or replaces a user by ID.
     *
     * @param user user to store.
     */
    override suspend fun createUser(user: User) {
        usersState.value = usersState.value + (user.userId to user)
    }

    override suspend fun updateNotificationsEnabled(userId: String, enabled: Boolean) {
        usersState.value[userId]?.let { user ->
            usersState.value = usersState.value + (userId to user.copy(notificationsEnabled = enabled))
        }
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId ID of the user to retrieve.
     * @return the matching user, or `null` when it does not exist.
     */
    override suspend fun getUser(userId: String): User? {
        return usersState.value[userId]
    }

    /**
     * Observes a user and emits whenever that user's stored value changes.
     *
     * @param userId ID of the user to observe.
     * @return a flow containing the matching user, or `null` when it does not exist.
     */
    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        usersState
            .onEach { users ->
            trySend(users[userId])
            }
            .launchIn(this)
        awaitClose()
    }

    /**
     * Removes all users from the fake collection.
     */
    fun clear() {
        usersState.value = emptyMap()
    }

    /**
     * Adds a user synchronously for test setup.
     *
     * @param user user to add to the fake collection.
     */
    fun addUser(user: User) {
        usersState.value = usersState.value + (user.userId to user)
    }
}
