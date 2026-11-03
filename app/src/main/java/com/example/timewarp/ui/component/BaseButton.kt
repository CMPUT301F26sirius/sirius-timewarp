package com.example.timewarp.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timewarp.ui.theme.ComponentStyles

sealed interface BaseButtonType {
    data object Default : BaseButtonType
    data object Outlined : BaseButtonType
    data object Passive : BaseButtonType
    data object Critical : BaseButtonType
}

@Composable
fun BaseButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    type: BaseButtonType = BaseButtonType.Default,
    enabled: Boolean = true,
//    content: @Composable RowScope.() -> Unit
) {
    if (type == BaseButtonType.Outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(ComponentStyles.ButtonHeight),
            enabled = enabled,
            shape = ComponentStyles.ButtonShape,
            contentPadding = ComponentStyles.ButtonPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.height(17.dp)
            )
        }
    } else if (type == BaseButtonType.Default) {
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
    } else if (type == BaseButtonType.Passive) {
        Button(
            onClick = onClick,
            modifier = modifier.height(ComponentStyles.ButtonHeight),
            enabled = enabled,
            shape = ComponentStyles.ButtonShape,
            contentPadding = ComponentStyles.ButtonPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.height(17.dp)
            )
        }
    } else if (type == BaseButtonType.Critical) {
        Button(
            onClick = onClick,
            modifier = modifier.height(ComponentStyles.ButtonHeight),
            enabled = enabled,
            shape = ComponentStyles.ButtonShape,
            contentPadding = ComponentStyles.ButtonPadding,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.height(17.dp)
            )
        }
    }
}