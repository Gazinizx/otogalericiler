package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.utils.Resource

actual class NotificationRepository {
    actual suspend fun getNotifications(userId: String): Resource<List<NotificationItem>> {
        val list = LocalStore.notifications[userId]?.toList() ?: emptyList()
        return Resource.Success(list)
    }

    actual suspend fun markAsRead(notificationId: String): Resource<Unit> {
        LocalStore.notifications.values.forEach { list ->
            val idx = list.indexOfFirst { it.id == notificationId }
            if (idx != -1) {
                list[idx] = list[idx].copy(isRead = true)
            }
        }
        return Resource.Success(Unit)
    }

    actual suspend fun sendNotification(notification: NotificationItem): Resource<Unit> {
        val list = LocalStore.notifications.getOrPut(notification.userId) { mutableListOf() }
        list.add(0, notification)
        return Resource.Success(Unit)
    }
}
