package com.example.timewarp.di

import com.example.timewarp.data.repository.FirebaseAuthRepository
import com.example.timewarp.data.repository.FirebaseCampsiteRepository
import com.example.timewarp.data.repository.FirebaseRatingRepository
import com.example.timewarp.data.repository.FirebaseUserRepository
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their Firebase-backed implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FakeRepositoryModule {
//    /**
//     * Binds [FakeAuthRepository] to [AuthRepository].
//     *
//     * @param implementation Firebase-backed authentication repository.
//     * @return repository exposed through the [AuthRepository] interface.
//     */
//    @Binds
//    @Singleton
//    abstract fun bindAuthRepository(
//        implementation: FakeAuthRepository
//    ): AuthRepository
}