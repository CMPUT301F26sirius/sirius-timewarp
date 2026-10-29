package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun createUser(user: User) {
        firestore.collection("users")
            .document(user.userId)
            .set(user)
            .await()
    }

    override suspend fun getUser(userId: String): User? {
        val document = firestore.collection("users")
            .document(userId)
            .get()
            .await()

        return document.toObject(User::class.java)
//            ?.copy(id = document.id)
    }
}