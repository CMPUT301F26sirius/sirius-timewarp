package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [UserRepository].
 *
 * Stores and retrieves users from a `users` collection.
 */
@Singleton
class FakeUserRepository @Inject constructor() : UserRepository {
    private val _users = HashMap<String, User>();
    private val users = MutableStateFlow<Map<String, User>>(emptyMap())

    /**
     * Writes to a user's to the list of users.
     *
     * @param user user object to add.
     */
    override suspend fun createUser(user: User) {
        _users[user.userId] = user
        users.value = _users.toMap()
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId id of the user to retrieve.
     * @return the user if the user exists, and `null` otherwise.
     */
    override suspend fun getUser(userId: String): User? {
        return _users[userId]
    }

    override fun observeUser(userId: String): Flow<User?> =
        users.map { it[userId] }
}