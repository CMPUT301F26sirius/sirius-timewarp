package com.example.timewarp.feature.campsite.edit

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme


/**
 * [EditCampsiteScreen]
 */
@Composable
fun EditCampsiteScreen(
    campsiteId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.EditCampsite,
    navController: NavController = rememberNavController(),
    viewModel: EditCampsiteViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(campsiteId) {
        viewModel.observe(campsiteId)
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {

    }
}

@Preview(showBackground = true)
@Composable
fun EditCampsitePreview() {
    TimewarpTheme() {
        EditCampsiteScreen(
            campsiteId = "0",
            viewModel = hiltViewModel()
        )
    }
}