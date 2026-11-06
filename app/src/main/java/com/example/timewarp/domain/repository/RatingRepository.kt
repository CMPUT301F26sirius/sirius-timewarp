package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * [RatingRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface RatingRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getUserRatings(userId: String): MutableList<Rating>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getRatings(): MutableList<Rating>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun createRating(rating: Rating)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getRating(ratingId: String): Rating?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>>
}