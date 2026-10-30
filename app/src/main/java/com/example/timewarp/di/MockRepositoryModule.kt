package com.example.timewarp.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds repository interfaces to their Firebase-backed implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MockRepositoryModule {
//    /**
//     * Binds [MockAuthRepository] to [AuthRepository].
//     *
//     * @param implementation Firebase-backed authentication repository.
//     * @return repository exposed through the [AuthRepository] interface.
//     */
//    @Binds
//    @Singleton
//    abstract fun bindAuthRepository(
//        implementation: MockAuthRepository
//    ): AuthRepository
}