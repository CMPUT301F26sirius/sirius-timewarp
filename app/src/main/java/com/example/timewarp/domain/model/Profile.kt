package com.example.timewarp.domain.model

/**
 * [Profile] data class.
 *
 * Personal profile information associated with a [User].
 *
 * Issues:
 * - images should point to Image via imageId.
 *
 * @property userId - Identifier of the associated user.
 * @property firstName - User's first name.
 * @property lastName - User's last name.
 * @property email - User's email address.
 * @property phoneNumber - User's phone number.
 * @property profilePicture - url of pfp
 * @property interests - Interests selected by the user.
 */
data class Profile (
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePicture: String = "",
    val interests: List<String> = emptyList<String>()
)