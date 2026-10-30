package com.example.timewarp.domain.model

data class Rating(
    val campsiteId: String,
    val userId: String,
    var rating: Int,
    var review: String,
)