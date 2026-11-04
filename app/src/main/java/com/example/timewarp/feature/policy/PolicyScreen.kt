package com.example.timewarp.feature.policy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun PolicyScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Policy,
    navController: NavController = rememberNavController(),
    viewModel: PolicyViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {
        when (val current = state) {
            PolicyState.Idle -> Unit
        }
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed id risus quis erat dapibus rhoncus. Praesent finibus, dolor ac ultricies condimentum, ligula magna placerat urna, eu posuere tellus est suscipit ligula. Pellentesque ultrices hendrerit ligula, vel lobortis ipsum sagittis et. Ut ut dictum purus. Proin euismod massa tortor, ut consectetur lorem ultricies et. Integer venenatis ac nibh dictum blandit. Etiam nec ipsum sollicitudin, posuere purus id, facilisis nulla. Quisque semper in urna a convallis. Mauris vestibulum ultricies velit, nec ullamcorper augue posuere a. Proin sed efficitur odio, sit amet laoreet orci. Integer ornare a eros non venenatis. Vestibulum malesuada lacinia euismod. Donec tincidunt consequat tellus. Mauris accumsan, mauris et lobortis vulputate, neque metus vulputate sapien, mattis mattis libero sapien eget sem. Donec ornare a nibh at congue.\n" +
                        "\n" +
                        "In ac faucibus justo. Nam sodales tempor dui, vitae elementum augue egestas sit amet. Nam ullamcorper consequat mauris, sit amet ornare massa condimentum quis. Etiam pretium velit vitae odio sodales egestas. Curabitur ultricies, justo et consectetur semper, arcu quam fermentum enim, id pulvinar felis elit sed elit. Suspendisse hendrerit dolor at dui ornare luctus. Pellentesque tempor, diam sit amet lacinia pellentesque, felis felis efficitur libero, at ultrices turpis nulla et ex.\n" +
                        "\n" +
                        "Pellentesque imperdiet gravida nunc, at ullamcorper ante gravida accumsan. Nunc vel pharetra quam, nec varius risus. Nulla euismod nibh a dignissim ullamcorper. Morbi ac faucibus lorem. Donec lorem odio, aliquam at viverra quis, ornare quis libero. Quisque elit orci, mattis nec euismod non, posuere vel tortor. Nulla varius sit amet quam id imperdiet. Nam bibendum, erat eget posuere luctus, ante massa maximus lectus, eu eleifend nunc metus non libero. Duis tempor non nisl vitae condimentum. Sed lacinia commodo magna sed auctor. In finibus malesuada laoreet. Suspendisse lobortis purus a viverra dictum. Phasellus commodo ante velit, quis egestas velit cursus ac."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TimewarpTheme() {
        PolicyScreen()
    }
}