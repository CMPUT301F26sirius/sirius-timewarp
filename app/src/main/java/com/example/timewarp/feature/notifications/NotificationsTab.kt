package com.example.timewarp.feature.notifications

import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType

sealed interface NotificationsTab {
    val label: String
    val key: String

    data object Incoming: NotificationsTab {
        override val label = "Incoming"
        override val key = "Incoming"
    }

    data object Outgoing: NotificationsTab {
        override val label = "Outgoing"
        override val key = "Outgoing"
    }

    data object AllNotifications: NotificationsTab {
        override val label = "All Notifications"
        override val key = "AllNotifications"
    }

    companion object {

        fun tabsFor(userType: UserType?): List<NotificationsTab> = when (userType) {
            UserType.Entrant, null -> listOf(Incoming)
            UserType.CampsiteOwner -> listOf(Incoming, Outgoing)
            UserType.Admin -> listOf(Incoming, Outgoing, AllNotifications)
        }
    }
}