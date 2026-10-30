package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfiles(): MutableList<Profile>?

    suspend fun createProfile(profile: Profile)

    suspend fun editProfile(userId: String, profile: Profile)

    suspend fun getProfile(userId: String): Profile?

    fun observeProfile(userId: String): Flow<Profile?>
}