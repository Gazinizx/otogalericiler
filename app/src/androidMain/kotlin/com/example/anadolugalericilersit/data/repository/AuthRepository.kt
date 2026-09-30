package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

actual class AuthRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    actual fun isUserLoggedIn(): Boolean {
        if (LocalStore.currentLoggedInUid != null) return true
        val savedUid = SessionManager.getSavedUid()
        return !savedUid.isNullOrBlank()
    }

    fun getCurrentUid(): String? {
        return LocalStore.currentLoggedInUid ?: SessionManager.getSavedUid()
    }

    actual suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val cleanEmail = email.trim()
            val cleanPass = password.trim()
            val normalizedInput = cleanEmail.lowercase().replace(" ", "")

            if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                return Resource.Error("E-posta/Telefon ve şifre boş bırakılamaz")
            }

            val isAdmin1Input = normalizedInput == "europexpert38@gmail.com" ||
                    normalizedInput == "europexpert38"

            if (isAdmin1Input) {
                if (cleanPass != "369369") {
                    return Resource.Error("Girilen şifre hatalı!")
                }

                val adminUser = User(
                    uid = "admin_europexpert",
                    email = "europexpert38@gmail.com",
                    password = "369369",
                    name = "EuropExpert Admin",
                    role = Role.SUPER_ADMIN,
                    canIssueDamga = true
                )
                LocalStore.users["admin_europexpert"] = adminUser
                LocalStore.currentLoggedInUid = adminUser.uid
                SessionManager.saveSession(adminUser.uid)

                try {
                    firestore.collection("users").document(adminUser.uid).set(adminUser).await()
                } catch (_: Exception) {
                    // ignore firestore sync errors offline
                }

                return Resource.Success(adminUser)
            }

            val isAdmin2Input = normalizedInput == "gazitasdemir46@gmail.com" ||
                    normalizedInput == "gazitasdemir46"

            if (isAdmin2Input) {
                if (cleanPass != "369369") {
                    return Resource.Error("Girilen şifre hatalı!")
                }

                val adminUser = User(
                    uid = "admin_gazitasdemir",
                    email = "gazitasdemir46@gmail.com",
                    password = "369369",
                    name = "Gazi Taşdemir Admin",
                    role = Role.SUPER_ADMIN,
                    canIssueDamga = true
                )
                LocalStore.users["admin_gazitasdemir"] = adminUser
                LocalStore.currentLoggedInUid = adminUser.uid
                SessionManager.saveSession(adminUser.uid)

                try {
                    firestore.collection("users").document(adminUser.uid).set(adminUser).await()
                } catch (_: Exception) {
                    // ignore firestore sync errors offline
                }

                return Resource.Success(adminUser)
            }

            val localUser = LocalStore.users.values.find {
                it.email.equals(cleanEmail, ignoreCase = true) ||
                        it.uid == cleanEmail
            }
            if (localUser != null) {
                if (localUser.password.isNotBlank() && localUser.password != cleanPass) {
                    return Resource.Error("Girilen şifre hatalı!")
                }
                LocalStore.currentLoggedInUid = localUser.uid
                SessionManager.saveSession(localUser.uid)
                return Resource.Success(localUser)
            }

            try {
                val queryByEmail = firestore.collection("users")
                    .whereEqualTo("email", cleanEmail)
                    .get()
                    .await()

                var doc = queryByEmail.documents.firstOrNull()

                if (doc == null || !doc.exists()) {
                    val dealerQuery = firestore.collection("dealers")
                        .whereEqualTo("email", cleanEmail)
                        .get()
                        .await()
                    doc = dealerQuery.documents.firstOrNull()
                }

                if (doc != null && doc.exists()) {
                    var user = doc.toObject(User::class.java)
                    if (user != null) {
                        if (user.password.isNotBlank() && user.password != cleanPass) {
                            return Resource.Error("Girilen şifre hatalı!")
                        }

                        if (user.email == "europexpert38@gmail.com" ||
                            user.email == "gazitasdemir46@gmail.com"
                        ) {
                            user = user.copy(role = Role.SUPER_ADMIN, canIssueDamga = true)
                        }

                        LocalStore.users[user.uid] = user
                        LocalStore.currentLoggedInUid = user.uid
                        SessionManager.saveSession(user.uid)

                        val dealerId = user.dealerId
                        if (!dealerId.isNullOrBlank()) {
                            val dealerDoc = firestore.collection("dealers").document(dealerId).get().await()
                            if (dealerDoc.exists()) {
                                val dealer = dealerDoc.toObject(Dealer::class.java)
                                if (dealer != null) {
                                    LocalStore.dealers[dealerId] = dealer
                                }
                            }
                        }

                        return Resource.Success(user)
                    }
                }
            } catch (_: Exception) {
                // proceed if query fails
            }

            Resource.Error("Girilen e-posta adresi veya kullanıcı bulunamadı! Lütfen önce kayıt olun.")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Giriş yapılırken hata oluştu")
        }
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
        return try {
            val uid = UUID.randomUUID().toString()
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
                password = hashPassword(password),
                name = galleryName,
                role = Role.DEALER,
                dealerId = uid
            )

            LocalStore.users[uid] = user
            LocalStore.dealers[uid] = dealer
            LocalStore.currentLoggedInUid = uid
            SessionManager.saveSession(uid, context)

            try {
                firestore.collection("dealers").document(uid).set(dealer).await()
                firestore.collection("users").document(uid).set(user).await()
            } catch (_: Exception) {
                // ignore firestore sync errors if offline
            }

            Resource.Success(dealer)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Kayıt olunurken hata oluştu")
        }
    }

    actual suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şifre sıfırlama başarısız")
        }
    }

    actual suspend fun checkSession(): Resource<User> {
        return getCurrentUser()
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

        return try {
            val doc = firestore.collection("users").document(safeUid).get().await()
            if (doc.exists()) {
                var user = doc.toObject(User::class.java)
                if (user != null) {
                    if (user.email == "europexpert38@gmail.com" ||
                        user.email == "gazitasdemir46@gmail.com"
                    ) {
                        user = user.copy(role = Role.SUPER_ADMIN, canIssueDamga = true)
                    }

                    LocalStore.users[safeUid] = user
                    LocalStore.currentLoggedInUid = safeUid
                    return Resource.Success(user)
                }
            }
            Resource.Error("Kullanıcı bulunamadı")
        } catch (_: Exception) {
            Resource.Error("Kullanıcı oturumu yüklenemedi")
        }
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

        return try {
            val doc = firestore.collection("dealers").document(safeUid).get().await()
            if (doc.exists()) {
                val dealer = doc.toObject(Dealer::class.java)
                if (dealer != null) {
                    LocalStore.dealers[safeUid] = dealer
                    return Resource.Success(dealer)
                }
            }
            Resource.Error("Galeri bilgisi bulunamadı")
        } catch (e: Exception) {
            Resource.Error("Galeri profili yüklenemedi")
        }
    }

    actual fun logout() {
        LocalStore.currentLoggedInUid = null
        SessionManager.clearSession()
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

    private fun hashPassword(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input
        }
    }
}
