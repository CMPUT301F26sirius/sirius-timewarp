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
 * Firebase-backed implementation of [RatingRepository].
 *
 * Stores and retrieves ratings from the Firestore `rating` collection.
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

    override fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>> = callbackFlow {
        val listener = firestore.collection("rating")
            .whereEqualTo("campsiteId", campsiteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val ratings = snapshot
                    ?.documents
                    .orEmpty()
                    .mapNotNull { it.toObject(Rating::class.java)?.copy(ratingId = it.id) }

                trySend(ratings)
            }

        awaitClose {
            listener.remove()
        }
    }
}