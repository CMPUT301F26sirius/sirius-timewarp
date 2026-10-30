package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [UserRepository].
 *
 * Stores and retrieves users from the Firestore `users` collection.
 *
 * @property firestore Firestore instance used to access user documents.
 */
@Singleton
class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun getCampsiteOwners(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    override suspend fun getAdmins(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    override suspend fun getEntrants(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    override suspend fun getUsers(): MutableList<User>? {
        TODO("Not yet implemented")
    }

    /**
     * Writes to a user's Firestore document.
     *
     * @param user user object to add.
     */
    override suspend fun createUser(user: User) {
        firestore.collection("users")
            .document(user.userId)
            .set(user)
            .await()
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId id of the user document to retrieve.
     * @return the user if the document exists, and `null` otherwise.
     */
    override suspend fun getUser(userId: String): User? {
        val document = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        return document.toObject(User::class.java)
            ?.copy(userId = document.id)
    }
}