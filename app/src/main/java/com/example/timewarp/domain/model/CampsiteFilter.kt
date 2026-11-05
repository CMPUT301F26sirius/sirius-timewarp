package com.example.timewarp.domain.model

import com.google.type.Date

/*
* Purpose: This file holds the user's filter choices for browsing the campsites:
*          Text search, location, and number of guests and date range.
*/
data class CampsiteFilter(
    val query: String = "",
    val location: String = "",
    val guests: Int? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    fun matches(campsite: Campsite): Boolean {
        val matchesQuery = query.isBlank() ||
                campsite.name.contains(query, ignoreCase = true)

        val matchesLocation = location.isBlank() ||
                campsite.location.address.contains(location, ignoreCase = true)

        val matchesGuests = guests == null ||
                campsite.occupancy.maxGuests >= guests

        val matchesDates = true
        /*
        * The Dates filtering needs availability data which we do not have so far
        */

        return matchesQuery && matchesLocation && matchesGuests && matchesDates
    }
}