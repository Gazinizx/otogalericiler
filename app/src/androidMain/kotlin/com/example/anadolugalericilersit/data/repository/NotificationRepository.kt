package com.example.anadolugalericilersit.data.repository

import android.util.Base64
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.utils.CloudinaryUploader
import com.example.anadolugalericilersit.utils.ImageCompressor
import com.example.anadolugalericilersit.utils.ImgBBUploader
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

actual class NotificationRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    actual suspend fun getNotifications(userId: String): Resource<List<NotificationItem>> {
        return try {
            val snapshot1 = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get().await()
            val snapshot2 = firestore.collection("notifications")
                .whereEqualTo("userId", "all")
                .get().await()
            val list = (snapshot1.toObjects(NotificationItem::class.java) + snapshot2.toObjects(NotificationItem::class.java))
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirimler yüklenemedi")
        }
    }

    actual suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).update("isRead", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirim okundu olarak işaretlenemedi")
        }
    }

    actual suspend fun sendNotification(notification: NotificationItem): Resource<Unit> {
        return try {
            var finalImageUrl = notification.imageUrl
            if (finalImageUrl.isNotBlank() && !finalImageUrl.startsWith("http") && !finalImageUrl.startsWith("data:")) {
                try {
                    val compressedBytes = ImageCompressor.compressImage(null, finalImageUrl, 65)
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
                            finalImageUrl = uploaded
                        }
                    }
                } catch (_: Exception) {}
            }
            val id = UUID.randomUUID().toString()
            val newItem = notification.copy(id = id, imageUrl = finalImageUrl, createdAt = System.currentTimeMillis())
            firestore.collection("notifications").document(id).set(newItem).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirim gönderilemedi")
        }
    }

    actual suspend fun deleteNotification(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Duyuru silinemedi")
        }
    }
}
