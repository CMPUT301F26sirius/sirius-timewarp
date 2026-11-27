package com.example.timewarp.feature.directmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
import com.example.timewarp.ui.component.BaseButtonType
import com.example.timewarp.ui.component.RemoteImage
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * [DirectMessageScreen]
 *
 * Displayed when a user navigates to a direct message
 */
@Composable
fun DirectMessageScreen(
    targetUserId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.DirectMessage,
    navController: NavController = rememberNavController(),
    viewModel: DirectMessageViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // State Interpretation
    LaunchedEffect(targetUserId) {
        viewModel.openConversation(targetUserId)
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        when (val currentState = state) {
            DirectMessageState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Text("Loading conversation...")
                }
            }

            is DirectMessageState.Error -> Text(
                text = currentState.message,
                color = MaterialTheme.colorScheme.error,
            )

            is DirectMessageState.Content -> DirectMessageContent(
                state = currentState,
                onDraftChange = viewModel::onDraftMessageChange,
                onSend = viewModel::sendMessage,
            )
        }
    }
}

/**
 * [DirectMessageContent]
 *
 * Actual content that gets displayed if successful
 */
@Composable
private fun DirectMessageContent(
    state: DirectMessageState.Content,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    // State
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val displayName = listOfNotNull(state.profile?.firstName, state.profile?.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { state.targetUserId }

    // State Interpretation
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    // User Interface
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Profile info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val profileImage = state.profileImage?.takeIf { it.url.isNotBlank() }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (profileImage != null) {
                    RemoteImage(
                        image = profileImage,
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
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Display bubbles
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Text(
                        text = "No messages yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.messages) { message ->
                    MessageBubble(
                        message = message,
                        isOutgoing = message.fromUserId == state.currentUserId,
                    )
                }
            }
        }

        state.sendError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.draftMessage,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .height(ComponentStyles.ButtonHeight),
                shape = ComponentStyles.ButtonShape,
                textStyle = MaterialTheme.typography.labelLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                ),
                placeholder = { Text("Write a message") },
                enabled = !state.isSending,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSend()
                        focusManager.clearFocus()
                    }
                ),
            )
            BaseButton(
                onClick = {
                    onSend()
                    focusManager.clearFocus()
                },
                text = if (state.isSending) "Sending..." else "Send",
                type = BaseButtonType.Default,
                enabled = state.draftMessage.isNotBlank() && !state.isSending,
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOutgoing) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (isOutgoing) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (isOutgoing) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectMessageScreenPreview() {
    TimewarpTheme {
        DirectMessageContent(
            state = DirectMessageState.Content(
                currentUserId = "current-user",
                targetUserId = "target-user",
                profile = Profile(firstName = "Alex", lastName = "Doe"),
                profileImage = null,
                messages = listOf(
                    Message(
                        message = "Hi there!",
                        fromUserId = "target-user",
                        toUserId = "current-user",
                    ),
                    Message(
                        message = "Hello!",
                        fromUserId = "current-user",
                        toUserId = "target-user",
                    ),
                ),
            ),
            onDraftChange = {},
            onSend = {},
        )
    }
}
