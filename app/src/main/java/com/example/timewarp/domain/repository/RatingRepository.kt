package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and retrieving [Rating] objects.
 */
interface RatingRepository {
    /**
     * @param campsiteId identifier of the campsite being rated.
     * @return ratings for the campsite, or `null` when no collection result is available.
     */
    suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>?

    /**
     * @param userId identifier of the rating author.
     * @return ratings submitted by the user, or `null` when no collection result is available.
     */
    suspend fun getUserRatings(userId: String): MutableList<Rating>?

    /**
     * @return all ratings, or `null` when no collection result is available.
     */
    suspend fun getRatings(): MutableList<Rating>?

    /**
     * @param rating rating to create.
     */
    suspend fun createRating(rating: Rating)

    /**
     * @param ratingId identifier of the rating to retrieve.
     * @return the matching rating, or `null` when it does not exist.
     */
    suspend fun getRating(ratingId: String): Rating?

    /**
     * @param campsiteId identifier of the campsite to observe.
     * @return a flow emitting the campsite's ratings.
     */
    fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>>
}