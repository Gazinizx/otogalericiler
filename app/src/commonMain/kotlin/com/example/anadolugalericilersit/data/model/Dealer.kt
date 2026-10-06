package com.example.anadolugalericilersit.data.model

import com.example.anadolugalericilersit.utils.currentTimeMillis

enum class DealerStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

data class Dealer(
    val id: String = "",
    val ownerUid: String = "",
    val galleryName: String = "",
    val authorizedName: String = "",
    val phone: String = "",
    val email: String = "",
    val city: String = "",
    val district: String = "",
    val address: String = "",
    val taxNumber: String = "",
    val iban: String = "",
    val ibanOwnerName: String = "",
    val description: String = "",
    val logoUrl: String = "",
    val profilePhotoUrl: String = "",
    val shopPhotoUrl: String = "",
    val latitude: Double = 39.9334,
    val longitude: Double = 32.8597,
    val workingHours: String = "09:00 - 18:00",
    val accountStatus: DealerStatus = DealerStatus.PENDING,
    val rejectionReason: String = "",
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis(),
    val totalListings: Int = 0,
    val activeListings: Int = 0,
    val soldListings: Int = 0,
    val pendingListings: Int = 0,
    val totalDebt: Double = 0.0,
    val damgaCount: Int = 0,
    val rewardCycleCount: Int = 0
)
