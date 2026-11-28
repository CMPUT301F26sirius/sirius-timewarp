package com.example.timewarp.feature.notifications

import android.crypto.hpke.Recipient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * [NotificationsScreen]
 */
@Composable
fun NotificationsScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Notifications,
    navController: NavController = rememberNavController(),
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf<NotificationsTab>(NotificationsTab.Incoming) }

    LaunchedEffect(viewModel) {
        viewModel.observe()
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        when (val s = state) {
            is NotificationsState.Idle -> Text("Loading...")
            is NotificationsState.Error -> Text("Error: ${s.message}")
            is NotificationsState.Content -> NotificationsContent(
                incomingNotifications = s.incomingNotifications,
                outgoingNotifications = s.outgoingNotifications,
                allNotifications = s.allNotifications,
                tabs = s.tabs,
                usersNames = s.usersNames,
                selectedTab = if (selectedTab in s.tabs) selectedTab else s.tabs.first(),
                onTabSelected = { selectedTab = it }
            )
        }
    }
}

/**
 * Display Content for Notification Screen
 *
 * @param incomingNotifications List of User's Incoming Notifications
 * @param outgoingNotifications List of User's Outgoing Notifications
 * @param allNotifications List of All Notifications (if user is admin, otherwise empty)
 * @param usersNames Names of users in Notifications
 * @param tabs List of Tabs user has access to
 * @param selectedTab Currently selected Tab
 * @param onTabSelected Switch to tab when clicked
 */
@Composable
fun NotificationsContent(
    incomingNotifications: MutableList<Notification>?,
    outgoingNotifications: MutableList<Notification>?,
    allNotifications: MutableList<Notification>?,
    usersNames: Map<String, String>,
    tabs: List<NotificationsTab>,
    selectedTab: NotificationsTab,
    onTabSelected: (NotificationsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {

        if(tabs.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEach {
                    tab ->
                    FilterChip(
                        selected = tab == selectedTab,
                        onClick = {onTabSelected(tab)},
                        label = {Text(tab.label)},
                        modifier = Modifier.testTag("notification_tab_${tab.key}")
                    )
                }
            }
        }

        when (selectedTab) {
            is NotificationsTab.Incoming -> NotificationsList(incomingNotifications, usersNames)
            is NotificationsTab.Outgoing -> NotificationsList(outgoingNotifications, usersNames)
            is NotificationsTab.AllNotifications -> NotificationsList(allNotifications, usersNames)
        }

    }
}

/**
 * Display List of Notifications
 *
 * @param notifications list of notifications to display
 * @param usersNames names of users tied to notification
 */
@Composable
private fun NotificationsList(
    notifications: MutableList<Notification>?,
    usersNames: Map<String, String>,
    modifier: Modifier = Modifier
) {
    if (notifications.isNullOrEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No Notifications to Show",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = notifications,
            ) { notification ->
                NotificationEntry(
                    notification = notification,
                    senderName = usersNames[notification.fromUserId] ?: "(no name)",
                    recipientName = usersNames[notification.toUserId] ?: "(no name)"
                )
            }
        }
    }
}

/**
 * Display a single entry of a notification
 * @param notification Notification to display
 * @param senderName Name of Sender
 * @param recipientName Name of Recipient
 */
@Composable
private fun NotificationEntry(
    notification: Notification,
    senderName: String,
    recipientName: String,
    modifier: Modifier= Modifier
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = notification.notification,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Sender: $senderName",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Recipient: $recipientName",
                    style = MaterialTheme.typography.bodySmall
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    TimewarpTheme {
        NotificationsScreen()
    }
}
