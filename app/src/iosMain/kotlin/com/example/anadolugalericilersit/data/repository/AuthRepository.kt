package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.SessionManager
import com.example.anadolugalericilersit.utils.generateUuid

actual class AuthRepository actual constructor() {

    actual fun isUserLoggedIn(): Boolean {
        if (LocalStore.currentLoggedInUid != null) return true
        val savedUid = SessionManager.getSavedUid()
        return !savedUid.isNullOrBlank()
    }

    actual suspend fun getCurrentUser(): Resource<User> {
        val uid = LocalStore.currentLoggedInUid ?: SessionManager.getSavedUid()
        if (uid.isNullOrBlank()) return Resource.Error("Oturum açık değil")
        val safeUid: String = uid

        val localUser = LocalStore.users[safeUid]
        if (localUser != null) {
            LocalStore.currentLoggedInUid = safeUid
            if (localUser.email == "europexpert38@gmail.com" ||
                localUser.email == "gazitasdemir46@gmail.com" ||
                safeUid.startsWith("admin")
            ) {
                val updated = localUser.copy(role = Role.SUPER_ADMIN, canIssueDamga = true)
                LocalStore.users[safeUid] = updated
                return Resource.Success(updated)
            }
            return Resource.Success(localUser)
        }

        if (safeUid == "admin_europexpert" || safeUid == "admin_gazitasdemir") {
            val is1 = safeUid == "admin_europexpert"
            val adminUser = User(
                uid = safeUid,
                email = if (is1) "europexpert38@gmail.com" else "gazitasdemir46@gmail.com",
                name = if (is1) "EuropExpert Admin" else "Gazi Taşdemir Admin",
                role = Role.SUPER_ADMIN,
                canIssueDamga = true
            )
            LocalStore.users[safeUid] = adminUser
            LocalStore.currentLoggedInUid = safeUid
            return Resource.Success(adminUser)
        }

        return Resource.Error("Kullanıcı oturumu yüklenemedi")
    }

    actual suspend fun getCurrentDealerProfile(): Resource<Dealer> {
        val uid = LocalStore.currentLoggedInUid ?: SessionManager.getSavedUid()
        if (uid.isNullOrBlank()) return Resource.Error("Oturum açık değil")
        val safeUid: String = uid

        val user = LocalStore.users[safeUid]
        if (user?.role == Role.SUPER_ADMIN ||
            user?.role == Role.ADMIN ||
            user?.email == "europexpert38@gmail.com" ||
            user?.email == "gazitasdemir46@gmail.com" ||
            safeUid.startsWith("admin")
        ) {
            return Resource.Error("Admin kullanıcısının galeri profili yoktur")
        }

        val localDealer = LocalStore.dealers[safeUid]
        if (localDealer != null) return Resource.Success(localDealer)

        return Resource.Error("Galeri profili yüklenemedi")
    }

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
        return getCurrentUser()
    }

    actual fun logout() {
        LocalStore.currentLoggedInUid = null
        SessionManager.clearSession()
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
