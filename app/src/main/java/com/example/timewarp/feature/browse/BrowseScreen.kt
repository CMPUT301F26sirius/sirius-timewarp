package com.example.timewarp.feature.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.IconButton
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun BrowseScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Browse,
    navController: NavController = rememberNavController(),
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        IconButton(
            text = "View Campsite Details",
            icon = Icons.Outlined.Terrain,
            onClick = {
                navController.navigate(Screen.CampsiteDetails.createRoute("0"))
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BrowseScreenPreview() {
    TimewarpTheme() {
        BrowseScreen()
    }
}

