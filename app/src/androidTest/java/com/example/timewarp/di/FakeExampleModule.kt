package com.example.timewarp.di

import com.example.timewarp.data.repository.FakeExampleRepository
import com.example.timewarp.domain.repository.ExampleRepository
import dagger.Module
import dagger.Binds
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FirebaseExampleModule::class]
)
abstract class FakeExampleModule {
    @Binds
    @Singleton
    abstract fun bindExampleRepository(
        implementation: FakeExampleRepository
    ): ExampleRepository
}