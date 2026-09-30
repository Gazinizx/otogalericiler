package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.utils.Resource

expect class AuthRepository() {
    fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): Resource<User>
    suspend fun getCurrentDealerProfile(): Resource<Dealer>
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun checkSession(): Resource<User>
    fun logout()
    suspend fun registerDealer(
        context: Any? = null,
        galleryName: String,
        authorizedName: String,
        phone: String,
        email: String,
        password: String,
        city: String,
        district: String,
        address: String,
        taxNumber: String,
        iban: String,
        ibanOwnerName: String,
        description: String,
        logoUri: Any? = null
    ): Resource<Dealer>
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>
}
