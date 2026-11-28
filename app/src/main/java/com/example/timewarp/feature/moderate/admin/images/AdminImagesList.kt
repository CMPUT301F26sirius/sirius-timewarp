package com.example.timewarp.feature.moderate.admin.images

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timewarp.domain.model.Image
import com.example.timewarp.feature.moderate.admin.AdminBrowseList
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseButtonType
import com.example.timewarp.ui.theme.PitchForest
import java.nio.file.WatchEvent

/**
 * This function will display the list of uploaded images for the admin moderation.
 * The admin can delete images from moderate image tab
 *
 * @param viewModel
 */
@Composable
fun AdminImagesList(
    viewModel: AdminImagesViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    val images = (state as? AdminImages.Content)?.images.orEmpty()
    var pendingDelete by remember { mutableStateOf<Image?>(null) }
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    var confirmDeleteSelected by remember { mutableStateOf(false) }

    AdminBrowseList(
        isLoading = state is AdminImages.Loading,
        error = (state as? AdminImages.Error)?.message,
        isEmpty = images.isEmpty(),
        listTag = "admin_images_list",
        emptyText = "No Images Found"
    ) {
        // This shows a select and unselect button only when at least one or all the images are selected
        if (images.isNotEmpty()) {
            item {
                val allSelected = images.all { it.imageId in selected }
                TextButton(
                    onClick = {
                        viewModel.toggleSelectAll(images.map { it.imageId })
                    },
                    modifier = Modifier.testTag("admin_images_select_all")
                ) {
                    Text(
                        if (allSelected) "Unselect all"
                        else
                        "Select all"
                    )
                }
            }
        }

        // This shows a delete action only when one or more items are selected
        if (selected.isNotEmpty()) {
            item {
                BaseButton(
                    type = BaseButtonType.Critical,
                    text = "Delete selected (${selected.size})",
                    modifier = Modifier.fillMaxWidth().testTag("admin_images_delete_selected"),
                    onClick = {confirmDeleteSelected = true}
                )
            }
        }
        items(
            images,
            key = {it.imageId}
        ) {
            image ->
            Card(
                Modifier.fillMaxWidth().testTag("admin_image_item_${image.imageId}"),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = image.imageId in selected,
                        onCheckedChange = {viewModel.toggleSelected(image.imageId)},
                        modifier = Modifier.testTag("admin_image_select${image.imageId}")
                    )
                    Column(
                        Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            "Image ${image.imageId.take(8)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = PitchForest
                        )

                        Text(
                            "Uploaded by: ${image.uploadingUserId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PitchForest
                        )
                    }

                    IconButton(
                        onClick = {pendingDelete = image},
                        modifier = Modifier.testTag("admin_image_delete_${image.imageId}")
                    ) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "Delte Image"
                        )
                    }
                }
            }
        }
    }
    // This will show a confirmation screen when an image is selected to delete it
    pendingDelete?.let {
        image ->
        AlertDialog(
            onDismissRequest = {pendingDelete = null},
            title = {Text("Are you sure?")},
            text = {Text("This will permanently delete the image")},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteImages(image.imageId)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_admin_action")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {pendingDelete = null}
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    // This will let you select multiple images to delete.
    // will show a confirmation screen when multiple images are selected to delete
    if(confirmDeleteSelected) {
        AlertDialog(
            onDismissRequest = {confirmDeleteSelected = false},
            title = {Text("Are your sure?")},
            text = {Text("This will delete ${selected.size} images permanently")},
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelected()
                        confirmDeleteSelected = false
                    },
                    modifier = Modifier.testTag("confirm_delete_selected")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {confirmDeleteSelected = false}
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}