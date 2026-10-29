package com.example.timewarp.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timewarp.ui.theme.ComponentStyles
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchPillUnselected

@Composable
fun IconPill(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
//    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ComponentStyles.PillHeight),
        enabled = enabled,
        shape = ComponentStyles.PillShape,
        contentPadding = ComponentStyles.PillPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = PitchPillUnselected,
            contentColor = PitchForest,
            disabledContainerColor = PitchPillUnselected,
            disabledContentColor = PitchForest
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Spacer(
                modifier = Modifier.width(9.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                modifier = Modifier.height(12.dp),
            )
        }
    }
}