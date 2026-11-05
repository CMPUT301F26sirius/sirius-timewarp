package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.RatingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [RatingRepository].
 *
 * Stores and retrieves ratings from the a `rating` collection.
 */
@Singleton
class FakeRatingRepository @Inject constructor() : RatingRepository {
    private val ratings = MutableStateFlow<List<Rating>>(emptyList())

    fun clear() {
        ratings.value = emptyList()
    }
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRatings(): MutableList<Rating>? {
        return ratings.value.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>? {
        return ratings.value.filter { it.campsiteId == campsiteId }.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getUserRatings(userId: String): MutableList<Rating>? {
        return ratings.value.filter { it.userId == userId }.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createRating(rating: Rating) {
        ratings.value = ratings.value + rating
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getRating(ratingId: String): Rating? {
        return ratings.value.firstOrNull { it.ratingId == ratingId }
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>> =
        ratings.map { items -> items.filter { it.campsiteId == campsiteId } }
}