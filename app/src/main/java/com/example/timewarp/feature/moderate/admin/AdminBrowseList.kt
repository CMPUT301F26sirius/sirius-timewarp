package com.example.timewarp.feature.moderate.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text

/**
 * @param isLoading shows if the data is being loaded. Shows a progress inidcator in center
 * @param error error message when loading fails
 * @param emptyText when there is no items in the list to display it shows a message
 * @param listTag test tag for UI tests
 * @param isEmpty whether the dataset has any items to show or no
 * @param modifier Modifier applied to the Box
 */
@Composable
fun AdminBrowseList(
    isLoading: Boolean,
    error: String?,
    emptyText: String,
    listTag: String,
    isEmpty: Boolean,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit
) {
    Box(
        modifier.fillMaxSize()
    ) {
        when {
            isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            error != null -> Text(error, Modifier.align(Alignment.Center))
            isEmpty -> Text(emptyText, Modifier.align(Alignment.Center))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().testTag(listTag),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                content = content
            )

        }
    }
}