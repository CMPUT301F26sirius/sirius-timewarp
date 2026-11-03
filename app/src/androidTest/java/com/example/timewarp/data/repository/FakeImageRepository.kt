package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.ImageRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [ImageRepository].
 *
 * Stores and retrieves images from local storage
 */
@Singleton
class FakeImageRepository @Inject constructor() : ImageRepository {

}
