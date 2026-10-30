package com.example.timewarp.ui.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.ui.component.NavButton

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .height(24.dp)
            .fillMaxSize()
    ) {
        NavButton(
            onClick = {},
            icon = Icons.Outlined.Search,
            text = "Browse",
            selected = true
        )
        NavButton(
            onClick = {},
            icon = Icons.Outlined.Search,
            text = "Browse",
            selected = true
        )
        NavButton(
            onClick = {},
            icon = Icons.Outlined.Search,
            text = "Browse",
            selected = true
        )
        NavButton(
            onClick = {},
            icon = Icons.Outlined.Search,
            text = "Browse",
            selected = true
        )
    }
}