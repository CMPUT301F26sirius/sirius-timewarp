package com.example.timewarp.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timewarp.ui.theme.ComponentStyles

@Composable
fun BaseButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
//    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ComponentStyles.ButtonHeight),
        enabled = enabled,
        shape = ComponentStyles.ButtonShape,
        contentPadding = ComponentStyles.ButtonPadding,
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.height(17.dp)
        )
    }
}