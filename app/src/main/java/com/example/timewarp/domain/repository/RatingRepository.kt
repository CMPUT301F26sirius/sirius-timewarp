package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>?

    suspend fun getUserRatings(userId: String): MutableList<Rating>?

    suspend fun getRatings(): MutableList<Rating>?

    suspend fun createRating(rating: Rating)

    suspend fun getRating(ratingId: String): Rating?

    fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>>
}