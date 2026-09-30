package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.utils.ImageCompressor
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class DealerRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun getDealerById(dealerId: String): Resource<Dealer> {
        return try {
            val doc = firestore.collection("dealers").document(dealerId).get().await()
            if (doc.exists()) {
                val dealer = doc.toObject(Dealer::class.java) ?: return Resource.Error("Galeri profili okunamadı")
                if (dealer.galleryName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                    dealer.id == "dealer_anadolu_01" ||
                    dealer.id.lowercase().contains("anadolu")
                ) {
                    return Resource.Error("Galeri bulunamadı")
                }
                LocalStore.dealers[dealer.id] = dealer
                Resource.Success(dealer)
            } else {
                Resource.Error("Galeri bulunamadı")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galeri yüklenirken hata oluştu")
        }
    }

    suspend fun getAllDealers(status: DealerStatus? = null): Resource<List<Dealer>> {
        return try {
            val query = if (status != null) {
                firestore.collection("dealers").whereEqualTo("accountStatus", status.name)
            } else {
                firestore.collection("dealers")
            }
            val snapshot = query.get().await()
            val dealers = snapshot.toObjects(Dealer::class.java)
            dealers.forEach { LocalStore.dealers[it.id] = it }

            val localDealers = LocalStore.dealers.values.toList()
            val combined = (dealers + localDealers).distinctBy { it.id }
                .filterNot {
                    it.galleryName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.id == "dealer_anadolu_01" ||
                            it.id.lowercase().contains("anadolu")
                }
            val filtered = if (status != null) {
                combined.filter { it.accountStatus == status }
            } else {
                combined
            }
            Resource.Success(filtered)
        } catch (e: Exception) {
            val localDealers = LocalStore.dealers.values.toList()
                .filterNot {
                    it.galleryName.contains("Anadolu Otomotiv", ignoreCase = true) ||
                            it.id == "dealer_anadolu_01" ||
                            it.id.lowercase().contains("anadolu")
                }
            val filtered = if (status != null) {
                localDealers.filter { it.accountStatus == status }
            } else {
                localDealers
            }
            Resource.Success(filtered)
        }
    }

    suspend fun updateDealerStatus(dealerId: String, status: DealerStatus, rejectionReason: String = ""): Resource<Unit> {
        return try {
            val updates = mapOf(
                "accountStatus" to status.name,
                "rejectionReason" to rejectionReason,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("dealers").document(dealerId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galeri durumu güncellenemedi")
        }
    }

    suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit> {
        return try {
            val updates = mapOf(
                "galleryName" to dealer.galleryName,
                "authorizedName" to dealer.authorizedName,
                "phone" to dealer.phone,
                "city" to dealer.city,
                "district" to dealer.district,
                "address" to dealer.address,
                "taxNumber" to dealer.taxNumber,
                "iban" to dealer.iban,
                "ibanOwnerName" to dealer.ibanOwnerName,
                "description" to dealer.description,
                "workingHours" to dealer.workingHours,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("dealers").document(dealer.id).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galeri profili güncellenemedi")
        }
    }

    suspend fun updateDealerLogo(context: Any? = null, dealerId: String, logoUri: Any): Resource<String> {
        return try {
            val compressedBytes = ImageCompressor.compressImage(context, logoUri)
            if (compressedBytes.isEmpty()) return Resource.Error("Fotoğraf işlenemedi")

            val ref = storage.reference.child("dealer_logos/$dealerId.jpg")
            val taskSnapshot = ref.putBytes(compressedBytes).await()
            val downloadUrl = try {
                ref.downloadUrl.await().toString()
            } catch (e: Exception) {
                try {
                    taskSnapshot.storage.downloadUrl.await().toString()
                } catch (e2: Exception) {
                    logoUri.toString()
                }
            }

            firestore.collection("dealers").document(dealerId).update("logoUrl", downloadUrl).await()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Logo güncellenirken hata oluştu")
        }
    }
}
