package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun signInAnonymously(): String {
        val existingUser = auth.currentUser

        if (existingUser != null) {
            return existingUser.uid
        }

        return auth.signInAnonymously()
            .await()
            .user
            ?.uid
            ?: error("Anonymous sign-in did not return a user.")
    }
}