package com.example.timewarp.feature.campsite.detail

import androidx.hilt.navigation.compose.hiltViewModel



import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.timewarp.domain.model.Campsite


import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@Composable
fun CampsiteDetailScreen(
    viewModel: CampsiteDetailViewModel = hiltViewModel()
) {
    val campsite = viewModel.campsite

    Column {
        Text(text = campsite.name)

        Text(text = campsite.description)

        Button(onClick = {
            // Edit functionality later
        }) {
            Text("Edit Campsite")
        }
    }
}