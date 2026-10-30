package com.example.timewarp.feature.campsite.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.ui.template.MainPage
import com.example.timewarp.ui.template.SubPage
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun CampsiteDetailsScreen(
    campsiteId: String,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: CampsiteDetailsViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // User Interface
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            SubPage(
                title = "A good day starts\noutside.",
                subtitle = "PITCH/CAMPSITE/DETAILS",
                navController = navController,
            ) {
                // Other stuff
                Text(campsiteId)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CampsiteDetailsPreview() {
    TimewarpTheme() {
        CampsiteDetailsScreen("0")
    }
}