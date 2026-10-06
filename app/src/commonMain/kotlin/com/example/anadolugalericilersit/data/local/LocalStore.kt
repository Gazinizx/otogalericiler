package com.example.anadolugalericilersit.data.local

import com.example.anadolugalericilersit.data.model.*
import com.example.anadolugalericilersit.utils.currentTimeMillis
import com.example.anadolugalericilersit.utils.generateUuid

data class RewardRequest(
    val code: String = "",
    val dealerId: String = "",
    val galleryName: String = "",
    val authorizedName: String = "",
    val iban: String = "",
    val ibanOwnerName: String = "",
    var isApproved: Boolean = false,
    val createdAt: Long = currentTimeMillis()
)

data class StampCodeRequest(
    val code: String = "",
    val dealerId: String = "",
    val galleryName: String = "",
    val vehicleId: String = "",
    val vehicleTitle: String = "",
    var isUsed: Boolean = false,
    val createdAt: Long = currentTimeMillis()
)

enum class TransactionType {
    DEBT,    // Borç Ekle (+)
    PAYMENT  // Ödendi / Ödeme Alındı (-)
}

data class DebtTransaction(
    val id: String = generateUuid(),
    val dealerId: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val type: TransactionType = TransactionType.DEBT,
    val timestamp: Long = currentTimeMillis()
)

object LocalStore {
    var currentLoggedInUid: String? = null

    val users = mutableMapOf<String, User>()
    val dealers = mutableMapOf<String, Dealer>()

    val vehicles = mutableMapOf<String, Vehicle>()

    val favorites = mutableMapOf<String, MutableSet<String>>()
    val notifications = mutableMapOf<String, MutableList<NotificationItem>>()
    val reports = mutableListOf<Report>()
    val rewardRequests = mutableMapOf<String, RewardRequest>()
    val stampCodes = mutableMapOf<String, StampCodeRequest>()
    val debtTransactions = mutableMapOf<String, MutableList<DebtTransaction>>() // dealerId -> transactions
}
