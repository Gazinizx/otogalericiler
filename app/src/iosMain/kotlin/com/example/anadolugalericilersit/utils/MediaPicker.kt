package com.example.anadolugalericilersit.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit = { onResult(null) }

@Composable
actual fun rememberMultipleImagePickerLauncher(
    onResult: (List<String>) -> Unit
): () -> Unit = { onResult(emptyList()) }
