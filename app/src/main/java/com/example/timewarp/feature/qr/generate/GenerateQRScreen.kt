package com.example.timewarp.feature.qr.generate

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.timewarp.feature.navigation.NavigationScreen
import com.example.timewarp.navigation.Screen
import com.example.timewarp.ui.component.BaseButton
import com.example.timewarp.ui.theme.TimewarpTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

/**
 * [GenerateQRScreen]
 *
 * Displays a QR code, link, copy and share options for the passed url.
 *
 * @param modifier - to applied to the scaffold
 * @param url - to be turned into a QR code
 * @param screen - the screen object identifying this screen
 * @param navController - for navigation
 */
@Composable
fun GenerateQRScreen(
    modifier: Modifier = Modifier,
    url: String = "",
    screen: Screen = Screen.GenerateQR,
    navController: NavController = rememberNavController(),
) {
    // State
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val qrResult = remember(url) { runCatching { createQrBitmap(url) } }

    NavigationScreen(
        screen = screen,
        modifier = modifier,
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                url.isBlank() -> Text(
                    text = "No link was provided.",
                    color = MaterialTheme.colorScheme.error
                )

                qrResult.isSuccess -> {
                    Image(
                        bitmap = qrResult.getOrNull()?.asImageBitmap() ?: createBitmap(0, 0).asImageBitmap(),
                        contentDescription = "QR code for $url",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BaseButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(url))
                            Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                        },
                        text = "Copy link",
                        modifier = Modifier.fillMaxWidth()
                    )
                    BaseButton(
                        onClick = {
                            context.startActivity(
                                Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    },
                                    "Share campsite link"
                                )
                            )
                        },
                        text = "Share link",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> Text(
                    text = "Unable to generate QR code: ${qrResult.exceptionOrNull()?.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Converts a string into a qr code bitmap.
 *
 * @param content - string to turn into a QR code bitmap.
 */
private fun createQrBitmap(content: String): Bitmap {
    val matrix: BitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 512, 512)
    return createBitmap(matrix.width, matrix.height).apply {
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                set(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenerateQRScreenPreview() {
    TimewarpTheme {
        GenerateQRScreen(url = "pitch://campsite/details/0")
    }
}
