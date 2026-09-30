package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

actual class FavoriteRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    actual suspend fun getFavoriteVehicleIds(userId: String): Resource<List<String>> {
        return try {
            val favsSnapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get().await()
            val vehicleIds = favsSnapshot.documents.mapNotNull { it.getString("vehicleId") }
            Resource.Success(vehicleIds)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Favori ID'leri alınamadı")
        }
    }

    actual suspend fun addFavorite(userId: String, vehicleId: String): Resource<Unit> {
        toggleFavorite(vehicleId, userId)
        return Resource.Success(Unit)
    }

    actual suspend fun removeFavorite(userId: String, vehicleId: String): Resource<Unit> {
        toggleFavorite(vehicleId, userId)
        return Resource.Success(Unit)
    }

    suspend fun isFavorite(vehicleId: String, userId: String): Boolean {
        return try {
            val docId = "${userId}_$vehicleId"
            val snapshot = firestore.collection("favorites").document(docId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun toggleFavorite(vehicleId: String, userId: String): Resource<Boolean> {
        return try {
            val docId = "${userId}_$vehicleId"
            val docRef = firestore.collection("favorites").document(docId)
            val vehicleRef = firestore.collection("vehicles").document(vehicleId)

            val snapshot = docRef.get().await()
            val isFavNow: Boolean

            if (snapshot.exists()) {
                docRef.delete().await()
                isFavNow = false
                firestore.runTransaction { transaction ->
                    val vSnap = transaction.get(vehicleRef)
                    val currentCount = vSnap.getLong("favoriteCount") ?: 0L
                    if (currentCount > 0) {
                        transaction.update(vehicleRef, "favoriteCount", currentCount - 1)
                    }
                }.await()
            } else {
                val data = mapOf(
                    "userId" to userId,
                    "vehicleId" to vehicleId,
                    "createdAt" to System.currentTimeMillis()
                )
                docRef.set(data).await()
                isFavNow = true
                firestore.runTransaction { transaction ->
                    val vSnap = transaction.get(vehicleRef)
                    val currentCount = vSnap.getLong("favoriteCount") ?: 0L
                    transaction.update(vehicleRef, "favoriteCount", currentCount + 1)
                }.await()
            }

            Resource.Success(isFavNow)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Favori işlemi başarısız")
        }
    }

    actual suspend fun getFavoriteVehicles(userId: String): Resource<List<Vehicle>> {
        return try {
            val favsSnapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .get().await()

            val vehicleIds = favsSnapshot.documents.mapNotNull { it.getString("vehicleId") }
            if (vehicleIds.isEmpty()) return Resource.Success(emptyList())

            val vehicles = mutableListOf<Vehicle>()
            vehicleIds.chunked(10).forEach { chunk ->
                val vSnapshot = firestore.collection("vehicles")
                    .whereIn("id", chunk)
                    .get().await()
                vehicles.addAll(vSnapshot.toObjects(Vehicle::class.java))
            }

            Resource.Success(vehicles)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Favori araçlar alınamadı")
        }
    }
}
