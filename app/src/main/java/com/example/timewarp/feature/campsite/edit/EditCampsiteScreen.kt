package com.example.timewarp.feature.campsite.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.campsite.create.CreateCampsiteForm
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton

@Composable
fun EditCampsiteScreen(
    campsiteId: String,
    modifier: Modifier = Modifier,
    screen: Screen = Screen.EditCampsite,
    navController: NavController = rememberNavController(),
    viewModel: EditCampsiteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val savedId = (state as? EditCampsiteState.Ready)?.draft?.savedCampsiteId

    LaunchedEffect(campsiteId) {
        viewModel.observe(campsiteId)
    }

    LaunchedEffect(savedId) {
        val id = savedId ?: return@LaunchedEffect
        val detailsRoute = Screen.CampsiteDetails.createRoute(id)
        if (!navController.popBackStack(detailsRoute, inclusive = false)) {
            navController.navigate(detailsRoute) {
                popUpTo("${Screen.EditCampsite.route}/{id}") { inclusive = true }
            }
        }
    }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        when (val current = state) {
            EditCampsiteState.Loading -> CircularProgressIndicator()

            is EditCampsiteState.Error -> Column {
                Text(current.message)
                BaseButton(
                    text = "Try again",
                    onClick = { viewModel.observe(campsiteId) }
                )
                BaseButton(
                    text = "Back",
                    onClick = { navController.popBackStack() }
                )
            }

            is EditCampsiteState.Ready -> CreateCampsiteForm(
                state = current.draft,
                onChange = viewModel::updateDraft,
                onRegister = viewModel::save,
                onCancel = { navController.popBackStack() },
                isEditing = true
            )
        }
    }
}