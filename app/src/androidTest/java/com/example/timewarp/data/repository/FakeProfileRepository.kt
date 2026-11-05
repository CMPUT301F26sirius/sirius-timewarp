package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [ProfileRepository].
 *
 * Stores and retrieves profiles from a `profile` collection.
 */
@Singleton
class FakeProfileRepository @Inject constructor() : ProfileRepository {
    private val profiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getProfiles(): MutableList<Profile>? {
        return profiles.value.values.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createProfile(profile: Profile) {
        profiles.value = profiles.value + (profile.userId to profile)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editProfile(userId: String, profile: Profile) {
        profiles.value = profiles.value + (userId to profile)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getProfile(userId: String): Profile? {
        return profiles.value[userId]
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeProfile(userId: String): Flow<Profile?> =
        profiles.map { it[userId] }

    /**
     * NEEDS_DOCUMENTATION
     */
    fun addProfile(profile: Profile) {
        profiles.value = profiles.value + (profile.userId to profile)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    fun clear() {
        profiles.value = emptyMap()
    }
}
