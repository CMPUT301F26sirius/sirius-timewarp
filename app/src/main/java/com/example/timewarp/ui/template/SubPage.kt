package com.example.timewarp.ui.template

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SubPage(
    title: String = "",
    navController: NavController = rememberNavController(),
    content: @Composable () -> Unit
//    content: @Composable RowScope.() -> Unit
) {
//    Page
    content()
}