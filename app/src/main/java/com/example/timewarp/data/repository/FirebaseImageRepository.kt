package com.example.timewarp.data.repository

import com.example.timewarp.domain.repository.ImageRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ImageRepository].
 *
 * Stores and retrieves images from the Firestore using cloud Storage
 *
 * @property firestore Firestore instance used to access images.
 */
@Singleton
class FirebaseImageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ImageRepository {

}
