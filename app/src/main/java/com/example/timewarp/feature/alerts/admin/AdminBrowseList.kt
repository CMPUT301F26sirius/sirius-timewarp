package com.example.timewarp.feature.alerts.admin

import android.text.TextUtils.isEmpty
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