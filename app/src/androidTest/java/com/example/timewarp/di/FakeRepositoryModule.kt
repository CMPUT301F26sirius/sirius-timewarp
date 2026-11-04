package com.example.timewarp.di

import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeImageRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeMessageRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeRatingRepository
import com.example.timewarp.data.repository.FakeUserRepository
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
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Binds repository interfaces to their fake implementations.
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseRepositoryModule::class]
)
abstract class FakeRepositoryModule {
    /**
     * Binds [FakeAuthRepository] to [AuthRepository].
     *
     * @param implementation Fake authentication repository.
     * @return repository exposed through the [AuthRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: FakeAuthRepository
    ): AuthRepository

    /**
     * Binds [FakeUserRepository] to [UserRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [UserRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        implementation: FakeUserRepository
    ): UserRepository

    /**
     * Binds [FakeCampsiteRepository] to [CampsiteRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [CampsiteRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindCampsiteRepository(
        implementation: FakeCampsiteRepository
    ): CampsiteRepository

    /**
     * Binds [FakeRatingRepository] to [RatingRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [RatingRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindRatingRepository(
        implementation: FakeRatingRepository
    ): RatingRepository

    /**
     * Binds [FakeImageRepository] to [ImageRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [ImageRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindImageRepository(
        implementation: FakeImageRepository
    ): ImageRepository

    /**
     * Binds [FakeLotteryRepository] to [LotteryRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [LotteryRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindLotteryRepository(
        implementation: FakeLotteryRepository
    ): LotteryRepository

    /**
     * Binds [FakeMessageRepository] to [MessageRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [MessageRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        implementation: FakeMessageRepository
    ): MessageRepository

    /**
     * Binds [FakeNotificationRepository] to [NotificationRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [NotificationRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        implementation: FakeNotificationRepository
    ): NotificationRepository

    /**
     * Binds [FakeProfileRepository] to [ProfileRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [ProfileRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: FakeProfileRepository
    ): ProfileRepository

    /**
     * Binds [FakeReservationRepository] to [ReservationRepository].
     *
     * @param implementation Fake user repository.
     * @return the repository exposed through the [ReservationRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindReservationRepository(
        implementation: FakeReservationRepository
    ): ReservationRepository
}