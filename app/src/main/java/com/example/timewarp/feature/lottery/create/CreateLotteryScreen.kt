
// Parts of this code were made with the assistance of Claude AI
// While no functionally was implemented wholly by the AI
// I prompted it with questions about how to do certain things in Kotlin/Android
// Or assistance in debugging

package com.example.timewarp.feature.lottery.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController


import com.example.timewarp.navigation.Screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Period
import com.example.timewarp.feature.lottery.form.LotteryForm
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.theme.PitchCream
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.TimewarpTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.ui.theme.TimewarpTheme


/**
 * [CreateLotteryScreen]

 */
@Composable
fun CreateLotteryScreen(
    modifier: Modifier = Modifier,
    campsiteId: String = "",
    screen: Screen = Screen.CreateLottery,
    navController: NavController = rememberNavController(),
    viewModel: CreateLotteryViewModel = hiltViewModel()
) {

    // State
    val state by viewModel.state.collectAsStateWithLifecycle()

    // State Interpretation & Navigation

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (val currentState = state) {

                CreateLotteryState.Loading -> {
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                            color = PitchCream,
                            trackColor = Color.Transparent,
                            strokeWidth = 4.dp,
                        )
                    }
                }
                is CreateLotteryState.Error -> Text("Error: ${currentState.message}")

                // Choose Campsites
                is CreateLotteryState.Content -> {
                    LotteryForm(
                        form = viewModel.form,
                        campsites = currentState.campsites,
                        onSubmit = { viewModel.createLotteryInDB { navController.popBackStack() }},
                        submitError = viewModel.createError,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateLotteryPreview() {
    TimewarpTheme() {
        CreateLotteryScreen(
            viewModel = hiltViewModel()
        )

    }
}