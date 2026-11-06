package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Profile
import kotlinx.coroutines.flow.Flow

/**
 * [ProfileRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface ProfileRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getProfiles(): MutableList<Profile>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun createProfile(profile: Profile)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun editProfile(userId: String, profile: Profile)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getProfile(userId: String): Profile?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    fun observeProfile(userId: String): Flow<Profile?>
}