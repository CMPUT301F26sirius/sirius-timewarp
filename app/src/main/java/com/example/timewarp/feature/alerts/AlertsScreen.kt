package com.example.timewarp.feature.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.alerts.admin.AdminSelectTab
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.theme.TimewarpTheme

/**
 * This function displays an "Admin" tab for the user whose role is admin under the alerts button
 *
 * @param modifier Modifier applied to the Navigation Screen
 * @param screen navigation destination for selection state
 * @param navController used for screen level navigation actions
 */
@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Alerts,
    navController: NavController = rememberNavController(),
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isAdmin) {
                TabRow(
                    selectedTabIndex = tab,
                    modifier = Modifier.clip(RoundedCornerShape(15.dp))
                ) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("Alerts") },
                        modifier = Modifier.testTag("alerts_tab_alerts")
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("Admin") },
                        modifier = Modifier.testTag("alerts_tab_admin")
                    )
                }
            }
            if (isAdmin && tab == 1) {
                AdminSelectTab()
            } else {
                // This will be replaced later with actual alerts
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Alerts So Far")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    TimewarpTheme() {
        AlertsScreen()
    }
}