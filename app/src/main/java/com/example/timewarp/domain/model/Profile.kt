package com.example.timewarp.domain.model

data class Profile (
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePicture: String = "",
    val interests: List<String> = emptyList<String>()
)