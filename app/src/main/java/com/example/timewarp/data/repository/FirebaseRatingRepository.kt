package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [RatingRepository].
 *
 * Stores and retrieves ratings from the Firestore `ratings` collection.
 *
 * @property firestore Firestore instance used to access user documents.
 */
@Singleton
class FirebaseRatingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RatingRepository {
    override suspend fun getRatings(): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserRatings(userId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun createRating(rating: Rating) {
        TODO("Not yet implemented")
    }

    override suspend fun getRating(ratingId: String): Rating? {
        TODO("Not yet implemented")
    }
}