package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.DebtTransaction
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.utils.Resource

actual class DealerRepository actual constructor() {
    actual suspend fun getAllDealers(statusFilter: DealerStatus?): Resource<List<Dealer>> {
        return getDealers(statusFilter)
    }

    actual suspend fun getDealers(status: DealerStatus?): Resource<List<Dealer>> {
        val all = LocalStore.dealers.values.toList()
        val filtered = if (status != null) all.filter { it.accountStatus == status } else all
        return Resource.Success(filtered)
    }

    actual suspend fun getDealerById(dealerId: String): Resource<Dealer> {
        val dealer = LocalStore.dealers[dealerId] ?: return Resource.Error("Galeri bulunamadı")
        return Resource.Success(dealer)
    }

    actual suspend fun updateDealerStatus(dealerId: String, newStatus: DealerStatus, rejectionReason: String): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId] ?: return Resource.Error("Galeri bulunamadı")
        LocalStore.dealers[dealerId] = dealer.copy(accountStatus = newStatus, rejectionReason = rejectionReason)
        return Resource.Success(Unit)
    }

    actual suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit> {
        LocalStore.dealers[dealer.id] = dealer
        return Resource.Success(Unit)
    }

    actual suspend fun updateDealerLogo(context: Any?, dealerId: String, logoUri: Any): Resource<String> {
        val url = logoUri.toString()
        val dealer = LocalStore.dealers[dealerId]
        if (dealer != null) {
            LocalStore.dealers[dealerId] = dealer.copy(logoUrl = url)
        }
        return Resource.Success(url)
    }

    actual suspend fun recordDebtOrPayment(
        dealerId: String,
        amount: Double,
        isDebt: Boolean,
        note: String
    ): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId] ?: return Resource.Error("Galeri bulunamadı")
        val newDebt = if (isDebt) dealer.totalDebt + amount else (dealer.totalDebt - amount).coerceAtLeast(0.0)
        LocalStore.dealers[dealerId] = dealer.copy(totalDebt = newDebt)
        return Resource.Success(Unit)
    }

    actual suspend fun getDebtTransactions(dealerId: String): Resource<List<DebtTransaction>> {
        val list = LocalStore.debtTransactions[dealerId] ?: emptyList()
        return Resource.Success(list)
    }

    actual suspend fun updateDamgaCount(dealerId: String, newDamgaCount: Int): Resource<Unit> {
        val dealer = LocalStore.dealers[dealerId]
        if (dealer != null) {
            LocalStore.dealers[dealerId] = dealer.copy(damgaCount = newDamgaCount)
        }
        return Resource.Success(Unit)
    }
}
