package com.example.timewarp.feature.lottery.waitlist

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
import com.example.timewarp.feature.lottery.waitlist.WaitlistViewModel
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * [WaitlistScreen]
 */
@Composable
fun WaitlistScreen(
    lotteryId: String,
    modifier: Modifier = Modifier,
    // screen: Screen = Screen.Waitlist,
    navController: NavController = rememberNavController(),
    viewModel: WaitlistViewModel = hiltViewModel()
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(lotteryId) {
        viewModel.observe(lotteryId)
    }

    // User Interface
    NavigationScreen(
        // screen = screen,
        modifier = modifier,
        navController = navController,
    ) {

    }
}

@Preview(showBackground = true)
@Composable
fun EditLotteryPreview() {
    TimewarpTheme() {
        WaitlistScreen(
            lotteryId = "0",
            viewModel = hiltViewModel()
        )
    }
}