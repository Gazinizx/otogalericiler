package com.example.anadolugalericilersit.data.model

import com.example.anadolugalericilersit.utils.currentTimeMillis

enum class VehicleStatus {
    DRAFT,
    PENDING,
    PUBLISHED,
    REJECTED,
    SOLD,
    PASSIVE
}

enum class DamgaStatus {
    NONE,
    PENDING,
    APPROVED,
    REJECTED
}

data class Vehicle(
    val id: String = "",
    val dealerId: String = "",
    val dealerName: String = "",
    val dealerPhone: String = "",
    val dealerCity: String = "",
    val dealerDistrict: String = "",
    val dealerLogoUrl: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 2024,
    val km: Int = 0,
    val fuelType: String = "",
    val transmission: String = "",
    val bodyType: String = "",
    val engineSize: String = "",
    val enginePower: String = "",
    val drivetrain: String = "",
    val color: String = "",
    val price: Double = 0.0,
    val negotiable: Boolean = false,
    val suitableForCredit: Boolean = false,
    val description: String = "",
    val licensePlate: String = "",
    val features: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val mainImageUrl: String = "",
    val videoUrl: String = "",
    val status: VehicleStatus = VehicleStatus.PUBLISHED,
    val viewCount: Long = 0,
    val favoriteCount: Long = 0,
    val rejectionReason: String = "",
    val hasDamga: Boolean = false,
    val expertInspectionCost: Double = 0.0,
    val expertReportStatus: String = "",
    val expertReportDetails: String = "",
    val expertReportImageUrl: String = "",
    val bodyPartsCondition: Map<String, String> = emptyMap(),
    val damgaStatus: DamgaStatus = DamgaStatus.NONE,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis()
)
