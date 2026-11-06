package com.example.timewarp.domain.model

import com.google.type.Date

/**
 * [CampsiteFilter] data class
 *
 * Purpose: This file holds the user's filter choices for browsing the campsites:
 *  Text search, location, and number of guests and date range.
 *
 * @property query - Query entered by user
 * @property location - A string location used to filter (address)
 * @property startDate - Start date specified by user
 * @property endDate - End date specified by user
*/
data class CampsiteFilter(
    val query: String = "",
    val location: String = "",
    val guests: Int? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    /**
     * Determines if a campsite is a match.
     */
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