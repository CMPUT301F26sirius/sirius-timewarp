
package com.example.timewarp.ui.component

import android.R.style
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.PitchMutedText


sealed interface BaseInputType {
    data class Text(
        val minChars: Int = 0,
        val maxChars: Int = Int.MAX_VALUE
    ) : BaseInputType

    data class Integer(
        val min: Int? = null,
        val max: Int? = null
    ) : BaseInputType

    data class FloatNumber(
        val min: Float? = null,
        val max: Float? = null
    ) : BaseInputType

    data class Dropdown(
        val options: List<String>
    ) : BaseInputType
}

@Composable
fun BaseInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    type: BaseInputType,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null
) {
    Column(
        verticalArrangement =  Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        when (type) {
            is BaseInputType.Text -> {
                val validLimits =
                    type.minChars >= 0 &&
                            type.maxChars >= type.minChars

                val lengthError = validLimits &&
                        value.isNotEmpty() &&
                        value.length < type.minChars

                Column(
                    verticalArrangement =  Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= type.maxChars) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.InputHeight),
                        shape = ComponentStyles.InputShape,
                        textStyle = ComponentStyles.InputTextStyle,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ComponentStyles.InputBackgroundColor,
                            unfocusedContainerColor = ComponentStyles.InputBackgroundColor,
                            focusedBorderColor = ComponentStyles.InputBorderColor,
                            unfocusedBorderColor = ComponentStyles.InputBorderColor,
                        ),
                        enabled = enabled,
                        placeholder = placeholder?.let {
                            { Text(
                                text = it,
                                style = ComponentStyles.InputTextStyle,
                                color = PitchMutedText
                            ) }
                        },
                        isError = lengthError || !validLimits,
                        singleLine = true
                    )

                    when {
                        !validLimits -> Text(
                            text = "Invalid character limits",
                            style = MaterialTheme.typography.labelSmall
                        )
                        lengthError -> Text(
                            text = "Enter at least ${type.minChars} characters",
                            style = MaterialTheme.typography.labelSmall
                        )
                        else -> Text(
                            text = "${value.length}/${type.maxChars} characters",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            is BaseInputType.Integer -> {
                val number = value.toIntOrNull()
                val rangeError = number != null && (
                        (type.min != null && number < type.min) ||
                                (type.max != null && number > type.max)
                        )

                Column(
                    verticalArrangement =  Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (
                                newValue.isEmpty() ||
                                newValue.toIntOrNull() != null ||
                                newValue == "-"
                            ) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.InputHeight),
                        shape = ComponentStyles.InputShape,
                        textStyle = ComponentStyles.InputTextStyle,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ComponentStyles.InputBackgroundColor,
                            unfocusedContainerColor = ComponentStyles.InputBackgroundColor,
                            focusedBorderColor = ComponentStyles.InputBorderColor,
                            unfocusedBorderColor = ComponentStyles.InputBorderColor,
                        ),
                        enabled = enabled,
                        placeholder = placeholder?.let {
                            {
                                Text(
                                    text = it,
                                    style = ComponentStyles.InputTextStyle,
                                    color = PitchMutedText
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        isError = rangeError,
                        singleLine = true
                    )

                    when {
                        rangeError -> Text("Value is outside the allowed range")
                        type.min != null && type.max != null -> Text(
                            text = "Range: ${type.min}–${type.max}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        type.min != null -> Text(
                            text = "Minimum: ${type.min}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        type.max != null -> Text(
                            text = "Maximum: ${type.max}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        else -> {}
                    }
                }
            }

            is BaseInputType.FloatNumber -> {
                val number = value.toFloatOrNull()
                val rangeError = number != null && (
                        (type.min != null && number < type.min) ||
                                (type.max != null && number > type.max)
                        )

                Column(
                    verticalArrangement =  Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            if (
                                newValue.isEmpty() ||
                                newValue.toFloatOrNull() != null ||
                                newValue == "-" ||
                                newValue == "." ||
                                newValue == "-."
                            ) {
                                onValueChange(newValue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ComponentStyles.InputHeight),
                        shape = ComponentStyles.InputShape,
                        textStyle = ComponentStyles.InputTextStyle,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ComponentStyles.InputBackgroundColor,
                            unfocusedContainerColor = ComponentStyles.InputBackgroundColor,
                            focusedBorderColor = ComponentStyles.InputBorderColor,
                            unfocusedBorderColor = ComponentStyles.InputBorderColor,
                        ),
                        enabled = enabled,
                        placeholder = placeholder?.let {
                            {
                                Text(
                                    text = it,
                                    style = ComponentStyles.InputTextStyle,
                                    color = PitchMutedText
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = rangeError,
                        singleLine = true
                    )

                    when {
                        rangeError -> Text(
                            text = "Value is outside the allowed range",
                            style = MaterialTheme.typography.labelSmall
                        )
                        type.min != null && type.max != null -> Text(
                            text = "Range: ${type.min}–${type.max}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        type.min != null -> Text(
                            text = "Minimum: ${type.min}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        type.max != null -> Text(
                            text = "Maximum: ${type.max}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        else -> {}
                    }
                }
            }

            is BaseInputType.Dropdown -> {
                var expanded by remember { mutableStateOf(false) }

                Column(
                    verticalArrangement =  Arrangement.spacedBy(7.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium
                    )

                    Box(
                        modifier = modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ComponentStyles.InputHeight),
                            shape = ComponentStyles.InputShape,
                            textStyle = ComponentStyles.InputTextStyle,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ComponentStyles.InputBackgroundColor,
                                unfocusedContainerColor = ComponentStyles.InputBackgroundColor,
                                focusedBorderColor = ComponentStyles.InputBorderColor,
                                unfocusedBorderColor = ComponentStyles.InputBorderColor,
                            ),
                            enabled = enabled,
                            readOnly = true,
                            placeholder = placeholder?.let {
                                { Text(
                                    text = it,
                                    style = ComponentStyles.InputTextStyle,
                                    color = PitchMutedText
                                ) }
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = PitchMutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                        )

                        // Menu trigger covers the text field.
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable() { expanded = true }
                        ) {

                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(ComponentStyles.InputBackgroundColor)
                        ) {
                            type.options.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option,
                                            style = ComponentStyles.InputTextStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = modifier.fillMaxWidth(),
                                        )
                                    },
                                    onClick = {
                                        onValueChange(option)
                                        expanded = false
                                    },
                                    modifier = modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}