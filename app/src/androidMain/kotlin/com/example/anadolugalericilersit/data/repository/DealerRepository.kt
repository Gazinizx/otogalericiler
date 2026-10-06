package com.example.anadolugalericilersit.data.repository

import android.util.Base64
import com.example.anadolugalericilersit.data.local.DebtTransaction
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.TransactionType
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.utils.CloudinaryUploader
import com.example.anadolugalericilersit.utils.ImageCompressor
import com.example.anadolugalericilersit.utils.ImgBBUploader
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

actual class DealerRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    actual suspend fun getDealerById(dealerId: String): Resource<Dealer> {
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

    actual suspend fun getAllDealers(statusFilter: DealerStatus?): Resource<List<Dealer>> {
        return getDealers(statusFilter)
    }

    actual suspend fun getDealers(status: DealerStatus?): Resource<List<Dealer>> {
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

    actual suspend fun updateDealerStatus(dealerId: String, newStatus: DealerStatus, rejectionReason: String): Resource<Unit> {
        return try {
            val updates = mapOf(
                "accountStatus" to newStatus.name,
                "rejectionReason" to rejectionReason,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("dealers").document(dealerId).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galeri durumu güncellenemedi")
        }
    }

    actual suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit> {
        return try {
            var finalProfileUrl = dealer.profilePhotoUrl
            var finalShopUrl = dealer.shopPhotoUrl
            var finalLogoUrl = dealer.logoUrl

            if (finalProfileUrl.isNotBlank() && !finalProfileUrl.startsWith("http")) {
                val compressedBytes = ImageCompressor.compressImage(null, finalProfileUrl, 65)
                if (compressedBytes.isNotEmpty()) {
                    var uploaded: String? = ImgBBUploader.uploadImageBytes(compressedBytes)
                    if (uploaded.isNullOrBlank()) {
                        uploaded = CloudinaryUploader.uploadImageBytes(compressedBytes)
                    }
                    if (uploaded.isNullOrBlank()) {
                        val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                        uploaded = "data:image/jpeg;base64,$base64Str"
                    }
                    if (!uploaded.isNullOrBlank()) {
                        finalProfileUrl = uploaded
                    }
                }
            }

            if (finalShopUrl.isNotBlank() && !finalShopUrl.startsWith("http")) {
                val compressedBytes = ImageCompressor.compressImage(null, finalShopUrl, 65)
                if (compressedBytes.isNotEmpty()) {
                    var uploaded: String? = ImgBBUploader.uploadImageBytes(compressedBytes)
                    if (uploaded.isNullOrBlank()) {
                        uploaded = CloudinaryUploader.uploadImageBytes(compressedBytes)
                    }
                    if (uploaded.isNullOrBlank()) {
                        val base64Str = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                        uploaded = "data:image/jpeg;base64,$base64Str"
                    }
                    if (!uploaded.isNullOrBlank()) {
                        finalShopUrl = uploaded
                    }
                }
            }

            if (finalLogoUrl.isBlank() || !finalLogoUrl.startsWith("http")) {
                finalLogoUrl = if (finalShopUrl.isNotBlank()) finalShopUrl else if (finalProfileUrl.isNotBlank()) finalProfileUrl else dealer.logoUrl
            }

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
                "profilePhotoUrl" to finalProfileUrl,
                "shopPhotoUrl" to finalShopUrl,
                "logoUrl" to finalLogoUrl,
                "updatedAt" to System.currentTimeMillis()
            )

            val updatedDealer = dealer.copy(
                profilePhotoUrl = finalProfileUrl,
                shopPhotoUrl = finalShopUrl,
                logoUrl = finalLogoUrl
            )
            LocalStore.dealers[dealer.id] = updatedDealer

            firestore.collection("dealers").document(dealer.id).update(updates).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Galeri profili güncellenemedi")
        }
    }

    actual suspend fun updateDealerLogo(context: Any?, dealerId: String, logoUri: Any): Resource<String> {
        return try {
            val compressedBytes = ImageCompressor.compressImage(context, logoUri)
            if (compressedBytes.isEmpty()) return Resource.Error("Fotoğraf işlenemedi")

            val ref = storage.reference.child("dealer_logos/$dealerId.jpg")
            val taskSnapshot = ref.putBytes(compressedBytes).await()
            val downloadUrl = try {
                taskSnapshot.metadata?.reference?.downloadUrl?.await()?.toString()
                    ?: ref.downloadUrl.await().toString()
            } catch (e: Exception) {
                logoUri.toString()
            }

            firestore.collection("dealers").document(dealerId).update("logoUrl", downloadUrl).await()
            Resource.Success(downloadUrl)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Logo güncellenirken hata oluştu")
        }
    }

    actual suspend fun recordDebtOrPayment(
        dealerId: String,
        amount: Double,
        isDebt: Boolean,
        note: String
    ): Resource<Unit> {
        return try {
            val doc = firestore.collection("dealers").document(dealerId).get().await()
            var dealer = doc.toObject(Dealer::class.java) ?: LocalStore.dealers[dealerId]
            if (dealer == null) return Resource.Error("Galeri bulunamadı")

            val newDebt = if (isDebt) dealer.totalDebt + amount else (dealer.totalDebt - amount).coerceAtLeast(0.0)
            val updatedDealer = dealer.copy(totalDebt = newDebt)
            LocalStore.dealers[dealerId] = updatedDealer

            // 1. Update dealer totalDebt in Firestore
            firestore.collection("dealers").document(dealerId).update("totalDebt", newDebt).await()

            // 2. Save debt transaction in Firestore & LocalStore
            val now = System.currentTimeMillis()
            val txType = if (isDebt) TransactionType.DEBT else TransactionType.PAYMENT
            val tx = DebtTransaction(
                dealerId = dealerId,
                amount = amount,
                description = note,
                type = txType,
                timestamp = now
            )
            val txList = LocalStore.debtTransactions.getOrPut(dealerId) { mutableListOf() }
            txList.add(0, tx)

            val txMap = mapOf(
                "dealerId" to dealerId,
                "amount" to amount,
                "description" to note,
                "type" to txType.name,
                "timestamp" to now
            )
            val txId = UUID.randomUUID().toString()
            firestore.collection("debt_transactions").document(txId).set(txMap).await()

            // 3. Create & send NotificationItem to Firestore & LocalStore
            val targetUserUid = updatedDealer.ownerUid.ifBlank { dealerId }
            val title = if (isDebt) "Hesabınıza Borç Eklendi" else "Ödeme Kaydı İşlendi"
            val message = "${updatedDealer.galleryName} hesabınıza ${amount.toInt()} ₺ ${if (isDebt) "borç eklenmiştir" else "ödeme kaydı düşülmüştür"}. Açıklama: $note"

            val notifId = UUID.randomUUID().toString()
            val notifItem = NotificationItem(
                id = notifId,
                userId = targetUserUid,
                title = title,
                message = message,
                type = "DEBT",
                targetId = dealerId,
                isRead = false,
                createdAt = now
            )
            firestore.collection("notifications").document(notifId).set(notifItem).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Borç kaydı güncellenemedi")
        }
    }

    actual suspend fun getDebtTransactions(dealerId: String): Resource<List<DebtTransaction>> {
        return try {
            val snap = firestore.collection("debt_transactions")
                .whereEqualTo("dealerId", dealerId)
                .get().await()
            val list = snap.documents.mapNotNull { doc ->
                val amount = doc.getDouble("amount") ?: 0.0
                val description = doc.getString("description") ?: ""
                val typeName = doc.getString("type") ?: "DEBT"
                val type = try { TransactionType.valueOf(typeName) } catch (_: Exception) { TransactionType.DEBT }
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                DebtTransaction(
                    dealerId = dealerId,
                    amount = amount,
                    description = description,
                    type = type,
                    timestamp = timestamp
                )
            }.sortedByDescending { it.timestamp }

            if (list.isNotEmpty()) {
                LocalStore.debtTransactions[dealerId] = list.toMutableList()
            }
            val localList = LocalStore.debtTransactions[dealerId] ?: emptyList()
            val combined = (list + localList).distinctBy { it.timestamp }
            Resource.Success(combined.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            val local = LocalStore.debtTransactions[dealerId] ?: emptyList()
            Resource.Success(local)
        }
    }

    actual suspend fun updateDamgaCount(dealerId: String, newDamgaCount: Int): Resource<Unit> {
        return try {
            firestore.collection("dealers").document(dealerId).update("damgaCount", newDamgaCount).await()
            val dealer = LocalStore.dealers[dealerId]
            if (dealer != null) {
                LocalStore.dealers[dealerId] = dealer.copy(damgaCount = newDamgaCount)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Damga sayısı güncellenemedi")
        }
    }
}
