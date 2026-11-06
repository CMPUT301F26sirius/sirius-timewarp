package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and observing [Profile] objects.
 */
interface ProfileRepository {
    /**
     * @return all profiles, or `null` when no collection result is available.
     */
    suspend fun getProfiles(): MutableList<Profile>?

    /**
     * @param profile profile to create.
     */
    suspend fun createProfile(profile: Profile)

    /**
     * @param userId identifier of the profile owner.
     * @param profile replacement profile data.
     */
    suspend fun editProfile(userId: String, profile: Profile)

    /**
     * @param userId identifier of the profile to retrieve.
     * @return the matching profile, or `null` when it does not exist.
     */
    suspend fun getProfile(userId: String): Profile?

    /**
     * @param userId identifier of the profile to observe.
     * @return a flow emitting the profile, or `null` when it is unavailable.
     */
    fun observeProfile(userId: String): Flow<Profile?>
}