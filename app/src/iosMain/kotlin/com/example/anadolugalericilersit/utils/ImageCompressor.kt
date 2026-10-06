package com.example.anadolugalericilersit.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.useContents
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.CoreGraphics.CGSizeMake
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIGraphicsEndImageContext
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual object ImageCompressor {
    actual suspend fun compressImage(context: Any?, imageInput: Any, quality: Int): ByteArray = withContext(Dispatchers.Default) {
        try {
            val uiImage = when (imageInput) {
                is UIImage -> imageInput
                is NSData -> UIImage(data = imageInput)
                is ByteArray -> memScoped {
                    val nsData = NSData.create(bytes = imageInput.refTo(0).getPointer(this), length = imageInput.size.toULong())
                    nsData?.let { UIImage(data = it) }
                }
                is String -> {
                    if (imageInput.startsWith("file://") || imageInput.startsWith("http://") || imageInput.startsWith("https://")) {
                        val url = NSURL.URLWithString(imageInput)
                        url?.let { NSData.dataWithContentsOfURL(it) }?.let { UIImage(data = it) }
                    } else {
                        NSData.dataWithContentsOfFile(imageInput)?.let { UIImage(data = it) }
                    }
                }
                is NSURL -> {
                    NSData.dataWithContentsOfURL(imageInput)?.let { UIImage(data = it) }
                }
                else -> null
            } ?: return@withContext ByteArray(0)

            val resizedImage = resizeImageIfNeeded(uiImage, 600.0) ?: uiImage
            val compressionQuality = (quality.coerceIn(40, 65)) / 100.0
            val nsData = UIImageJPEGRepresentation(resizedImage, compressionQuality) ?: return@withContext ByteArray(0)

            val length = nsData.length.toInt()
            if (length <= 0) return@withContext ByteArray(0)

            val bytes = ByteArray(length)
            nsData.bytes?.let { pointer ->
                bytes.usePinned { pinned ->
                    memcpy(pinned.addressOf(0), pointer, length.toULong())
                }
            }
            bytes
        } catch (_: Exception) {
            ByteArray(0)
        }
    }

    private fun resizeImageIfNeeded(image: UIImage, maxDimension: Double): UIImage? {
        val size = image.size
        val width = size.useContents { width }
        val height = size.useContents { height }

        if (width <= maxDimension && height <= maxDimension) {
            return image
        }

        val ratio = width / height
        val targetWidth: Double
        val targetHeight: Double

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = maxDimension / ratio
        } else {
            targetHeight = maxDimension
            targetWidth = maxDimension * ratio
        }

        val targetSize = CGSizeMake(targetWidth, targetHeight)
        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
        image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
        val resultImage =UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resultImage
    }
}
