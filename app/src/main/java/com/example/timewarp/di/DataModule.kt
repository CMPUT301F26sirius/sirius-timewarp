package com.example.timewarp.di

import com.example.timewarp.data.repository.FirebaseExampleRepository
import com.example.timewarp.domain.repository.ExampleRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Binds
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExampleModule {
    @Binds
    @Singleton
    abstract fun bindExampleRepository(
        implementation: FirebaseExampleRepository
    ): ExampleRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()
}