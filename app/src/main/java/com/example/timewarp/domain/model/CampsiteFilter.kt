package com.example.timewarp.domain.model

/*
* Purpose: This file holds the user's filter choices for browsing the campsites:
*          Text search, location, and number of guests.
*/
data class CampsiteFilter(
    val query: String = "",
    val location: String = "",
    val guests: Int? = null,
) {
    fun matches(campsite: Campsite): Boolean {
        val matchesQuery = query.isBlank() ||
                campsite.name.contains(query, ignoreCase = true)

        val matchesLocation = location.isBlank() ||
                campsite.location.address.contains(location, ignoreCase = true)

        val matchesGuests = guests == null ||
                campsite.occupancy.maxGuests >= guests

        return matchesQuery && matchesLocation && matchesGuests
    }
}