package com.example.timewarp.feature.browse

import com.example.timewarp.domain.model.UserType

/*
* This sealed interface shows the tabs on the Browse screen. Tabs shown depends on the user type
*/
sealed interface BrowseTab {
    val label: String
    val key: String

    data object Browse : BrowseTab {
        override val label = "Browse"
        override val key = "Browse"
    }

    data object MyCampsites : BrowseTab {
        override val label = "My Campsites"
        override val key = "MyCampsites"
    }

    data object AllCampsites : BrowseTab {
        override val label = "All Campsites"
        override val key = "AllCampsites"
    }

    companion object {
        /*
        * Entrants: 0 tabs
        * Campsites Owners: 2 tabs
        * Admins: all 3 tabs
        */
        fun tabsFor(userType: UserType?): List<BrowseTab> = when (userType) {
            UserType.Entrant, null -> listOf(Browse)
            UserType.CampsiteOwner -> listOf(Browse, MyCampsites)
            UserType.Admin -> listOf(Browse, MyCampsites, AllCampsites)
        }
    }
}