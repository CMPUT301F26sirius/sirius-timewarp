package com.example.timewarp.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SubPage(
    title: String = "",
    subtitle: String = "",
    navController: NavController = rememberNavController(),
    content: @Composable () -> Unit = {}
//    content: @Composable RowScope.() -> Unit
) {
// Title
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .fillMaxWidth()
        )
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    // Content
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        content()
    }
}