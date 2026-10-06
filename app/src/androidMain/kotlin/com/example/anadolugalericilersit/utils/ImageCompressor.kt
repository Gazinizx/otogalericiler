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
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            when (imageInput) {
                is Uri -> {
                    ctx.contentResolver.openInputStream(imageInput)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
                is String -> {
                    val uri = Uri.parse(imageInput)
                    try {
                        ctx.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, options)
                        }
                    } catch (_: Exception) {
                        try {
                            BitmapFactory.decodeFile(imageInput, options)
                        } catch (_: Exception) {}
                    }
                }
                is Bitmap -> {
                    val stream = ByteArrayOutputStream()
                    imageInput.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(40, 65), stream)
                    return@withContext stream.toByteArray()
                }
                is ByteArray -> {
                    BitmapFactory.decodeByteArray(imageInput, 0, imageInput.size, options)
                }
            }

            val maxDimension = 600
            var inSampleSize = 1
            val height = options.outHeight
            val width = options.outWidth
            if (height > maxDimension || width > maxDimension) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while ((halfHeight / inSampleSize) >= maxDimension && (halfWidth / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val bitmap = when (imageInput) {
                is Uri -> {
                    ctx.contentResolver.openInputStream(imageInput)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }
                }
                is String -> {
                    val uri = Uri.parse(imageInput)
                    try {
                        ctx.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it, null, decodeOptions)
                        }
                    } catch (_: Exception) {
                        try {
                            BitmapFactory.decodeFile(imageInput, decodeOptions)
                        } catch (_: Exception) {
                            null
                        }
                    }
                }
                is ByteArray -> {
                    BitmapFactory.decodeByteArray(imageInput, 0, imageInput.size, decodeOptions)
                }
                else -> null
            } ?: return@withContext ByteArray(0)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(40, 65), stream)
            val bytes = stream.toByteArray()
            bitmap.recycle()
            bytes
        } catch (e: Exception) {
            ByteArray(0)
        }
    }
}
