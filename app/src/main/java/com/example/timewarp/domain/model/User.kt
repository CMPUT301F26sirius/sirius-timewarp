package com.example.timewarp.domain.model

class User (
    val userId: String,
    val userType: UserType = UserType.Entrant,
)