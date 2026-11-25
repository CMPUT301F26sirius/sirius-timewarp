package com.example.timewarp.feature.alerts.admin.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/*
* This function is for Admin browse all the notifications section.
*/
@Composable
fun AdminNotificationSection() {

    Box(
        Modifier.fillMaxSize().testTag("admin_notifications_placeholder"),
    ) {
        Text("Notifications: Coming Soon")
    }
}