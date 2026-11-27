package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
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
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRatings(): MutableList<Rating>? {
        return firestore.collection("rating")
            .get()
            .await()
            .documents
            .mapNotNull { it.toObject(Rating::class.java)?.copy(ratingId = it.id) }
            .toMutableList()
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
        val snapshot = firestore.collection("rating")
            .whereEqualTo("userId", userId).get().await()

        return snapshot.documents.mapNotNull {
            it.toObject(Rating::class.java)?.copy(ratingId = it.id)
        }.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createRating(rating: Rating) {
        firestore.collection("rating")
            .document(rating.ratingId)
            .set(rating)
            .await()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRating(ratingId: String): Rating? {
        return firestore.collection("rating")
            .document(ratingId)
            .get()
            .await()
            .toObject(Rating::class.java)?.copy(ratingId = ratingId)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
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