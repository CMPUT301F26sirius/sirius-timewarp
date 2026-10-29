package com.example.timewarp.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.timewarp.ui.theme.ComponentStyles

@Composable
fun PitchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ComponentStyles.ButtonHeight),
        enabled = enabled,
        shape = ComponentStyles.ButtonShape,
        contentPadding = ComponentStyles.ButtonPadding,
        content = content,
    )
}