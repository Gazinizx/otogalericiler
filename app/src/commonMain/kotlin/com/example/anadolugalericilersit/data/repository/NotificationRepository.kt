package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.utils.Resource

expect class NotificationRepository() {
    suspend fun getNotifications(userId: String): Resource<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
    suspend fun sendNotification(notification: NotificationItem): Resource<Unit>
}
