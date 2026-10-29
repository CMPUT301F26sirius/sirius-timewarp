package com.example.timewarp.di

import com.example.timewarp.data.repository.FirebaseAuthRepository
import com.example.timewarp.data.repository.FirebaseUserRepository
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ExampleRepository
import com.example.timewarp.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Binds
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        implementation: FirebaseUserRepository
    ): UserRepository
}