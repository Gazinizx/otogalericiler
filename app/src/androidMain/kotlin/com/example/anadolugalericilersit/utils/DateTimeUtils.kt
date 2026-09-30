package com.example.anadolugalericilersit.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun generateUuid(): String = UUID.randomUUID().toString()

actual fun formatPrice(price: Double): String {
    return try {
        NumberFormat.getInstance(Locale("tr", "TR")).format(price)
    } catch (_: Exception) {
        price.toLong().toString()
    }
}

actual fun formatKm(km: Int): String {
    return try {
        NumberFormat.getInstance(Locale("tr", "TR")).format(km)
    } catch (_: Exception) {
        km.toString()
    }
}

actual fun formatDate(timestamp: Long): String {
    return try {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(timestamp))
    } catch (_: Exception) {
        timestamp.toString()
    }
}
