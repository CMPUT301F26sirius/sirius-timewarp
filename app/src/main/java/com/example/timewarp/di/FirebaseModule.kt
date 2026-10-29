package com.example.timewarp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides application wide Firebase dependencies for repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    /**
     * Provides the application-wide Firestore instance.
     *
     * @return the default Firebase Firestore instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    /**
     * Provides the application-wide Firebase Authentication instance.
     *
     * @return the default Firebase Authentication instance.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}