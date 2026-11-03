package com.example.timewarp.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.theme.PitchLink
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.Account,
    navController: NavController = rememberNavController(),
    viewModel: AccountViewModel = hiltViewModel(),
) {
    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State Interpretation & Navigation
    LaunchedEffect(state) {

    }

    // String building
    val termsString = buildAnnotatedString {
        append("By using our app, you agree to our ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms_and_conditions",
                styles = TextLinkStyles(style = SpanStyle(color = PitchLink)),
                linkInteractionListener = {
                    navController.navigate(Screen.Policy.route);
                }
            )
        ) {
            append("Terms & Privacy Policy")
        }
        append(".")
    }

    // User Interface
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BaseButton(
                text = "Update profile",
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate(Screen.UpdateProfile.route)
                }
            )
            BaseButton(
                text = "Delete profile",
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            )
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

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    TimewarpTheme() {
        AccountScreen()
    }
}