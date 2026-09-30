package com.example.anadolugalericilersit.data.model

data class Report(
    val id: String = "",
    val vehicleId: String = "",
    val vehicleTitle: String = "",
    val reporterUid: String = "",
    val reporterEmail: String = "",
    val reason: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
