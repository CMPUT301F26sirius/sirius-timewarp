package com.example.timewarp.feature.moderate

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
import com.example.timewarp.feature.browse.BrowseContent
import com.example.timewarp.feature.browse.BrowseViewModel
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun ModerateScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Moderate,
    navController: NavController = rememberNavController(),
    viewModel: ModerateViewModel = hiltViewModel(),
    browseViewModel: BrowseViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val browseState by browseViewModel.state.collectAsStateWithLifecycle()

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        BrowseContent(
            state = browseState,
            onCampsiteClick = {
                id -> navController.navigate(Screen.CampsiteDetails.createRoute(id))
            },
            query = browseState.filters.query,
            onSearch = browseViewModel::onSearch,
            onFilterClick = {},
            showFilterButton = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ModerateScreenPreview() {
    TimewarpTheme() {
        ModerateScreen()
    }
}