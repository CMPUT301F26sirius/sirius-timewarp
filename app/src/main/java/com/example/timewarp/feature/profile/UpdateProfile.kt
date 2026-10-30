package com.example.timewarp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.timewarp.ui.component.BaseInput
import com.example.timewarp.ui.component.BaseInputType
import com.example.timewarp.ui.component.IconPill
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme
import com.example.timewarp.ui.theme.BrandGradient
import com.example.timewarp.ui.theme.PitchLink

/*

Main Responsibility: Get profile data, update user profile (Name, email
                     phone number, profile picture, etc.)

Parameters: None right now, may change later on if better way to use them.

 */
@Composable
fun UpdateProfile() {

    var updatedFirstName by remember { mutableStateOf("") }
    var updatedLastName by remember { mutableStateOf("") }
    var updatedEmail by remember { mutableStateOf("")}
    var updatedPhoneNumber by remember {mutableStateOf("")}

    // Before Column, we need the logic for getting from database
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("<-- Update Profile", style = MaterialTheme.typography.titleMedium)

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            BaseInput(
                label = "First Name",
                value = updatedFirstName,
                onValueChange = { updatedFirstName = it },
                type = BaseInputType.Text(minChars = 1, maxChars = 30),
                modifier = Modifier.weight(1f)
            )
            BaseInput(
                label = "Last Name",
                value = updatedLastName,
                onValueChange = { updatedLastName = it },
                type = BaseInputType.Text(minChars = 1, maxChars = 30),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){

            BaseInput(
                label = "Email",
                value = updatedEmail,
                onValueChange = { updatedEmail = it },
                type = BaseInputType.Text(minChars = 1, maxChars = 30),
                placeholder = "yourinfo@example.com",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.width(8.dp))
            BaseInput(
                label = "Phone Number",
                value = updatedPhoneNumber,
                onValueChange = { updatedPhoneNumber = it },
                type = BaseInputType.Text(minChars = 1, maxChars = 30),
                placeholder = "xxx xxx xxxx",
                modifier = Modifier.fillMaxWidth()
            )

        }

    }

}