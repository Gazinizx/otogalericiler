package com.example.anadolugalericilersit.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.NSUUID

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun generateUuid(): String = NSUUID().UUIDString()

actual fun formatPrice(price: Double): String {
    val formatter = NSNumberFormatter().apply {
        locale = NSLocale("tr_TR")
        numberStyle = 1u
    }
    return formatter.stringFromNumber(NSNumber(double = price)) ?: price.toLong().toString()
}

actual fun formatKm(km: Int): String {
    val formatter = NSNumberFormatter().apply {
        locale = NSLocale("tr_TR")
        numberStyle = 1u
    }
    return formatter.stringFromNumber(NSNumber(int = km)) ?: km.toString()
}

actual fun formatDate(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
    val formatter = NSDateFormatter().apply {
        locale = NSLocale("tr_TR")
        dateFormat = "dd.MM.yyyy HH:mm"
    }
    return formatter.stringFromDate(date)
}
