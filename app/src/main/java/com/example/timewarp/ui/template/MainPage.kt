package com.example.timewarp.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.browse.BrowseScreen
import com.example.timewarp.ui.theme.TimewarpTheme

@Composable
fun MainPage(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    content: @Composable () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                navController = navController
            )
        }
    ) { paddingValues ->
        Column() {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .padding(paddingValues)
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
                }

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    TimewarpTheme() {
        MainPage(
            title = "Test",
            subtitle = "Test",
        )
    }
}