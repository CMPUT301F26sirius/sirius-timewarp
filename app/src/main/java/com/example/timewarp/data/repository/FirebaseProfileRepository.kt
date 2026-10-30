package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ProfileRepository].
 *
 * Stores and retrieves profiles from Firestore.
 *
 * @property firestore Firestore instance used to access profiles.
 */
@Singleton
class FirebaseProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {
    override suspend fun getProfiles(): MutableList<Profile>? {
        TODO("Not yet implemented")
    }

    override suspend fun createProfile(profile: Profile) {
        TODO("Not yet implemented")
    }

    override suspend fun editProfile(userId: String, profile: Profile) {
        TODO("Not yet implemented")
    }

    override suspend fun getProfile(userId: String): Profile? {
        TODO("Not yet implemented")
    }
}
