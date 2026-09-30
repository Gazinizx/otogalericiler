package com.example.anadolugalericilersit.utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onResult(uri?.toString())
    }
    return { launcher.launch("image/*") }
}

@Composable
actual fun rememberMultipleImagePickerLauncher(
    onResult: (List<String>) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        onResult(uris.map { it.toString() })
    }
    return { launcher.launch("image/*") }
}
