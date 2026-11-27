package com.example.timewarp.feature.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.feature.registration.RegistrationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.RemoteImage
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * [MessagesScreen]
 *
 * Displays a directory of people to direct message
 */
@Composable
fun MessagesScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Messages,
    navController: NavController = rememberNavController(),
    viewModel: MessagesViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        MessagesContent(
            state = state,
            onOpenConversation = { targetUserId ->
                navController.navigate(Screen.DirectMessage.createRoute(targetUserId))
            },
            onRetry = viewModel::load,
        )
    }
}

/**
 * Actual content of the screen, LazyColumn of MessageConversationCard if success
 */
@Composable
private fun MessagesContent(
    state: MessagesState,
    onOpenConversation: (String) -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        MessagesState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Text("Loading messages...")
            }
        }

        is MessagesState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                )
                BaseButton(
                    onClick = onRetry,
                    text = "Retry",
                )
            }
        }

        is MessagesState.Content -> {
            if (state.conversations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No conversations yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = state.conversations,
                        key = { it.targetUserId },
                    ) { conversation ->
                        MessageConversationCard(
                            conversation = conversation,
                            currentUserId = state.currentUserId,
                            // Open reservation
                            onClick = { onOpenConversation(conversation.targetUserId) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card with info about the direct message
 */
@Composable
private fun MessageConversationCard(
    conversation: MessageConversation,
    currentUserId: String,
    onClick: () -> Unit,
) {
    // Info
    val displayName = listOfNotNull(conversation.profile?.firstName, conversation.profile?.lastName,)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { conversation.targetUserId }
    val image = conversation.profileImage?.takeIf { it.url.isNotBlank() }
    val latestMessagePreview = if (conversation.latestMessage.fromUserId == currentUserId) {
        "You: ${conversation.latestMessage.message}"
    } else {
        conversation.latestMessage.message
    }

    // Card
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("message_conversation_${conversation.targetUserId}"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (image != null) {
                    RemoteImage(
                        image = image,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile picture unavailable",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = latestMessagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagesScreenPreview() {
    TimewarpTheme {
        MessagesContent(
            state = MessagesState.Content(
                currentUserId = "current-user",
                conversations = listOf(
                    MessageConversation(
                        targetUserId = "target-user",
                        profile = Profile(firstName = "Alex", lastName = "Doe"),
                        profileImage = null,
                        latestMessage = Message(
                            timestamp = 2,
                            message = "Looking forward to the trip!",
                            fromUserId = "target-user",
                            toUserId = "current-user",
                        ),
                    ),
                ),
            ),
            onOpenConversation = {},
            onRetry = {},
        )
    }
}
