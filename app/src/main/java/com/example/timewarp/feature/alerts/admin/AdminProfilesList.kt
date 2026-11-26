package com.example.timewarp.feature.alerts.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdminProfileList(
    viewModel: AdminProfilesViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selected by remember { mutableStateOf<AdminProfile?>(null) }
    var toDelete by remember { mutableStateOf<AdminProfile?>(null) }

    AdminBrowseList(
        isLoading = state.isLoading,
        error = state.error,
        isEmpty = state.items.isEmpty(),
        listTag = "admin_profiles_list",
        emptyText = "No Profiles"
    ) {
        items(state.items, key = {it.userId}) {
            item ->
            Card(
                Modifier.fillMaxWidth().clickable{selected = item}
                    .testTag("admin_profile_item_${item.userId}")
            ) {
                Column(
                    Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        item.name.ifBlank { "(no name)" },
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        item.email,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        item.userType.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    selected?.let {
        item ->
        AlertDialog(
            onDismissRequest = {selected = null},
            title = { Text(item.name.ifBlank { "(no name)" })},
            text = {
                Column(
                    Modifier.testTag("admin_profile_detail"),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        "Role: ${item.userType.name}"
                    )

                    Text(
                        "Email: ${item.profile?.email.orEmpty()}"
                    )

                    Text(
                        "Phone: ${item.profile?.phoneNumber.orEmpty()}"
                    )

                    Text(
                        "Interests: ${item.profile?.interests?.
                        joinToString().orEmpty().ifBlank{"None"}}"
                    )

                    Text(
                        "User ID: ${item.userId}"
                    )

                    if (
                        item.userId != state.currentUserId
                    ) {
                        TextButton(
                            onClick = {toDelete = item},
                            modifier = Modifier.testTag("admin_delete_profile_${item.userId}")
                        ) {
                            Text("Delete Account")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {selected = null}
                ) {
                    Text(
                        "Close"
                    )
                }
            }
        )
    }

    toDelete?.let {
        item ->
        AlertDialog(
            onDismissRequest = {toDelete = null},
            title = {Text("Delete this account?")},
            text = {
                Text("Past reservations stay. Active reservations are cancelled")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteProfile(item.userId)
                        toDelete = null
                        selected = null
                    },
                    modifier = Modifier.testTag("confirm_admin_action")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {toDelete = null}
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}