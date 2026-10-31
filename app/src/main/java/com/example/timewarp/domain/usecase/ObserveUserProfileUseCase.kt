package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.UserProfile
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.UserRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(userId: String): Flow<UserProfile?> =
        userRepository.observeUser(userId)
            .flatMapLatest { user ->
                if (user == null) {
                    flowOf(null)
                } else {
                    profileRepository.observeProfile(userId)
                        .map { profile ->
                            UserProfile(
                                user = user,
                                profile = profile
                            )
                        }
                }
            }
}