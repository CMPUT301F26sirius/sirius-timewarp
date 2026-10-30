package com.example.timewarp.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun MainPage(
    title: String = "",
    subtitle: String = "",
    navController: NavController = rememberNavController(),
    content: @Composable () -> Unit
//    content: @Composable RowScope.() -> Unit
) {
//    Page
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
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
            content()
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            content()
        }

        // Bottom bar
        BottomBar(
            navController = navController
        )
    }
}