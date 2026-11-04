package com.example.timewarp.ui.component

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

// Button that saves [csvText] as a CSV file the user picks a location for.
@Composable
fun CsvDownloadButton(
    csvText: String,
    fileName: String,
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Opens Android's "Save as" screen; returns where the user wants the file
    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        // uri is null when the user backs out of the picker
        if (uri != null) {
            scope.launch {
                val saved = writeTextToUri(context, uri, csvText)
                var message = "Couldn't save the CSV"
                if (saved) {
                    message = "CSV saved"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    IconButton(
        onClick = { saveLauncher.launch("$fileName.csv") },
        icon = Icons.Outlined.Download,
        text = text,
        modifier = modifier
    )
}

// Writes the text into the chosen file; runs off the main thread because it's disk I/O
private suspend fun writeTextToUri(context: Context, uri: Uri, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        var saved = false
        try {
            val stream = context.contentResolver.openOutputStream(uri, "wt")
            if (stream != null) {
                stream.use { output -> output.write(text.toByteArray(Charsets.UTF_8)) }
                saved = true
            }
        } catch (error: IOException) {
            saved = false
        }
        saved
    }
}