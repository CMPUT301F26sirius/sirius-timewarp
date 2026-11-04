package com.example.timewarp.di

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

}