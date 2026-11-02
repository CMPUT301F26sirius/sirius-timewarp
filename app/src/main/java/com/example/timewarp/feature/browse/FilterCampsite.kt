package com.example.timewarp.feature.browse

/*
* Purpose: This file holds the user's filter choices on location and number of guests
*/
data class FilterCampsite(
    val location: String = "",
    val guests: Int? = null,
)