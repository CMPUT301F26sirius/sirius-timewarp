package com.example.timewarp.domain.model

data class User (
    val userId: String = "",
    val userType: UserType = UserType.Entrant,
)