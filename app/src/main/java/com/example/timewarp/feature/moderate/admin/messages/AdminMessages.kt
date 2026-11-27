package com.example.timewarp.feature.moderate.admin.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/*
* This function is for Admin browse all the message logs section.
*/
@Composable
fun AdminMessages() {
    Box(
        Modifier.fillMaxSize().testTag("admin_messages_placeholder"),
    ) {
        Text("Messages: Coming Soon")
    }
}
