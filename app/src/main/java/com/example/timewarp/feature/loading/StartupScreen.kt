package com.example.timewarp.feature.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.R
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme
import com.example.timewarp.ui.theme.BrandGradient
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.PitchForest

@Composable
fun StartupScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: StartupViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // State Interpretation & Navigation
    LaunchedEffect(state) {
        when (val current = state) {
            StartupState.Loading -> Unit

            StartupState.Registration -> {
                navController.navigate(Screen.Registration.route) {
                    popUpTo(Screen.Startup.route) {
                        inclusive = true
                    }
                }
            }

            is StartupState.Home -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = true
                    }
                }
            }

            StartupState.ProfileSetup -> {
                navController.navigate(Screen.Registration.route) {
                    popUpTo(Screen.Registration.route) {
                        inclusive = true
                    }
                }
            }

            is StartupState.Error -> {
                // Show an error and provide a retry action.
            }
        }
    }

    if (state is StartupState.Home) {
        Text("goon")
    }

    // User Interface
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        // Background
        Image(
            painter = painterResource(R.drawable.nordegg),
            contentDescription = "Example campsite",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )

        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = { drawRect(BrandGradient) }
        )

        // Brand container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Brand alignment
            Column(
                verticalArrangement =  Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Terrain,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "pitch",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Spinner
                CircularProgressIndicator(
                    modifier = modifier.size(24.dp).align(alignment = Alignment.CenterHorizontally),
                    color = PitchCream,
                    trackColor = PitchForest,
                    strokeWidth = 4.dp,
                )

                // Brand Promise
                Column(
                    verticalArrangement =  Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                ) {
                    Text(
                        text = "A LITTLE CLOSER TO OUTSIDE",
                        color = Color.White,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = "Find your place.\nLeave the noise.",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 36.sp * 1.06
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartupScreenPreview() {
    TimewarpTheme() {
        StartupScreen(
            viewModel = hiltViewModel()
        )
    }
}