package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [ProfileRepository].
 *
 * Stores and retrieves profiles from a `profile` collection.
 */
@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {
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
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getProfile(userId: String): Profile? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeProfile(userId: String): Flow<Profile?> = callbackFlow {
        TODO("Not yet implemented")
    }
}
