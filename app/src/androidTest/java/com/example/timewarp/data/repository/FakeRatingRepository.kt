package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [RatingRepository].
 *
 * Stores and retrieves ratings from the a `rating` collection.
 */
@Singleton
class FakeRatingRepository @Inject constructor() : RatingRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRatings(): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getUserRatings(userId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createRating(rating: Rating) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRating(ratingId: String): Rating? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>> = callbackFlow {
        TODO("Not yet implemented")
    }
}