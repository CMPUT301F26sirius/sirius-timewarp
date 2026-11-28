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
import com.example.timewarp.ui.theme.PitchForest

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

    AdminBrowseList(
        isLoading = state is AdminImages.Loading,
        error = (state as? AdminImages.Error)?.message,
        isEmpty = images.isEmpty(),
        listTag = "admin_images_list",
        emptyText = "No Images Found"
    ) {
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
}