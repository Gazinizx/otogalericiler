package com.example.anadolugalericilersit.data.model

import com.example.anadolugalericilersit.utils.currentTimeMillis

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO",
    val targetId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = currentTimeMillis()
)
