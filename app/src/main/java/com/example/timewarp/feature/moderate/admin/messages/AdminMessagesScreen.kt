package com.example.timewarp.feature.moderate.admin.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timewarp.feature.moderate.admin.AdminBrowseList
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * Displays all sent messages for administrators, including the sender and recipient.
 *
 * Shows loading, error, and empty states while observing the message list.
 *
 * @param viewModel supplies the observed messages and their participant labels.
 */
@Composable
fun AdminMessagesScreen(
    viewModel: AdminMessagesViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // User interface
    AdminMessagesContent(state = state)
}

/**
 * Displays the loading, error, empty, or message-list state.
 *
 * @param state current state of the admin message log.
 */
@Composable
private fun AdminMessagesContent(
    state: AdminMessagesState
) {
    AdminBrowseList(
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.items.isEmpty(),
        listTag = "admin_messages_list",
        emptyText = "No Messages"
    ) {
        itemsIndexed(state.items) { index, item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_message_item_$index"),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "From: ${item.sender}",
                        style = MaterialTheme.typography.titleSmall,
                        color = PitchForest
                    )
                    Text(
                        "To: ${item.recipient}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PitchForest
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        item.message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminMessagesScreenPreview() {
    TimewarpTheme {
        AdminMessagesContent(
            state = AdminMessagesState(
                isLoading = false,
                items = listOf(
                    AdminMessageItem(
                        message = "Looking forward to the trip!",
                        sender = "Alex Parker",
                        recipient = "Bryn Taylor"
                    ),
                    AdminMessageItem(
                        message = "Thanks for the details.",
                        sender = "Bryn Taylor",
                        recipient = "Alex Parker"
                    )
                )
            )
        )
    }
}
