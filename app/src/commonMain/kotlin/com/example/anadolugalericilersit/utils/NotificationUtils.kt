package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.model.Dealer

expect object NotificationUtils {
    fun sendDebtNotification(title: String, message: String)
    fun sendDebtOrPaymentNotification(context: Any? = null, dealer: Dealer, type: String, amount: Double)
}
