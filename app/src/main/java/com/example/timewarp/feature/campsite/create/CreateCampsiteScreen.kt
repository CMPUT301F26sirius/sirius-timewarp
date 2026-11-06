package com.example.timewarp.feature.campsite.create

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
 * [CreateCampsiteScreen]
 */
@Composable
fun CreateCampsiteScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.CreateCampsite,
    navController: NavController = rememberNavController(),
    viewModel: CreateCampsiteViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation

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
fun CreateCampsitePreview() {
    TimewarpTheme() {
        CreateCampsiteScreen(
            viewModel = hiltViewModel()
        )
    }
}