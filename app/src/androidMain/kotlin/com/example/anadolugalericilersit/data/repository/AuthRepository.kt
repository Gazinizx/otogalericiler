package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

actual class AuthRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    actual fun isUserLoggedIn(): Boolean {
        if (FirebaseAuth.getInstance().currentUser != null) return true
        if (LocalStore.currentLoggedInUid != null) return true
        val savedUid = SessionManager.getSavedUid()
        return !savedUid.isNullOrBlank()
    }

    fun getCurrentUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: LocalStore.currentLoggedInUid
            ?: SessionManager.getSavedUid()
    }

    actual suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val cleanEmail = email.trim()
            val cleanPass = password.trim()

            if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                return Resource.Error("E-posta ve şifre boş bırakılamaz")
            }

            // 1. Authenticate with Firebase Auth
            val authResult = try {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            } catch (e: Exception) {
                val rawMsg = e.message?.lowercase() ?: ""
                val msg = when {
                    rawMsg.contains("invalid-credential") || rawMsg.contains("wrong-password") || rawMsg.contains("user-not-found") || rawMsg.contains("no user record") || rawMsg.contains("invalid credential") -> "Geçersiz e-posta adresi veya şifre."
                    rawMsg.contains("invalid-email") || rawMsg.contains("badly formatted") -> "Geçersiz e-posta adresi biçimi."
                    rawMsg.contains("user-disabled") -> "Bu kullanıcı hesabı askıya alınmıştır."
                    rawMsg.contains("too-many-requests") -> "Çok fazla hatalı deneme yapıldı. Lütfen birkaç dakika sonra tekrar deneyiniz."
                    rawMsg.contains("network") || rawMsg.contains("connection") -> "İnternet bağlantısı bulunamadı. Lütfen bağlantınızı kontrol ediniz."
                    else -> "Geçersiz e-posta adresi veya şifre."
                }
                return Resource.Error(msg)
            }

            val firebaseUser = authResult.user ?: return Resource.Error("Kullanıcı oturumu açılamadı")
            val uid = firebaseUser.uid

            // 2. Fetch user profile from Firestore users/{uid}
            val userDoc = firestore.collection("users").document(uid).get().await()
            var user = userDoc.toObject(User::class.java)

            if (user == null) {
                user = User(
                    uid = uid,
                    email = cleanEmail,
                    password = "", // Never store password in Firestore
                    name = cleanEmail,
                    role = Role.DEALER,
                    dealerId = uid
                )
                firestore.collection("users").document(uid).set(user).await()
            }

            LocalStore.users[uid] = user
            LocalStore.currentLoggedInUid = uid
            SessionManager.saveSession(uid)

            // Load dealer profile if dealer
            if (user.role == Role.DEALER) {
                val dealerDoc = firestore.collection("dealers").document(uid).get().await()
                if (dealerDoc.exists()) {
                    val dealer = dealerDoc.toObject(Dealer::class.java)
                    if (dealer != null) {
                        LocalStore.dealers[uid] = dealer
                    }
                }
            }

            Resource.Success(user)
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
            val cleanEmail = email.trim()
            val cleanPass = password.trim()

            if (cleanEmail.isBlank() || cleanPass.isBlank() || galleryName.isBlank()) {
                return Resource.Error("Lütfen e-posta, şifre ve galeri adını doldurun")
            }
            if (cleanPass.length < 6) {
                return Resource.Error("Şifre en az 6 karakter olmalıdır")
            }

            // 1. Register with Firebase Authentication
            val authResult = try {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            } catch (e: Exception) {
                val rawMsg = e.message?.lowercase() ?: ""
                val msg = when {
                    rawMsg.contains("email-already-in-use") || rawMsg.contains("already in use") -> "Bu e-posta adresi zaten kullanımda."
                    rawMsg.contains("weak-password") || rawMsg.contains("weak password") -> "Şifre çok zayıf (en az 6 karakter olmalıdır)."
                    rawMsg.contains("invalid-email") || rawMsg.contains("badly formatted") -> "Geçersiz e-posta adresi biçimi."
                    rawMsg.contains("network") || rawMsg.contains("connection") -> "İnternet bağlantısı bulunamadı."
                    else -> "Kayıt olunurken bir hata oluştu. Lütfen bilgilerinizi kontrol ediniz."
                }
                return Resource.Error(msg)
            }

            val firebaseUser = authResult.user ?: return Resource.Error("Kullanıcı oluşturulamadı")
            val uid = firebaseUser.uid

            val dealer = Dealer(
                id = uid,
                ownerUid = uid,
                galleryName = galleryName,
                authorizedName = authorizedName,
                phone = phone,
                email = cleanEmail,
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

            // CRITICAL: NEVER store password in Firestore!
            val user = User(
                uid = uid,
                email = cleanEmail,
                password = "", 
                name = galleryName,
                role = Role.DEALER,
                dealerId = uid
            )

            LocalStore.users[uid] = user
            LocalStore.dealers[uid] = dealer
            LocalStore.currentLoggedInUid = uid
            SessionManager.saveSession(uid, context)

            // Save profile data to Firestore
            firestore.collection("dealers").document(uid).set(dealer).await()
            firestore.collection("users").document(uid).set(user).await()

            Resource.Success(dealer)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Kayıt olunurken hata oluştu")
        }
    }

    actual suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            val cleanEmail = email.trim()
            if (cleanEmail.isBlank()) {
                return Resource.Error("Lütfen e-posta adresinizi giriniz.")
            }
            FirebaseAuth.getInstance().sendPasswordResetEmail(cleanEmail).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            val rawMsg = e.message?.lowercase() ?: ""
            val msg = when {
                rawMsg.contains("user-not-found") || rawMsg.contains("no user record") -> "Bu e-posta adresiyle kayıtlı bir kullanıcı bulunamadı."
                rawMsg.contains("invalid-email") || rawMsg.contains("badly formatted") -> "Geçersiz e-posta adresi biçimi."
                else -> e.localizedMessage ?: "Şifre sıfırlama e-postası gönderilemedi."
            }
            Resource.Error(msg)
        }
    }

    actual suspend fun checkSession(): Resource<User> {
        return getCurrentUser()
    }

    actual suspend fun getCurrentUser(): Resource<User> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: LocalStore.currentLoggedInUid
            ?: SessionManager.getSavedUid()

        if (uid.isNullOrBlank()) return Resource.Error("Oturum açık değil")
        val safeUid: String = uid

        val localUser = LocalStore.users[safeUid]
        if (localUser != null) {
            LocalStore.currentLoggedInUid = safeUid
            return Resource.Success(localUser)
        }

        return try {
            val doc = firestore.collection("users").document(safeUid).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                if (user != null) {
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: LocalStore.currentLoggedInUid
            ?: SessionManager.getSavedUid()

        if (uid.isNullOrBlank()) return Resource.Error("Oturum açık değil")
        val safeUid: String = uid

        val user = LocalStore.users[safeUid]
        if (user?.role == Role.SUPER_ADMIN || user?.role == Role.ADMIN) {
            return Resource.Error("Admin kullanıcısının galeri profili yoktur")
        }

        return try {
            val doc = firestore.collection("dealers").document(safeUid).get().await()
            if (doc.exists()) {
                val dealer = doc.toObject(Dealer::class.java)
                if (dealer != null) {
                    LocalStore.dealers[safeUid] = dealer
                    return Resource.Success(dealer)
                }
            }
            val localDealer = LocalStore.dealers[safeUid]
            if (localDealer != null) return Resource.Success(localDealer)
            Resource.Error("Galeri bilgisi bulunamadı")
        } catch (e: Exception) {
            val localDealer = LocalStore.dealers[safeUid]
            if (localDealer != null) return Resource.Success(localDealer)
            Resource.Error("Galeri profili yüklenemedi")
        }
    }

    actual fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        LocalStore.currentLoggedInUid = null
        SessionManager.clearSession()
    }

    actual suspend fun deleteAccount(): Resource<Unit> {
        val uid = getCurrentUid()
        if (!uid.isNullOrBlank()) {
            LocalStore.users.remove(uid)
            LocalStore.dealers.remove(uid)
        }
        try {
            FirebaseAuth.getInstance().currentUser?.delete()?.await()
        } catch (_: Exception) {}
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
