package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getNotifications(userId: String): Resource<List<NotificationItem>> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get().await()
            val list = snapshot.toObjects(NotificationItem::class.java).sortedByDescending { it.createdAt }
            Resource.Success(list)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirimler yüklenemedi")
        }
    }

    suspend fun markAsRead(notificationId: String): Resource<Unit> {
        return try {
            firestore.collection("notifications").document(notificationId).update("isRead", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirim okundu olarak işaretlenemedi")
        }
    }

    suspend fun sendNotification(notification: NotificationItem): Resource<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newItem = notification.copy(id = id, createdAt = System.currentTimeMillis())
            firestore.collection("notifications").document(id).set(newItem).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Bildirim gönderilemedi")
        }
    }
}
