package com.example.timewarp.domain.model

/*
* Purpose: This file holds the user's filter choices on location and number of guests
*/
data class CampsiteFilter(
    val location: String = "",
    val guests: Int? = null,
)