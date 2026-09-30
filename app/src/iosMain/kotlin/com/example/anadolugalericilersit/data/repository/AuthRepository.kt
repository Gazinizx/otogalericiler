package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.SessionManager
import com.example.anadolugalericilersit.utils.generateUuid

actual class AuthRepository {

    actual suspend fun login(email: String, password: String): Resource<User> {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        val normalizedInput = cleanEmail.lowercase().replace(" ", "")

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            return Resource.Error("E-posta/Telefon ve şifre boş bırakılamaz")
        }

        if (normalizedInput == "europexpert38@gmail.com" || normalizedInput == "europexpert38") {
            if (cleanPass != "369369") return Resource.Error("Girilen şifre hatalı!")
            val user = User(
                uid = "admin_europexpert",
                email = "europexpert38@gmail.com",
                password = "369369",
                name = "EuropExpert Admin",
                role = Role.SUPER_ADMIN,
                canIssueDamga = true
            )
            LocalStore.users["admin_europexpert"] = user
            LocalStore.currentLoggedInUid = user.uid
            SessionManager.saveSession(user.uid)
            return Resource.Success(user)
        }

        if (normalizedInput == "gazitasdemir46@gmail.com" || normalizedInput == "gazitasdemir46") {
            if (cleanPass != "369369") return Resource.Error("Girilen şifre hatalı!")
            val user = User(
                uid = "admin_gazitasdemir",
                email = "gazitasdemir46@gmail.com",
                password = "369369",
                name = "Gazi Taşdemir Admin",
                role = Role.SUPER_ADMIN,
                canIssueDamga = true
            )
            LocalStore.users["admin_gazitasdemir"] = user
            LocalStore.currentLoggedInUid = user.uid
            SessionManager.saveSession(user.uid)
            return Resource.Success(user)
        }

        val localUser = LocalStore.users.values.find {
            it.email.equals(cleanEmail, ignoreCase = true) || it.uid == cleanEmail
        }
        if (localUser != null) {
            if (localUser.password.isNotBlank() && localUser.password != cleanPass) {
                return Resource.Error("Girilen şifre hatalı!")
            }
            LocalStore.currentLoggedInUid = localUser.uid
            SessionManager.saveSession(localUser.uid)
            return Resource.Success(localUser)
        }

        return Resource.Error("Girilen e-posta adresi veya kullanıcı bulunamadı! Lütfen önce kayıt olun.")
    }

    actual suspend fun checkSession(): Resource<User> {
        val uid = LocalStore.currentLoggedInUid ?: SessionManager.getSavedUid()
        if (uid.isNullOrBlank()) return Resource.Error("Oturum açık değil")
        val user = LocalStore.users[uid] ?: return Resource.Error("Kullanıcı bulunamadı")
        LocalStore.currentLoggedInUid = uid
        return Resource.Success(user)
    }

    actual suspend fun logout(): Resource<Unit> {
        LocalStore.currentLoggedInUid = null
        SessionManager.clearSession()
        return Resource.Success(Unit)
    }

    actual suspend fun registerDealer(
        context: Any?,
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
        logoUri: Any?
    ): Resource<Dealer> {
        val uid = generateUuid()
        val dealer = Dealer(
            id = uid,
            ownerUid = uid,
            galleryName = galleryName,
            authorizedName = authorizedName,
            phone = phone,
            email = email,
            city = city.ifBlank { "Kayseri" },
            district = district.ifBlank { "Kocasinan" },
            address = address,
            taxNumber = taxNumber,
            iban = iban,
            ibanOwnerName = ibanOwnerName,
            description = description,
            logoUrl = logoUri?.toString() ?: "",
            accountStatus = DealerStatus.PENDING
        )
        val user = User(
            uid = uid,
            email = email,
            password = password,
            name = galleryName,
            role = Role.DEALER,
            dealerId = uid
        )
        LocalStore.users[uid] = user
        LocalStore.dealers[uid] = dealer
        LocalStore.currentLoggedInUid = uid
        SessionManager.saveSession(uid)
        return Resource.Success(dealer)
    }

    actual suspend fun resetPassword(email: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    actual suspend fun deleteAccount(): Resource<Unit> {
        val uid = LocalStore.currentLoggedInUid ?: SessionManager.getSavedUid()
        if (!uid.isNullOrBlank()) {
            LocalStore.users.remove(uid)
            LocalStore.dealers.remove(uid)
        }
        LocalStore.currentLoggedInUid = null
        SessionManager.clearSession()
        return Resource.Success(Unit)
    }
}
