package com.example.anadolugalericilersit.data.repository

import android.util.Base64
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.CloudinaryUploader
import com.example.anadolugalericilersit.utils.ImgBBUploader
import com.example.anadolugalericilersit.utils.ImageCompressor
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID


actual class VehicleRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy {
        try {
            FirebaseStorage.getInstance("gs://galeri-6fa72.appspot.com")
        } catch (_: Exception) {
            try {
                FirebaseStorage.getInstance("gs://galeri-6fa72.firebasestorage.app")
            } catch (_: Exception) {
                FirebaseStorage.getInstance()
            }
        }
    }
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
            val remoteVehicles = snapshot.toObjects(Vehicle::class.java)
            remoteVehicles.forEach { LocalStore.vehicles[it.id] = it }

            val localVehicles = LocalStore.vehicles.values.filter { it.dealerId == dealerId }
            val combined = (remoteVehicles + localVehicles).distinctBy { it.id }
                .filterNot {
                    it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.dealerId == "dealer_anadolu_01" ||
                            it.dealerId.lowercase().contains("anadolu")
                }

            val filtered = if (statusFilter != null) {
                combined.filter { it.status == statusFilter }
            } else {
                combined
            }
            Resource.Success(filtered.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            val localVehicles = LocalStore.vehicles.values.filter { it.dealerId == dealerId }
                .filterNot {
                    it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.dealerId == "dealer_anadolu_01" ||
                            it.dealerId.lowercase().contains("anadolu")
                }
            val filtered = if (statusFilter != null) {
                localVehicles.filter { it.status == statusFilter }
            } else {
                localVehicles
            }
            Resource.Success(filtered.sortedByDescending { it.createdAt })
        }
    }

    actual suspend fun getVehiclesByStatusForAdmin(statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        return try {
            val snapshot = firestore.collection("vehicles").get().await()
            val remoteVehicles = snapshot.toObjects(Vehicle::class.java)
            remoteVehicles.forEach { LocalStore.vehicles[it.id] = it }

            val localVehicles = LocalStore.vehicles.values.toList()
            val combined = (remoteVehicles + localVehicles).distinctBy { it.id }
                .filterNot {
                    it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.dealerId == "dealer_anadolu_01" ||
                            it.dealerId.lowercase().contains("anadolu")
                }

            val filtered = if (statusFilter != null) {
                combined.filter { it.status == statusFilter }
            } else {
                combined
            }
            Resource.Success(filtered.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            val localVehicles = LocalStore.vehicles.values.toList()
                .filterNot {
                    it.dealerName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.dealerId == "dealer_anadolu_01" ||
                            it.dealerId.lowercase().contains("anadolu")
                }
            val filtered = if (statusFilter != null) {
                localVehicles.filter { it.status == statusFilter }
            } else {
                localVehicles
            }
            Resource.Success(filtered.sortedByDescending { it.createdAt })
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
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                ?: LocalStore.currentLoggedInUid
                ?: SessionManager.getSavedUid()

            if (currentUid.isNullOrBlank()) {
                return Resource.Error("İlan vermek için giriş yapmalısınız.")
            }
            val userId = currentUid

            onProgress(10, "Görseller sıkıştırılıyor...")
            val finalImageUrls = mutableListOf<String>()
            val vehicleId = if (vehicle.id.isBlank()) UUID.randomUUID().toString() else vehicle.id

            vehicle.imageUrls.forEach { url ->
                if (url.isNotBlank() && url.startsWith("http") && !finalImageUrls.contains(url)) {
                    finalImageUrls.add(url)
                }
            }

            val totalImages = newImageUris.size
            newImageUris.forEachIndexed { index, uri ->
                if (uri is String && uri.startsWith("http")) {
                    if (!finalImageUrls.contains(uri)) {
                        finalImageUrls.add(uri)
                    }
                    return@forEachIndexed
                }

                val progressPercent = 10 + ((index + 1) * 60 / (totalImages.coerceAtLeast(1)))
                onProgress(progressPercent, "Fotoğraf yükleniyor (${index + 1}/$totalImages)...")

                val compressedBytes = ImageCompressor.compressImage(context, uri, 55)
                if (compressedBytes.isNotEmpty()) {
                    var uploadedUrl: String? = ImgBBUploader.uploadImageBytes(compressedBytes)

                    if (uploadedUrl.isNullOrBlank()) {
                        uploadedUrl = CloudinaryUploader.uploadImageBytes(compressedBytes)
                    }

                    if (uploadedUrl.isNullOrBlank()) {
                        try {
                            val uniqueFileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
                            val imgRef = storage.reference.child("vehicles/$userId/$vehicleId/$uniqueFileName")

                            val taskSnapshot = imgRef.putBytes(compressedBytes).await()
                            uploadedUrl = taskSnapshot.metadata?.reference?.downloadUrl?.await()?.toString()
                                ?: imgRef.downloadUrl.await().toString()
                        } catch (_: Exception) {}
                    }

                    if (uploadedUrl.isNullOrBlank()) {
                        val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                        uploadedUrl = "data:image/jpeg;base64,$base64Str"
                    }

                    if (!finalImageUrls.contains(uploadedUrl)) {
                        finalImageUrls.add(uploadedUrl)
                    }
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
                    val videoRef = storage.reference.child("vehicles/$userId/$vehicleId/video.mp4")
                    val taskSnapshot = videoRef.putBytes(bytes).await()
                    videoUrl = taskSnapshot.metadata?.reference?.downloadUrl?.await()?.toString()
                        ?: videoRef.downloadUrl.await().toString()
                } catch (_: Exception) {
                    videoUrl = videoUri.toString()
                }
            }

            var expertReportUrl = vehicle.expertReportImageUrl
            if (expertReportUri != null) {
                onProgress(85, "Ekspertiz raporu yükleniyor...")
                val compressedBytes = ImageCompressor.compressImage(context, expertReportUri, 70)
                if (compressedBytes.isNotEmpty()) {
                    var uploadedUrl: String? = ImgBBUploader.uploadImageBytes(compressedBytes)

                    if (uploadedUrl.isNullOrBlank()) {
                        uploadedUrl = CloudinaryUploader.uploadImageBytes(compressedBytes)
                    }
                    if (uploadedUrl.isNullOrBlank()) {
                        try {
                            val reportRef = storage.reference.child("vehicles/$userId/$vehicleId/expert_report.jpg")
                            val taskSnapshot = reportRef.putBytes(compressedBytes).await()
                            uploadedUrl = taskSnapshot.metadata?.reference?.downloadUrl?.await()?.toString()
                                ?: reportRef.downloadUrl.await().toString()
                        } catch (_: Exception) {}
                    }
                    if (!uploadedUrl.isNullOrBlank() && uploadedUrl.startsWith("http")) {
                        expertReportUrl = uploadedUrl
                    }
                } else if (expertReportUri is String && expertReportUri.startsWith("http")) {
                    expertReportUrl = expertReportUri
                }
            }

            onProgress(90, "kaydediliyor...")
            val mainImage = finalImageUrls.firstOrNull() ?: ""

            // Fetch active user & dealer details from Firestore to guarantee 100% account ownership
            var dName = vehicle.dealerName
            var dPhone = vehicle.dealerPhone
            var dCity = vehicle.dealerCity
            var dDistrict = vehicle.dealerDistrict
            var dLogo = vehicle.dealerLogoUrl

            try {
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userObj = userDoc.toObject(User::class.java)

                val dealerDoc = firestore.collection("dealers").document(userId).get().await()
                val dealerObj = if (dealerDoc.exists()) dealerDoc.toObject(Dealer::class.java) else null

                if (dealerObj != null) {
                    dName = if (dealerObj.galleryName.isNotBlank()) dealerObj.galleryName else if (userObj?.name?.isNotBlank() == true) userObj.name else userObj?.email ?: "Galeri"
                    dPhone = dealerObj.phone
                    dCity = dealerObj.city
                    dDistrict = dealerObj.district
                    dLogo = if (dealerObj.logoUrl.isNotBlank()) dealerObj.logoUrl else if (dealerObj.shopPhotoUrl.isNotBlank()) dealerObj.shopPhotoUrl else dealerObj.profilePhotoUrl
                } else if (userObj != null) {
                    dName = if (userObj.name.isNotBlank()) userObj.name else userObj.email
                }
            } catch (_: Exception) {}

            val updatedVehicle = vehicle.copy(
                id = vehicleId,
                dealerId = userId,
                dealerName = dName.ifBlank { "Galeri İlanı" },
                dealerPhone = dPhone,
                dealerCity = dCity.ifBlank { "Kayseri" },
                dealerDistrict = dDistrict.ifBlank { "Melikgazi" },
                dealerLogoUrl = dLogo,
                imageUrls = finalImageUrls,
                mainImageUrl = mainImage,
                videoUrl = videoUrl,
                expertReportImageUrl = expertReportUrl,
                updatedAt = System.currentTimeMillis()
            )

            LocalStore.vehicles[vehicleId] = updatedVehicle

            firestore.collection("vehicles").document(vehicleId).set(updatedVehicle).await()
            updateDealerVehicleStats(userId)

            onProgress(100, "Tamamlandı!")
            Resource.Success(updatedVehicle)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "İlan kaydedilirken hata oluştu: ${e.message}")
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
            LocalStore.vehicles.remove(vehicleId)

            if (dealerId.isNotBlank()) {
                updateDealerVehicleStats(dealerId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            LocalStore.vehicles.remove(vehicleId)
            val vehicle = LocalStore.vehicles[vehicleId]
            val dId = vehicle?.dealerId ?: ""
            if (dId.isNotBlank()) {
                updateDealerVehicleStats(dId)
            }
            Resource.Success(Unit)
        }
    }

    actual suspend fun deleteAllVehicles(): Resource<Unit> {
        return try {
            LocalStore.vehicles.clear()
            val snapshot = firestore.collection("vehicles").get().await()
            for (doc in snapshot.documents) {
                try {
                    doc.reference.delete().await()
                } catch (_: Exception) {}
            }
            val dealerDocs = firestore.collection("dealers").get().await()
            for (dealerDoc in dealerDocs.documents) {
                try {
                    dealerDoc.reference.update(
                        mapOf(
                            "totalListings" to 0,
                            "activeListings" to 0,
                            "soldListings" to 0,
                            "pendingListings" to 0
                        )
                    ).await()
                } catch (_: Exception) {}
            }
            LocalStore.dealers.values.forEach { d ->
                LocalStore.dealers[d.id] = d.copy(
                    totalListings = 0,
                    activeListings = 0,
                    soldListings = 0,
                    pendingListings = 0
                )
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            LocalStore.vehicles.clear()
            Resource.Success(Unit)
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

            val localDealer = LocalStore.dealers[dealerId]
            if (localDealer != null) {
                LocalStore.dealers[dealerId] = localDealer.copy(
                    totalListings = total,
                    activeListings = active,
                    soldListings = sold,
                    pendingListings = pending
                )
            }
        } catch (e: Exception) {
            val localVehicles = LocalStore.vehicles.values.filter { it.dealerId == dealerId }
            val total = localVehicles.size
            val active = localVehicles.count { it.status == VehicleStatus.PUBLISHED }
            val sold = localVehicles.count { it.status == VehicleStatus.SOLD }
            val pending = localVehicles.count { it.status == VehicleStatus.PENDING }

            val localDealer = LocalStore.dealers[dealerId]
            if (localDealer != null) {
                LocalStore.dealers[dealerId] = localDealer.copy(
                    totalListings = total,
                    activeListings = active,
                    soldListings = sold,
                    pendingListings = pending
                )
            }
        }
    }
}
