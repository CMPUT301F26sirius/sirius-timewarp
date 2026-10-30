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
abstract class FirebaseRepositoryModule {
    /**
     * Binds [FirebaseAuthRepository] to [AuthRepository].
     *
     * @param implementation Firebase-backed authentication repository.
     * @return repository exposed through the [AuthRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: FirebaseAuthRepository
    ): AuthRepository

    /**
     * Binds [FirebaseUserRepository] to [UserRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [UserRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        implementation: FirebaseUserRepository
    ): UserRepository

    /**
     * Binds [FirebaseCampsiteRepository] to [CampsiteRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [CampsiteRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindCampsiteRepository(
        implementation: FirebaseCampsiteRepository
    ): CampsiteRepository

    /**
     * Binds [FirebaseRatingRepository] to [RatingRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [RatingRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindRatingRepository(
        implementation: FirebaseRatingRepository
    ): RatingRepository
}