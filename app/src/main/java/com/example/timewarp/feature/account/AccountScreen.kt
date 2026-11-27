package com.example.timewarp.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseButtonType
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.theme.PitchLink
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Account,
    navController: NavController = rememberNavController(),
    viewModel: AccountViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isUpdatingNotifications by viewModel.isUpdatingNotifications.collectAsStateWithLifecycle()
    val isDeleting by viewModel.isDeleting.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var showDelete by rememberSaveable { mutableStateOf(false) }
    // State Interpretation & Navigation
    LaunchedEffect(state) {
        when (state) {

            is AccountState.Deleted -> navController.navigate(Screen.Startup.route) {
                popUpTo(navController.graph.id) {inclusive = true}
            }

            is AccountState.Error -> showDelete = false
            is AccountState.Idle -> Unit

        }
    }

    // String building
    val termsString = buildAnnotatedString {
        append("By using our app, you agree to our ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms_and_conditions",
                styles = TextLinkStyles(style = SpanStyle(color = PitchLink)),
                linkInteractionListener = {
                    navController.navigate(Screen.Policy.route);
                }
            )
        ) {
            append("Terms & Privacy Policy")
        }
        append(".")
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            currentUser?.let { user ->
                BaseInput(
                    label = if (user.notificationsEnabled) {
                        "Notifications: On"
                    } else {
                        "Notifications: Off"
                    },
                    value = user.notificationsEnabled.toString(),
                    onValueChange = { enabled ->
                        viewModel.setNotificationsEnabled(enabled.toBoolean())
                    },
                    type = BaseInputType.Boolean,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUpdatingNotifications,
                )
            }
            BaseButton(
                text = "Update profile",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(Screen.UpdateProfile.route)
                }
            )
            BaseButton(
                type = BaseButtonType.Critical,
                text = "Delete profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("delete_account"),
                onClick = { showDelete = true }
            )
            Text(
                text = termsString,
                style = MaterialTheme.typography.bodySmall,
                color = PitchMutedText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showDelete) {
        DeleteAccount(
            isDeleting = isDeleting,
            onConfirm = viewModel::deleteAccount,
            onCancel = { showDelete = false }
        )
    }

}

/**
 * Responsible for asking the user to confirm the deletion of their account.
 *
 * @param isDeleting True while deleting
 * @param onConfirm called when user taps delete
 * @param onCancel called when the user taps cancel
 */
@Composable
fun DeleteAccount(
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onCancel() },
        modifier = Modifier.testTag("delete_dialog"),
        title = { Text("Are you sure you want to delete your profile?") },
        text = { Text(
                text = "Deleting your profile removes your active reservations and waitlist " +
                        "spots, and you'll lose access to this account. This can not be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = PitchMutedText,
            )
        },
        confirmButton = {
            BaseButton(
                type = BaseButtonType.Critical,
                text = if (isDeleting) "Deleting..." else "Delete",
                enabled = !isDeleting,
                modifier = Modifier.testTag("delete_confirm"),
                onClick = onConfirm,
            )
        },
        dismissButton = {
            BaseButton(
                text = "Cancel",
                enabled = !isDeleting,
                modifier = Modifier.testTag("delete_cancel"),
                onClick = onCancel,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    TimewarpTheme() {
        AccountScreen()
    }
}