package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.ImageCompressor
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

actual class VehicleRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private val viewedVehicleIds = mutableSetOf<String>()

    actual suspend fun getVehicles(
        filter: VehicleFilter,
        sort: VehicleSort,
        limit: Int
    ): Resource<List<Vehicle>> {
        return try {
            val snapshot = firestore.collection("vehicles")
                .whereEqualTo("status", VehicleStatus.PUBLISHED.name)
                .get()
                .await()

            var vehicles = snapshot.toObjects(Vehicle::class.java)

            vehicles = vehicles.filterNot {
                it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                        it.dealerName.contains("Anadolu Oto", ignoreCase = true) ||
                        it.dealerId == "dealer_anadolu_01" ||
                        it.dealerId.lowercase().contains("anadolu")
            }

            if (!filter.brand.isNullOrBlank()) {
                vehicles = vehicles.filter { it.brand.equals(filter.brand, ignoreCase = true) }
            }
            if (!filter.model.isNullOrBlank()) {
                vehicles = vehicles.filter { it.model.contains(filter.model, ignoreCase = true) }
            }
            if (!filter.city.isNullOrBlank()) {
                vehicles = vehicles.filter { it.dealerCity.equals(filter.city, ignoreCase = true) }
            }
            if (!filter.fuelType.isNullOrBlank()) {
                vehicles = vehicles.filter { it.fuelType.equals(filter.fuelType, ignoreCase = true) }
            }
            if (!filter.transmission.isNullOrBlank()) {
                vehicles = vehicles.filter { it.transmission.equals(filter.transmission, ignoreCase = true) }
            }
            if (!filter.bodyType.isNullOrBlank()) {
                vehicles = vehicles.filter { it.bodyType.equals(filter.bodyType, ignoreCase = true) }
            }
            if (!filter.color.isNullOrBlank()) {
                vehicles = vehicles.filter { it.color.equals(filter.color, ignoreCase = true) }
            }

            if (filter.minPrice != null) vehicles = vehicles.filter { it.price >= filter.minPrice }
            if (filter.maxPrice != null) vehicles = vehicles.filter { it.price <= filter.maxPrice }
            if (filter.minYear != null) vehicles = vehicles.filter { it.year >= filter.minYear }
            if (filter.maxYear != null) vehicles = vehicles.filter { it.year <= filter.maxYear }
            if (filter.minKm != null) vehicles = vehicles.filter { it.km >= filter.minKm }
            if (filter.maxKm != null) vehicles = vehicles.filter { it.km <= filter.maxKm }

            if (!filter.searchQuery.isBlank()) {
                val q = filter.searchQuery.trim().lowercase()
                vehicles = vehicles.filter {
                    it.brand.lowercase().contains(q) ||
                            it.model.lowercase().contains(q) ||
                            it.description.lowercase().contains(q) ||
                            it.dealerName.lowercase().contains(q) ||
                            it.dealerCity.lowercase().contains(q)
                }
            }

            vehicles = when (sort) {
                VehicleSort.NEWEST -> vehicles.sortedByDescending { it.createdAt }
                VehicleSort.OLDEST -> vehicles.sortedBy { it.createdAt }
                VehicleSort.PRICE_ASC -> vehicles.sortedBy { it.price }
                VehicleSort.PRICE_DESC -> vehicles.sortedByDescending { it.price }
                VehicleSort.KM_ASC -> vehicles.sortedBy { it.km }
            }

            vehicles.forEach { LocalStore.vehicles[it.id] = it }

            Resource.Success(vehicles.take(limit))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Araçlar getirilirken hata oluştu")
        }
    }

    actual suspend fun getVehicleById(vehicleId: String): Resource<Vehicle> {
        return try {
            val doc = firestore.collection("vehicles").document(vehicleId).get().await()
            if (doc.exists()) {
                val vehicle = doc.toObject(Vehicle::class.java) ?: return Resource.Error("Araç verisi okunamadı")
                if (vehicle.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                    vehicle.dealerId == "dealer_anadolu_01" ||
                    vehicle.dealerId.lowercase().contains("anadolu")
                ) {
                    return Resource.Error("Araç bulunamadı")
                }
                LocalStore.vehicles[vehicle.id] = vehicle
                Resource.Success(vehicle)
            } else {
                Resource.Error("Araç bulunamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Araç detayları alınamadı")
        }
    }

    actual suspend fun getVehiclesByDealer(dealerId: String, statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        return try {
            val snapshot = firestore.collection("vehicles").whereEqualTo("dealerId", dealerId).get().await()
            var vehicles = snapshot.toObjects(Vehicle::class.java)
            vehicles = vehicles.filterNot {
                it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                        it.dealerId == "dealer_anadolu_01" ||
                        it.dealerId.lowercase().contains("anadolu")
            }
            vehicles.forEach { LocalStore.vehicles[it.id] = it }
            if (statusFilter != null) {
                vehicles = vehicles.filter { it.status == statusFilter }
            }
            Resource.Success(vehicles.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galerici araçları yüklenemedi")
        }
    }

    actual suspend fun getVehiclesByStatusForAdmin(statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        return try {
            val snapshot = firestore.collection("vehicles").get().await()
            var vehicles = snapshot.toObjects(Vehicle::class.java)
            vehicles = vehicles.filterNot {
                it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                        it.dealerId == "dealer_anadolu_01" ||
                        it.dealerId.lowercase().contains("anadolu")
            }
            if (statusFilter != null) {
                vehicles = vehicles.filter { it.status == statusFilter }
            }
            Resource.Success(vehicles.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "İlanlar listelenirken hata oluştu")
        }
    }

    actual suspend fun saveVehicle(
        context: Any?,
        vehicle: Vehicle,
        newImageUris: List<Any>,
        videoUri: Any?,
        expertReportUri: Any?,
        onProgress: (Int, String) -> Unit
    ): Resource<Vehicle> {
        return try {
            onProgress(10, "Görseller sıkıştırılıyor...")
            val finalImageUrls = vehicle.imageUrls.toMutableList()
            val vehicleId = if (vehicle.id.isBlank()) UUID.randomUUID().toString() else vehicle.id

            val totalImages = newImageUris.size
            newImageUris.forEachIndexed { index, uri ->
                val progressPercent = 10 + ((index + 1) * 60 / (totalImages.coerceAtLeast(1)))
                onProgress(progressPercent, "Fotoğraf yükleniyor (${index + 1}/$totalImages)...")

                val compressedBytes = ImageCompressor.compressImage(context, uri)
                if (compressedBytes.isNotEmpty()) {
                    val imgRef = storage.reference.child("vehicle_images/${vehicleId}_${UUID.randomUUID()}.jpg")
                    try {
                        val taskSnapshot = imgRef.putBytes(compressedBytes).await()
                        val url = try {
                            imgRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            try {
                                taskSnapshot.storage.downloadUrl.await().toString()
                            } catch (e2: Exception) {
                                uri.toString()
                            }
                        }
                        if (url.isNotBlank()) {
                            finalImageUrls.add(url)
                        }
                    } catch (e: Exception) {
                        finalImageUrls.add(uri.toString())
                    }
                } else {
                    finalImageUrls.add(uri.toString())
                }
            }

            var videoUrl = vehicle.videoUrl
            if (videoUri != null) {
                onProgress(80, "Video yükleniyor...")
                try {
                    val bytes = when (videoUri) {
                        is ByteArray -> videoUri
                        is String -> videoUri.encodeToByteArray()
                        else -> videoUri.toString().encodeToByteArray()
                    }
                    val videoRef = storage.reference.child("vehicle_videos/${vehicleId}_${UUID.randomUUID()}.mp4")
                    val taskSnapshot = videoRef.putBytes(bytes).await()
                    videoUrl = try {
                        videoRef.downloadUrl.await().toString()
                    } catch (e: Exception) {
                        videoUri.toString()
                    }
                } catch (e: Exception) {
                    videoUrl = videoUri.toString()
                }
            }

            var expertReportUrl = vehicle.expertReportImageUrl
            if (expertReportUri != null) {
                onProgress(85, "Ekspertiz raporu yükleniyor...")
                val compressedBytes = ImageCompressor.compressImage(context, expertReportUri)
                if (compressedBytes.isNotEmpty()) {
                    val reportRef = storage.reference.child("expert_reports/${vehicleId}_report.jpg")
                    try {
                        val taskSnapshot = reportRef.putBytes(compressedBytes).await()
                        expertReportUrl = try {
                            reportRef.downloadUrl.await().toString()
                        } catch (e: Exception) {
                            expertReportUri.toString()
                        }
                    } catch (e: Exception) {
                        expertReportUrl = expertReportUri.toString()
                    }
                } else {
                    expertReportUrl = expertReportUri.toString()
                }
            }

            onProgress(90, "İlan veritabanına kaydediliyor...")
            val mainImage = finalImageUrls.firstOrNull() ?: ""

            val updatedVehicle = vehicle.copy(
                id = vehicleId,
                imageUrls = finalImageUrls,
                mainImageUrl = mainImage,
                videoUrl = videoUrl,
                expertReportImageUrl = expertReportUrl,
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection("vehicles").document(vehicleId).set(updatedVehicle).await()
            updateDealerVehicleStats(vehicle.dealerId)

            onProgress(100, "Tamamlandı!")
            Resource.Success(updatedVehicle)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "İlan kaydedilirken hata oluştu")
        }
    }

    actual suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus, rejectionReason: String): Resource<Unit> {
        return try {
            val doc = firestore.collection("vehicles").document(vehicleId).get().await()
            val dealerId = doc.getString("dealerId") ?: ""

            val updates = mapOf(
                "status" to status.name,
                "rejectionReason" to rejectionReason,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("vehicles").document(vehicleId).update(updates).await()

            if (dealerId.isNotBlank()) {
                updateDealerVehicleStats(dealerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "İlan durumu güncellenemedi")
        }
    }

    actual suspend fun updateVehicleDamgaStatus(vehicleId: String, damgaStatus: String, approved: Boolean): Resource<Unit> {
        return try {
            val updates = mapOf(
                "damgaStatus" to damgaStatus,
                "hasDamga" to approved,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("vehicles").document(vehicleId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Damga durumu güncellenemedi")
        }
    }

    actual suspend fun deleteVehicle(vehicleId: String): Resource<Unit> {
        return try {
            val doc = firestore.collection("vehicles").document(vehicleId).get().await()
            val dealerId = doc.getString("dealerId") ?: ""

            firestore.collection("vehicles").document(vehicleId).delete().await()

            if (dealerId.isNotBlank()) {
                updateDealerVehicleStats(dealerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "İlan silinemedi")
        }
    }

    actual suspend fun incrementViewCount(vehicleId: String, currentUserId: String?): Resource<Unit> {
        if (viewedVehicleIds.contains(vehicleId)) return Resource.Success(Unit)
        return try {
            viewedVehicleIds.add(vehicleId)
            val docRef = firestore.collection("vehicles").document(vehicleId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentViews = snapshot.getLong("viewCount") ?: 0L
                transaction.update(docRef, "viewCount", currentViews + 1)
            }.await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Success(Unit)
        }
    }

    private suspend fun updateDealerVehicleStats(dealerId: String) {
        try {
            val snapshot = firestore.collection("vehicles").whereEqualTo("dealerId", dealerId).get().await()
            val vehicles = snapshot.toObjects(Vehicle::class.java)

            val total = vehicles.size
            val active = vehicles.count { it.status == VehicleStatus.PUBLISHED }
            val sold = vehicles.count { it.status == VehicleStatus.SOLD }
            val pending = vehicles.count { it.status == VehicleStatus.PENDING }

            val statsUpdate = mapOf(
                "totalListings" to total,
                "activeListings" to active,
                "soldListings" to sold,
                "pendingListings" to pending
            )
            firestore.collection("dealers").document(dealerId).update(statsUpdate).await()
        } catch (e: Exception) {
            // ignore
        }
    }
}
