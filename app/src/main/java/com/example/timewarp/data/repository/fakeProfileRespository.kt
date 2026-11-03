package com.example.timewarp.data.repository

import android.util.Log
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.repository.ProfileRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

@Singleton
class fakeProfileRespository @Inject constructor() : ProfileRepository{
    private val profiles = MutableStateFlow(
        mapOf(
            "test-user" to Profile(
                userId = "test-user",
                firstName = "John",
                lastName = "Doe",
                email = "johndoe@gmail.com",
                phoneNumber = "5870000000"
            )
        )
    )

    // Set to true to make every call fail, for testing error messages
    private var shouldFail = false

    override suspend fun getProfiles(): MutableList<Profile>? {
        simulateNetwork()
        return profiles.value.values.toMutableList()
    }

    override suspend fun createProfile(profile: Profile) {
        simulateNetwork()
        profiles.update { it + (profile.userId to profile) }
    }

    override suspend fun editProfile(userId: String, profile: Profile) {
        simulateNetwork()
        profiles.update { it + (userId to profile.copy(userId = userId)) }
        Log.d("FakeProfileRepository", "Saved $userId: ${profiles.value[userId]}")
    }

    override suspend fun getProfile(userId: String): Profile? {
        simulateNetwork()
        return profiles.value[userId]
    }

    // Emits again whenever the profile changes, like a Firestore listener
    override fun observeProfile(userId: String): Flow<Profile?> =
        profiles.map { it[userId] }

    private suspend fun simulateNetwork() {
        delay(500)
        if (shouldFail) throw Exception("Fake network error")
    }
}