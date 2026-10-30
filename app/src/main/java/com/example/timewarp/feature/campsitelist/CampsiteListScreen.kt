package com.example.timewarp.feature.campsitelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CampsiteListScreen(
    modifier: Modifier = Modifier,
    viewModel: CampsiteListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(15.dp)
        ) {
            items(state.campsites, key ={ it.id }) {
                campsite -> CampsiteSummary(campsite = campsite)
            }
        }
    }
}