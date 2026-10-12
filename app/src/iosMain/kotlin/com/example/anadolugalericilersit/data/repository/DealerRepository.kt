package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.DebtTransaction
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.utils.FirestoreRestClient
import com.example.anadolugalericilersit.utils.FirestoreSync
import com.example.anadolugalericilersit.utils.Resource

actual class DealerRepository actual constructor() {
    actual suspend fun getAllDealers(statusFilter: DealerStatus?): Resource<List<Dealer>> {
        return getDealers(statusFilter)
    }

    actual suspend fun getDealers(status: DealerStatus?): Resource<List<Dealer>> {
        val cloudDealers = FirestoreRestClient.fetchDealersFromCloud()
        val filtered = if (status != null) cloudDealers.filter { it.accountStatus == status } else cloudDealers
        return Resource.Success(filtered)
    }

    actual suspend fun getDealerById(dealerId: String): Resource<Dealer> {
        val cloudDealers = FirestoreRestClient.fetchDealersFromCloud()
        val dealer = cloudDealers.find { it.id == dealerId } ?: return Resource.Error("Galeri bulunamadı")
        return Resource.Success(dealer)
    }

    actual suspend fun updateDealerStatus(dealerId: String, newStatus: DealerStatus, rejectionReason: String): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        val updated = dealer.copy(accountStatus = newStatus, rejectionReason = rejectionReason)
        LocalStore.dealers[dealerId] = updated
        try {
            val fields = """
                {
                  "accountStatus": { "stringValue": "${updated.accountStatus.name}" },
                  "rejectionReason": { "stringValue": "${updated.rejectionReason}" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
        } catch (_: Exception) {}
        return Resource.Success(Unit)
    }

    actual suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit> {
        LocalStore.dealers[dealer.id] = dealer
        try {
            val fields = """
                {
                  "galleryName": { "stringValue": "${dealer.galleryName}" },
                  "authorizedName": { "stringValue": "${dealer.authorizedName}" },
                  "phone": { "stringValue": "${dealer.phone}" },
                  "city": { "stringValue": "${dealer.city}" },
                  "district": { "stringValue": "${dealer.district}" },
                  "address": { "stringValue": "${dealer.address}" },
                  "taxNumber": { "stringValue": "${dealer.taxNumber}" },
                  "iban": { "stringValue": "${dealer.iban}" },
                  "ibanOwnerName": { "stringValue": "${dealer.ibanOwnerName}" },
                  "description": { "stringValue": "${dealer.description}" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealer.id, fields)
        } catch (_: Exception) {}
        return Resource.Success(Unit)
    }

    actual suspend fun updateDealerLogo(context: Any?, dealerId: String, logoUri: Any): Resource<String> {
        val url = logoUri.toString()
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        LocalStore.dealers[dealerId] = dealer.copy(logoUrl = url)
        try {
            val fields = """
                {
                  "logoUrl": { "stringValue": "$url" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
        } catch (_: Exception) {}
        return Resource.Success(url)
    }

    actual suspend fun recordDebtOrPayment(
        dealerId: String,
        amount: Double,
        isDebt: Boolean,
        note: String
    ): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        val newDebt = if (isDebt) dealer.totalDebt + amount else (dealer.totalDebt - amount).coerceAtLeast(0.0)
        val updated = dealer.copy(totalDebt = newDebt)
        LocalStore.dealers[dealerId] = updated
        try {
            val fields = """
                {
                  "totalDebt": { "doubleValue": $newDebt }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
        } catch (_: Exception) {}
        return Resource.Success(Unit)
    }

    actual suspend fun getDebtTransactions(dealerId: String): Resource<List<DebtTransaction>> {
        val list = LocalStore.debtTransactions[dealerId] ?: emptyList()
        return Resource.Success(list)
    }

    actual suspend fun updateDamgaCount(dealerId: String, newDamgaCount: Int): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        val updated = dealer.copy(damgaCount = newDamgaCount)
        LocalStore.dealers[dealerId] = updated
        try {
            val fields = """
                {
                  "damgaCount": { "integerValue": "$newDamgaCount" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
        } catch (_: Exception) {}
        return Resource.Success(Unit)
    }
}
