package com.example.timewarp.feature.reservationconfirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ReservationConfirmationScreen(
    modifier: Modifier = Modifier,
    viewModel: ReservationConfirmationViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirmation = state.confirmation

    if (state.isLoading) {
        CircularProgressIndicator()
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(15.dp)
        ) {
            item {
                if (confirmation != null) {
                    ReservationSummary(confirmation)
                }
            }
            item {
                if (confirmation != null) {
                    ReservationCSVButton(confirmation)
                }
            }
        }
    }
}

