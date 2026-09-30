package com.example.anadolugalericilersit.utils

expect object ImageCompressor {
    suspend fun compressImage(context: Any? = null, imageInput: Any, quality: Int = 80): ByteArray
}
