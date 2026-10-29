package com.example.timewarp.ui.template

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun MainPageTemplate(
    title: String = "",
    navController: NavController = rememberNavController(),
    content: @Composable () -> Unit
//    content: @Composable RowScope.() -> Unit
) {
//    Page
    Column() {
        Text(
            text = title
        )
        content()
        BottomBar(
            navController = navController
        )
    }
}