package com.example.timewarp.feature.profile.update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseButtonType
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.theme.TimewarpTheme
/**
 * Screen where we connect UpdateProfileViewModel to the UI. It collects
 * the view model's state, shows a loading message, an error, or updateProfileForm
 * to match. Returns to previous screen once save is successful.
 *
 * @param modifier Modifier applied to the surrounding (NavigationScreen)
 * @param screen Navigation destination for this screen, used for the title and bottom bar
 * @param navController Controller used to go back if either saved or canceled update
 * @param viewModel Holds the state of the screen and loads and saves profile. (provided by\hilt)
 */

@Composable
fun UpdateProfileScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.UpdateProfile,
    navController: NavController = rememberNavController(),
    viewModel: UpdateProfileViewModel = hiltViewModel()
) {

    // State
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // State interpretation and navigation
    val isSaved = (state as? UpdateProfileState.Content)?.isSaved == true
    LaunchedEffect(isSaved) {
        if (isSaved) {
            navController.popBackStack()
        }
    }

    // UI
    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController,
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            when (val currentState = state) {
                UpdateProfileState.Loading -> Text("Loading profile...")
                is UpdateProfileState.Error -> Text("Error: ${currentState.message}")

                is UpdateProfileState.Content -> UpdateProfileForm(
                    state = currentState,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPhoneNumberChange = viewModel::onPhoneNumberChange,
                    onSave = viewModel::save,
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
/**
 * Displays the current fields, as well as placeholder for things such as
 * interests, and photos. Reports user action through callback.
 * This function holds no profile data itself. Designed this way so that we could
 * preview and test with any state. Also use internal so we can do testing
 * @param state Current state
 * @param onFirstNameChange Called when new text
 * @param onLastNameChange Called when new text
 * @param onEmailChange Called when new text
 * @param onPhoneNumberChange Called when new text
 * @param onSave Called when update is pressed
 * @param onCancel Called when cancel is pressed
 */
@Composable
internal fun UpdateProfileForm(
    state: UpdateProfileState.Content,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
){

    // Not part of Profile yet, so kept on screen only
    val interestOptions = listOf("Camping", "Browsing", "Hiking", "Other")
    var selectedInterests by remember { mutableStateOf(setOf<String>())}

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Details
        Text("Enter your details", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // BaseInput's Text type ignores `modifier`, so weight goes on a wrapper
            Box(
                modifier = Modifier
                .weight(1f)
                .testTag("updateFirstName")
            ) {
                BaseInput(
                    label = "First Name",
                    value = state.firstName,
                    onValueChange = onFirstNameChange,
                    type = BaseInputType.Text(minChars = 1, maxChars = 30),
                    placeholder = "John"
                )
            }
            Box(
                modifier = Modifier
                .weight(1f)
                .testTag("updateLastName")
            ) {
                BaseInput(
                    label = "Last Name",
                    value = state.lastName,
                    onValueChange = onLastNameChange,
                    type = BaseInputType.Text(minChars = 1, maxChars = 30),
                    placeholder = "Doe"
                )
            }
        }
        Box(modifier = Modifier.testTag("updateEmail")) {
            BaseInput(
                label = "Email",
                value = state.email,
                onValueChange = onEmailChange,
                type = BaseInputType.Text(maxChars = 100),
                placeholder = "johndoe@gmail.com"
            )
        }

        Box(modifier = Modifier.testTag("updatePhoneNumber")) {
            BaseInput(
                label = "Phone",
                value = state.phoneNumber,
                onValueChange = onPhoneNumberChange,
                type = BaseInputType.Text(minChars = 10, maxChars = 15),
                placeholder = "+1 (587) 000-0000"
            )
        }

        // Photo
        Text("Update photo", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(120.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable { /* open image picker */ }
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Interests
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("What are your interests?", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${selectedInterests.size} selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        interestOptions.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { option ->
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        var value by remember { mutableStateOf("False")}
                        BaseInput(
                            label = option,
                            value = value,
                            onValueChange = { newValue ->
                                if (newValue.toBoolean()) {
                                    selectedInterests + option
                                } else {
                                    selectedInterests - option
                                }
                                value = newValue
                            },
                            type = BaseInputType.Boolean,
                        )
                    }
//                    InterestOption(
//                        text = option,
//                        checked = option in selectedInterests,
//                        onToggle = {
//                            selectedInterests =
//                                if (option in selectedInterests) selectedInterests - option
//                                else selectedInterests + option
//                        },
//                        modifier = Modifier.weight(1f)
//                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        // Actions
        state.saveError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        BaseButton(
            text = if (state.isSaving) "Saving..." else "Update",
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("updateButton"),
        )

        BaseButton(
            type = BaseButtonType.Outlined,
            text = "Cancel",
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cancelButton")
        )
    }

}

/**
 * Used to preview layout without running. So no need for firebase or hilt.
 * Information provided simply for previewing the layout.
 */
@Preview(showBackground = true)
@Composable
fun UpdateProfilePreview() {
    TimewarpTheme {
        UpdateProfileForm(
            state = UpdateProfileState.Content(
                firstName = "John",
                lastName = "Doe",
                email = "johndoe@gmail.com",
                phoneNumber = "5870000000"
            ),
            onFirstNameChange = {},
            onLastNameChange = {},
            onEmailChange = {},
            onPhoneNumberChange = {},
            onSave = {},
            onCancel = {}
        )
    }
}
