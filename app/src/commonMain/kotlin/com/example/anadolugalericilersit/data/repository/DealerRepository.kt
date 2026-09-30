package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.utils.Resource

expect class DealerRepository() {
    suspend fun getAllDealers(statusFilter: DealerStatus? = null): Resource<List<Dealer>>
    suspend fun getDealers(status: DealerStatus? = null): Resource<List<Dealer>>
    suspend fun getDealerById(dealerId: String): Resource<Dealer>
    suspend fun updateDealerStatus(dealerId: String, newStatus: DealerStatus, rejectionReason: String = ""): Resource<Unit>
    suspend fun updateDealerProfile(dealer: Dealer): Resource<Unit>
    suspend fun updateDealerLogo(context: Any? = null, dealerId: String, logoUri: Any): Resource<String>
    suspend fun recordDebtOrPayment(dealerId: String, amount: Double, isDebt: Boolean, note: String): Resource<Unit>
}
