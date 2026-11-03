package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ProfileRepository].
 *
 * Stores and retrieves profiles from the Firestore `profile` collection.
 *
 * @property firestore Firestore instance used to access profiles.
 */
@Singleton
class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {
    private val profiles = firestore.collection("profile")

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getProfiles(): MutableList<Profile>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createProfile(profile: Profile) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editProfile(userId: String, profile: Profile) {
        profiles.document(userId)
            .set(profile, SetOptions.merge())
            .await()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getProfile(userId: String): Profile? {
        val snapshot = profiles.document(userId).get().await()

        return snapshot
            .takeIf { it.exists() }
            ?.toObject(Profile::class.java)
            ?.copy(userId = userId)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeProfile(userId: String): Flow<Profile?> = callbackFlow {
        val listener = firestore.collection("profile")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val profile = snapshot
                    ?.takeIf { it.exists() }
                    ?.toObject(Profile::class.java)
                    ?.copy(userId = userId)

                trySend(profile)
            }

        awaitClose {
            listener.remove()
        }
    }
}
