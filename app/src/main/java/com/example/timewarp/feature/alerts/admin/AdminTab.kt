package com.example.timewarp.feature.alerts.admin

/*
 * This Sealed interface shows the available tabs in the alerts section for admin
 */
sealed interface AdminTab {
    val label: String
    val key: String

    /*
    * Admin tab for user profiles
    */
    data object Profiles: AdminTab {
        override val label = "Profiles";
        override val key = "Profiles"
    }

    /*
    * Admin tab for images. (moderating or manage images)
    */
    data object Images: AdminTab {
        override val label = "Images";
        override val key = "Images"
    }

    /*
    * Admin tab for managing the messages
    */
    data object Messages: AdminTab {
        override val label = "Messages";
        override val key = "Messages"
    }

    /*
    * Admin tab for managing the notifications
    */
    data object Notifications: AdminTab {
        override val label = "Notifications";
        override val key = "Notifications"
    }

    companion object {
        val all: List<AdminTab> = listOf(Profiles, Images, Messages, Notifications)
    }
}