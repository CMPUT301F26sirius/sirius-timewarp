package com.example.timewarp.di

import com.example.timewarp.data.repository.FirebaseAuthRepository
import com.example.timewarp.data.repository.FirebaseCampsiteRepository
import com.example.timewarp.data.repository.FirebaseImageRepository
import com.example.timewarp.data.repository.FirebaseLotteryRepository
import com.example.timewarp.data.repository.FirebaseMessageRepository
import com.example.timewarp.data.repository.FirebaseNotificationRepository
import com.example.timewarp.data.repository.FirebaseProfileRepository
import com.example.timewarp.data.repository.FirebaseRatingRepository
import com.example.timewarp.data.repository.FirebaseReservationRepository
import com.example.timewarp.data.repository.FirebaseUserRepository
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ImageRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
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

    /**
     * Binds [FirebaseImageRepository] to [ImageRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [ImageRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindImageRepository(
        implementation: FirebaseImageRepository
    ): ImageRepository

    /**
     * Binds [FirebaseLotteryRepository] to [LotteryRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [LotteryRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindLotteryRepository(
        implementation: FirebaseLotteryRepository
    ): LotteryRepository

    /**
     * Binds [FirebaseMessageRepository] to [MessageRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [MessageRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        implementation: FirebaseMessageRepository
    ): MessageRepository

    /**
     * Binds [FirebaseNotificationRepository] to [NotificationRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [NotificationRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        implementation: FirebaseNotificationRepository
    ): NotificationRepository

    /**
     * Binds [FirebaseProfileRepository] to [ProfileRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [ProfileRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: FirebaseProfileRepository
    ): ProfileRepository

    /**
     * Binds [FirebaseReservationRepository] to [ReservationRepository].
     *
     * @param implementation Firebase-backed user repository.
     * @return the repository exposed through the [ReservationRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        implementation: FirebaseReservationRepository
    ): ReservationRepository
}