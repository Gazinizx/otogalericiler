package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.model.Dealer

actual object NotificationUtils {
    actual fun sendDebtNotification(title: String, message: String) {}
    actual fun sendDebtOrPaymentNotification(context: Any?, dealer: Dealer, type: String, amount: Double) {}
}
