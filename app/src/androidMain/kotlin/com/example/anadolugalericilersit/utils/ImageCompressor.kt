package com.example.anadolugalericilersit.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.anadolugalericilersit.AnadoluApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual object ImageCompressor {
    actual suspend fun compressImage(context: Any?, imageInput: Any, quality: Int): ByteArray = withContext(Dispatchers.IO) {
        try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            val bitmap = when (imageInput) {
                is Uri -> {
                    ctx.contentResolver.openInputStream(imageInput)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                }
                is Bitmap -> imageInput
                is ByteArray -> BitmapFactory.decodeByteArray(imageInput, 0, imageInput.size)
                else -> null
            } ?: return@withContext ByteArray(0)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}
