package com.example.timewarp.domain.model

data class Rating(
    val ratingId: String = "",
    val campsiteId: String = "",
    val userId: String = "",
    var rating: Int = 0,
    var review: String = "",
)