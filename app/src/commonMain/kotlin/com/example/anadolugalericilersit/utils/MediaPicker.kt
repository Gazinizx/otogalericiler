package com.example.anadolugalericilersit.utils

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(
    onResult: (String?) -> Unit
): () -> Unit

@Composable
expect fun rememberMultipleImagePickerLauncher(
    onResult: (List<String>) -> Unit
): () -> Unit
