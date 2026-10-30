package com.example.timewarp.di

import com.example.timewarp.data.repository.Eric_MockReservationRepository
import com.example.timewarp.domain.repository.Eric_ReservationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to their Firebase-backed implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MockRepositoryModule {
    /**
     * Binds [Eric_MockReservationRepository] to [Eric_ReservationRepository].
     *
     * @param implementation Firebase-backed authentication repository.
     * @return repository exposed through the [Eric_ReservationRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindEricReservationRepository(
        implementation: Eric_MockReservationRepository
    ): Eric_ReservationRepository
}