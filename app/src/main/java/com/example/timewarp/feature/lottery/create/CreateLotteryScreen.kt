package com.example.timewarp.feature.lottery.create

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

import com.example.timewarp.navigation.Screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.PitchSage

@Composable
fun CreateLotteryScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.CreateLottery,
    navController: NavController = rememberNavController(),
    viewModel: CreateLotteryViewModel = hiltViewModel()
) {

    // TODO: IMPLEMENT
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    NavigationScreen (
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            when (val currentState = state) {

                CreateLotteryState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center as Alignment.Horizontal),
                        color = PitchCream,
                        trackColor = Color.Transparent,
                        strokeWidth = 4.dp,
                    )
                }
                is CreateLotteryState.Error -> Text("Error: ${currentState.message}")

                is CreateLotteryState.Content -> {

                }
            }
        }
    }
}

@Composable
private fun CampsiteCard (
    campsite: Campsite,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,

) {
    Card (
        colors = if (selected) {
            CardDefaults.cardColors(PitchSage)
        } else {
            CardDefaults.cardColors(PitchCream)
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { onToggle()}
                )
                .padding(12.dp)
        ) {
            
        }
    }
}