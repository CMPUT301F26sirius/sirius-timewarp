package com.example.timewarp.feature.registration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.ui.component.IconButton
import com.example.timewarp.R
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.component.IconPill
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme
import com.example.timewarp.ui.theme.BrandGradient
import com.example.timewarp.ui.theme.PitchLink

@Composable
fun RegistrationScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Registration,
    navController: NavController = rememberNavController(),
    viewModel: RegistrationViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {
        when (val current = state) {
            RegistrationState.Idle -> Unit

            RegistrationState.Loading -> Unit

            is RegistrationState.Success -> {
                navController.navigate(Screen.Browse.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = false
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            }

            is RegistrationState.Error -> {
                // Show an error and provide a retry action.
            }
        }
    }

    // User Interface
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        // string building
        val termsString = buildAnnotatedString {
            append("By continuing, you agree to our ")
            withLink(
                LinkAnnotation.Clickable(
                    tag = "terms_and_conditions",
                    styles = TextLinkStyles(style = SpanStyle(color = PitchLink)),
                    linkInteractionListener = {}
                )
            ) {
                append("Terms & Privacy Policy")
            }
            append(".")
        }

        // Remember
        var userType by remember {mutableStateOf("Camper")}
        var adminCode by remember {mutableStateOf("")}

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header image and Text
            Box(
                modifier = Modifier
                    .height(340.dp)
            ) {
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

            // Content
            Column(
                verticalArrangement =  Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Your device section
                Column(
                    verticalArrangement =  Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconPill(
                        onClick = {},
                        text = "YOUR DEVICE IS YOUR KEY",
                        icon = Icons.Outlined.Key,
                    )
                    Text(
                        text = "More outdoors.\nFewer passwords.",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Register this device to save campsites, book a stay and share your experience. No username. No password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PitchMutedText
                    )
                }

                // Input user type
                Column(
                    verticalArrangement =  Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    BaseInput(
                        label = "What kind of user are you?",
                        value = userType,
                        placeholder = "",
                        onValueChange = {userType = it},
                        type = BaseInputType.Dropdown(listOf<String>("Camper", "Campsite Owner", "Admin"))
                    )
                    if (userType == "Admin") {
                        BaseInput(
                            label = "Please enter the admin code",
                            value = adminCode,
                            placeholder = "",
                            onValueChange = {adminCode = it},
                            type = BaseInputType.Text(4, 20)
                        )
                    }
                }

                // Registering
                Column(
                    verticalArrangement =  Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (userType == "Admin" && adminCode == "1234") {
                                // Very secure :thumbs_up:
                                viewModel.register(UserType.Admin)
                            } else if (userType == "Camper") {
                                viewModel.register(UserType.Entrant)
                            } else if (userType == "Campsite Owner") {
                                viewModel.register(UserType.CampsiteOwner)
                            }
                        },
                        icon = Icons.Outlined.VerifiedUser,
                        text = "Register this device",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Next time, pitch will authenticate this device automatically. No username, no password, no hassle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PitchMutedText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = termsString,
                    style = MaterialTheme.typography.bodySmall,
                    color = PitchMutedText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    TimewarpTheme() {
        RegistrationScreen()
    }
}