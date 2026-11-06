package com.example.timewarp.feature.campsite.register

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.R
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.component.BaseButtonType
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun RegisterCampsiteScreen(
    modifier: Modifier = Modifier,
    screen: Screen = Screen.RegisterCampsite,
    navController: NavController = rememberNavController(),
    viewModel: RegisterCampsiteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedCampsiteId) {
        state.savedCampsiteId?.let { campsiteId ->
            navController.navigate(Screen.CampsiteDetails.createRoute(campsiteId)) {
                popUpTo(Screen.RegisterCampsite.route) { inclusive = true }
            }
        }
    }

    NavigationScreen(modifier = modifier, screen = screen, navController = navController) {
        RegisterCampsiteForm(
            state = state,
            onChange = viewModel::updateDraft,
            onRegister = viewModel::register,
            onCancel = { navController.popBackStack() }
        )
    }
}

/** Stateless form kept separate from navigation and Firebase for previews and UI tests. */
@Composable
internal fun RegisterCampsiteForm(
    state: RegisterCampsiteState,
    onChange: (RegisterCampsiteState) -> Unit,
    onRegister: () -> Unit,
    onCancel: () -> Unit
) {
    val errors = if (state.showErrors) state.fieldErrors else emptyMap()
    val context = LocalContext.current
    var tagDraft by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Default campsite image
        SectionTitle("Set the scene")
        Image(
            painter = painterResource(R.drawable.maligne_lake),
            contentDescription = "Campsite photo",
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            contentScale = ContentScale.Crop
        )
        // 2. Name, location, description, and guest limits
        SectionTitle("About your place")
        RegistrationField("Campsite name *", "name", state.name, { value ->
            onChange(state.copy(name = value))
        }, state.isSaving, errors["name"])
        RegistrationField("Location *", "address", state.address, { value ->
            onChange(state.copy(address = value))
        }, state.isSaving, errors["address"])
        if (state.address.isNotBlank()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable(enabled = !state.isSaving) {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(state.address)}")
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                }
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    Text("View location on map")
                }
            }
        }
        RegistrationField("Description *", "description", state.description, { value ->
            onChange(state.copy(description = value))
        }, state.isSaving, errors["description"], multiline = true)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                RegistrationField("Max guests / pitch *", "maxGuests", state.maxGuests, { value ->
                    onChange(state.copy(maxGuests = value))
                }, state.isSaving, errors["maxGuests"], keyboardType = KeyboardType.Number)
            }
            Column(Modifier.weight(1f)) {
                RegistrationField("Minimum stay (nights)", "minimumStayNights", state.minimumStayNights, { value ->
                    onChange(state.copy(minimumStayNights = value))
                }, state.isSaving, errors["minimumStayNights"], keyboardType = KeyboardType.Number)
            }
        }
        // 3. Rates and available dates
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionTitle("Rates & availability")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                RegistrationField("Per night · CAD *", "costPerNight", state.costPerNight, { value ->
                    onChange(state.copy(costPerNight = value))
                }, state.isSaving, errors["costPerNight"], keyboardType = KeyboardType.Decimal)
            }
            Column(Modifier.weight(1f)) {
                RegistrationField("Preparation fee", "preparationFee", state.preparationFee, { value ->
                    onChange(state.copy(preparationFee = value))
                }, state.isSaving, errors["preparationFee"], keyboardType = KeyboardType.Decimal)
            }
        }
        Text("Available dates (optional)", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                RegistrationField("From (YYYY-MM-DD)", "startDate", state.startDate, { value ->
                    onChange(state.copy(startDate = value))
                }, state.isSaving)
            }
            Column(Modifier.weight(1f)) {
                RegistrationField("Until (YYYY-MM-DD)", "endDate", state.endDate, { value ->
                    onChange(state.copy(endDate = value))
                }, state.isSaving)
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { index, label ->
                val day = index + 1
                FilterChip(
                    selected = day in state.daysOfWeek,
                    enabled = !state.isSaving,
                    label = { Text(label) },
                    onClick = {
                        val days = if (day in state.daysOfWeek) state.daysOfWeek - day else state.daysOfWeek + day
                        onChange(state.copy(daysOfWeek = days))
                    }
                )
            }
        }
        errors["availability"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        // 4. Amenities and tags
        val selectedAmenities = state.amenities
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("What’s here?", Modifier.weight(1f))
            Text("${selectedAmenities.size} selected", style = MaterialTheme.typography.labelMedium)
        }
        listOf("Fire pit", "Drinking water", "Hot showers", "Dogs welcome", "Parking", "Lake access").chunked(2).forEach { options ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEach { option ->
                    Box(Modifier.weight(1f)) {
                        BaseInput(
                            label = option, value = (option in selectedAmenities).toString(),
                            onValueChange = { checked ->
                                val amenities = if (checked.toBoolean()) {
                                    state.amenities + option
                                } else {
                                    state.amenities - option
                                }
                                onChange(state.copy(amenities = amenities.distinct()))
                            },
                            type = BaseInputType.Boolean, enabled = !state.isSaving
                        )
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("Tags", Modifier.weight(1f))
            Text("${state.tags.size} selected", style = MaterialTheme.typography.labelMedium)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.tags.forEach { tag ->
                AssistChip(onClick = { onChange(state.copy(tags = state.tags - tag)) }, enabled = !state.isSaving,
                    label = { Text("$tag ×") })
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tagDraft, onValueChange = { tagDraft = it },
                placeholder = { Text("Start typing…") },
                modifier = Modifier.weight(1f), singleLine = true, enabled = !state.isSaving,
                shape = ComponentStyles.InputShape
            )
            TextButton(enabled = tagDraft.isNotBlank() && !state.isSaving, onClick = {
                onChange(state.copy(tags = (state.tags + tagDraft.trim()).distinct()))
                tagDraft = ""
            }) { Text("Add") }
        }
        // 5. Check-in, check-out, and policies
        SectionTitle("A few ground rules")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                RegistrationField("Check-in (HH:mm)", "checkInTime", state.checkInTime, { value ->
                    onChange(state.copy(checkInTime = value))
                }, state.isSaving, errors["checkInTime"])
            }
            Column(Modifier.weight(1f)) {
                RegistrationField("Check-out (HH:mm)", "checkOutTime", state.checkOutTime, { value ->
                    onChange(state.copy(checkOutTime = value))
                }, state.isSaving, errors["checkOutTime"])
            }
        }
        BaseInput(
            label = "Cancellation policy", value = state.cancellationPolicy,
            onValueChange = { value -> onChange(state.copy(cancellationPolicy = value)) },
            type = BaseInputType.Dropdown(listOf(
                "Flexible · Free until 3 days before arrival",
                "Moderate · Free until 7 days before arrival",
                "Strict · Non-refundable"
            )), enabled = !state.isSaving, placeholder = "Select a policy"
        )
        RegistrationField("Campsite rules (one per line)", "rules", state.rules, { value ->
            onChange(state.copy(rules = value))
        }, state.isSaving, multiline = true)

        // 6. Register and cancel actions
        Text("* Required fields", style = MaterialTheme.typography.bodySmall)
        state.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        BaseButton(
            text = if (state.isSaving) "Registering…" else "Register campsite",
            onClick = onRegister,
            enabled = !state.isSaving && state.savedCampsiteId == null,
            modifier = Modifier.fillMaxWidth().testTag("registerCampsiteButton")
        )
        BaseButton(
            text = "Cancel registration",
            type = BaseButtonType.Outlined,
            onClick = onCancel,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun RegistrationField(
    label: String,
    tag: String,
    value: String,
    onValueChange: (String) -> Unit,
    isSaving: Boolean,
    error: String? = null,
    multiline: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = Modifier.testTag("registerCampsite_$tag"),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        if (multiline) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                isError = error != null,
                minLines = 3,
                shape = ComponentStyles.InputShape,
                textStyle = ComponentStyles.InputTextStyle,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ComponentStyles.InputBackgroundColor,
                    unfocusedContainerColor = ComponentStyles.InputBackgroundColor,
                    focusedBorderColor = ComponentStyles.InputBorderColor,
                    unfocusedBorderColor = ComponentStyles.InputBorderColor
                )
            )
        } else {
            val inputType = when (keyboardType) {
                KeyboardType.Number -> BaseInputType.Integer(min = 1)
                KeyboardType.Decimal -> BaseInputType.FloatNumber(min = 0f)
                else -> BaseInputType.Text()
            }
            BaseInput(
                label = label,
                value = value,
                onValueChange = onValueChange,
                type = inputType,
                enabled = !isSaving
            )
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterCampsitePreview() {
    TimewarpTheme {
        RegisterCampsiteForm(RegisterCampsiteState(), {}, {}, {})
    }
}
