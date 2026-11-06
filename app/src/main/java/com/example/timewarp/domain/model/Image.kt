package com.example.timewarp.domain.model

/**
 * [Image] data class
 *
 * Image metadata stored for a user-uploaded campsite image.
 *
 * Images are stored in the Firebase `Image` collection. Other models should
 * preferably reference [imageId] rather than storing image URLs directly.
 *
 * @property uploadingUserId - UUID of the user who uploaded this image.
 * @property imageId - UUID identifying this image.
 * @property url - URL for the image in cloud storage.
 */
data class Image (
    val uploadingUserId: String = "",
    val imageId: String = "",
    val url: String = "",
)