package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.AdminStats
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.Resource

actual class AdminRepository {
    actual suspend fun getAdminStats(): Resource<AdminStats> {
        return try {
            val dealers = LocalStore.dealers.values.toList()
            val totalDealers = dealers.size
            val pendingDealers = dealers.count { it.accountStatus == DealerStatus.PENDING }

            val vehicles = LocalStore.vehicles.values.toList()
            val totalVehicles = vehicles.size
            val publishedVehicles = vehicles.count { it.status == VehicleStatus.PUBLISHED }
            val pendingVehicles = vehicles.count { it.status == VehicleStatus.PENDING }
            val soldVehicles = vehicles.count { it.status == VehicleStatus.SOLD }
            val totalViews = vehicles.sumOf { it.viewCount }

            val reports = LocalStore.reports
            val totalReports = reports.count { !it.isResolved }

            val stats = AdminStats(
                totalDealers = totalDealers,
                pendingDealers = pendingDealers,
                totalVehicles = totalVehicles,
                publishedVehicles = publishedVehicles,
                pendingVehicles = pendingVehicles,
                soldVehicles = soldVehicles,
                totalViews = totalViews,
                totalReports = totalReports
            )
            Resource.Success(stats)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İstatistikler hesaplanamadı")
        }
    }
}
