package com.example.timewarp.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.example.timewarp.domain.model.Image

@Composable
fun RemoteImage(
    image: Image,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = image.url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}