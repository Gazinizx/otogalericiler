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
        return try {
            val fields = """
                {
                  "accountStatus": { "stringValue": ${jsonQuote(updated.accountStatus.name)} },
                  "rejectionReason": { "stringValue": ${jsonQuote(updated.rejectionReason)} }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
            LocalStore.dealers[dealerId] = updated
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Galeri durumu buluta kaydedilemedi")
        }
    }

    actual suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit> {
        return try {
            val fields = """
                {
                  "galleryName": { "stringValue": ${jsonQuote(dealer.galleryName)} },
                  "authorizedName": { "stringValue": ${jsonQuote(dealer.authorizedName)} },
                  "phone": { "stringValue": ${jsonQuote(dealer.phone)} },
                  "city": { "stringValue": ${jsonQuote(dealer.city)} },
                  "district": { "stringValue": ${jsonQuote(dealer.district)} },
                  "address": { "stringValue": ${jsonQuote(dealer.address)} },
                  "taxNumber": { "stringValue": ${jsonQuote(dealer.taxNumber)} },
                  "iban": { "stringValue": ${jsonQuote(dealer.iban)} },
                  "ibanOwnerName": { "stringValue": ${jsonQuote(dealer.ibanOwnerName)} },
                  "description": { "stringValue": ${jsonQuote(dealer.description)} }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealer.id, fields)
            LocalStore.dealers[dealer.id] = dealer
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Galeri profili buluta kaydedilemedi")
        }
    }

    actual suspend fun updateDealerLogo(context: Any?, dealerId: String, logoUri: Any): Resource<String> {
        val url = logoUri.toString()
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        return try {
            val fields = """
                {
                  "logoUrl": { "stringValue": ${jsonQuote(url)} }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
            LocalStore.dealers[dealerId] = dealer.copy(logoUrl = url)
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Galeri logosu buluta kaydedilemedi")
        }
    }

    actual suspend fun recordDebtOrPayment(
        dealerId: String,
        amount: Double,
        isDebt: Boolean,
        note: String
    ): Resource<Unit> {
        if (!amount.isFinite() || amount <= 0.0) return Resource.Error("Geçerli bir tutar girin")
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        val newDebt = if (isDebt) dealer.totalDebt + amount else (dealer.totalDebt - amount).coerceAtLeast(0.0)
        val updated = dealer.copy(totalDebt = newDebt)
        return try {
            val fields = """
                {
                  "totalDebt": { "doubleValue": $newDebt }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
            LocalStore.dealers[dealerId] = updated
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Borç bilgisi buluta kaydedilemedi")
        }
    }

    actual suspend fun getDebtTransactions(dealerId: String): Resource<List<DebtTransaction>> {
        val list = LocalStore.debtTransactions[dealerId] ?: emptyList()
        return Resource.Success(list)
    }

    actual suspend fun updateDamgaCount(dealerId: String, newDamgaCount: Int): Resource<Unit> {
        if (newDamgaCount < 0) return Resource.Error("Damga sayısı negatif olamaz")
        val dealer = LocalStore.dealers[dealerId] ?: Dealer(id = dealerId)
        val updated = dealer.copy(damgaCount = newDamgaCount)
        return try {
            val fields = """
                {
                  "damgaCount": { "integerValue": "$newDamgaCount" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("dealers", dealerId, fields)
            LocalStore.dealers[dealerId] = updated
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Damga sayısı buluta kaydedilemedi")
        }
    }
    private fun jsonQuote(value: String): String = buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u")
                    append(ch.code.toString(16).padStart(4, '0'))
                } else append(ch)
            }
        }
        append('"')
    }

}
