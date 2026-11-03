package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.AdminStats
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

actual class AdminRepository actual constructor() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    actual suspend fun getAdminStats(): Resource<AdminStats> {
        return try {
            val dealersSnap = firestore.collection("dealers").get().await()
            val totalDealers = dealersSnap.size()
            val pendingDealers = dealersSnap.documents.count { doc ->
                doc.getString("accountStatus") == DealerStatus.PENDING.name
            }

            val vehiclesSnap = firestore.collection("vehicles").get().await()
            val vehicles = vehiclesSnap.toObjects(Vehicle::class.java)

            val totalVehicles = vehicles.size
            val publishedVehicles = vehicles.count { it.status == VehicleStatus.PUBLISHED }
            val pendingVehicles = vehicles.count { it.status == VehicleStatus.PENDING }
            val soldVehicles = vehicles.count { it.status == VehicleStatus.SOLD }
            val totalViews = vehicles.sumOf { it.viewCount }

            val reportsSnap = firestore.collection("reports").get().await()
            val totalReports = reportsSnap.documents.count { doc ->
                doc.getBoolean("isResolved") != true
            }

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
            Resource.Error(e.localizedMessage ?: "İstatistikler hesaplanamadı")
        }
    }

    actual suspend fun resetDatabase(): Resource<Unit> {
        return try {
            // 1. Delete vehicles
            val vehiclesSnap = firestore.collection("vehicles").get().await()
            for (doc in vehiclesSnap.documents) {
                doc.reference.delete().await()
            }

            // 2. Delete dealers
            val dealersSnap = firestore.collection("dealers").get().await()
            for (doc in dealersSnap.documents) {
                doc.reference.delete().await()
            }

            // 3. Delete users except admins
            val usersSnap = firestore.collection("users").get().await()
            for (doc in usersSnap.documents) {
                val email = doc.getString("email") ?: ""
                val role = doc.getString("role") ?: ""
                if (email != "europexpert38@gmail.com" && email != "gazitasdemir46@gmail.com" && role != "SUPER_ADMIN" && role != "ADMIN") {
                    doc.reference.delete().await()
                }
            }

            // 4. Delete notifications, reports, favorites, debt_transactions, etc.
            listOf("notifications", "reports", "favorites", "debt_transactions", "reward_requests", "stamp_codes").forEach { col ->
                try {
                    val snap = firestore.collection(col).get().await()
                    for (doc in snap.documents) {
                        doc.reference.delete().await()
                    }
                } catch (_: Exception) {}
            }

            // Clear LocalStore
            com.example.anadolugalericilersit.data.local.LocalStore.vehicles.clear()
            com.example.anadolugalericilersit.data.local.LocalStore.dealers.clear()
            com.example.anadolugalericilersit.data.local.LocalStore.reports.clear()
            com.example.anadolugalericilersit.data.local.LocalStore.favorites.clear()
            com.example.anadolugalericilersit.data.local.LocalStore.notifications.clear()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Sıfırlama başarısız")
        }
    }
}
