package com.example.timewarp.feature.directmessage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun DirectMessageScreen(
    targetUserId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.DirectMessage,
    navController: NavController = rememberNavController(),
    viewModel: DirectMessageViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(targetUserId) {
        viewModel.openConversation(targetUserId)
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (val currentState = state) {
                DirectMessageState.Idle -> Text("Loading conversation...")
                is DirectMessageState.Conversation -> {
                    Text("Conversation with ${currentState.targetUserId}")
                    Text("No messages yet.")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectMessageScreenPreview() {
    TimewarpTheme {
        DirectMessageScreen(targetUserId = "user-id")
    }
}
