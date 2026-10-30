package com.example.timewarp.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ComponentStyles {
    val ButtonShape = RoundedCornerShape(12.dp)
    val ButtonPadding = PaddingValues(
        horizontal = 16.dp,
        vertical = 16.dp
    )
    val ButtonHeight = 50.dp

    val PillShape = RoundedCornerShape(8.dp)
    val PillPadding = PaddingValues(
        horizontal = 9.dp,
        vertical = 6.dp
    )
    val PillHeight = 24.dp

    val InputShape = RoundedCornerShape(12.dp)
    val InputHeight = 46.dp
    val InputPadding = PaddingValues(12.dp)
    val InputTextStyle = TextStyle(
        fontSize = 13.sp,
        lineHeight = 20.sp
    )
    val InputBorderColor = PitchBorder
    val InputBackgroundColor = PitchSurface
    val InputBorderWidth = 1.dp

    val ScreenPadding = 24.dp
}