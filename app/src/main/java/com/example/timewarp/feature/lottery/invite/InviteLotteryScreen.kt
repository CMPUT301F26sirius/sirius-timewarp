package com.example.timewarp.feature.lottery.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.lottery.form.LotteryForm
import com.example.timewarp.navigation.Screen
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.TimewarpTheme


/**
 * [InviteLotteryScreen]
 */
@Composable
fun InviteLotteryScreen(
    lotteryId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.InviteLottery,
    navController: NavController = rememberNavController(),
    viewModel: InviteLotteryViewModel = hiltViewModel()
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
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {

    }
}

/**
 * [InviteLotteryContent]
 */
@Composable
fun InviteLotteryContent() {

}

@Preview(showBackground = true)
@Composable
fun EditLotteryPreview() {
    TimewarpTheme() {
        InviteLotteryContent(

        )
    }
}