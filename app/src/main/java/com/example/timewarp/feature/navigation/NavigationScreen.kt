package com.example.timewarp.feature.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BottomBar

@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Registration,
    navController: NavController = rememberNavController(),
    viewModel: NavigationViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {}
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigationItems by viewModel.navigationItems.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Navigation
    var isNavigatingBack by remember { mutableStateOf(false) }
    val canGoBack = navController.previousBackStackEntry != null && screen !in navigationItems

    // User Interface
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (navigationItems.isNotEmpty() && !canGoBack) {
                BottomBar(
                    items = navigationItems,
                    navController = navController
                )
            }
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Title
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Back button
                if (canGoBack) {
                    IconButton(
                        enabled = !isNavigatingBack,
                        onClick = {
                            if (!isNavigatingBack) {
                                isNavigatingBack = true
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }

                // Title Stuff
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "PITCH/${screen.route.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = screen.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}