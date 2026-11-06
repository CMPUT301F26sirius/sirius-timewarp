package com.example.timewarp.domain.model

/**
 * [Rating] data class
 *
 * User-submitted rating and review for a campsite.
 *
 * @property ratingId - Unique identifier for the rating.
 * @property campsiteId - Identifier of the reviewed campsite.
 * @property userId - Identifier of the user who submitted the rating.
 * @property rating - Numeric rating value.
 * @property review - Written review text.
 */
data class Rating(
    val ratingId: String = "",
    val campsiteId: String = "",
    val userId: String = "",
    var rating: Int = 0,
    var review: String = "",
)