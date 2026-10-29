package com.example.timewarp.di

import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.repository.AuthRepository
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
     * @param implementation fake authentication repository.
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
     * @param implementation fake user repository.
     * @return the repository exposed through the [UserRepository] interface.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        implementation: FakeUserRepository
    ): UserRepository
}