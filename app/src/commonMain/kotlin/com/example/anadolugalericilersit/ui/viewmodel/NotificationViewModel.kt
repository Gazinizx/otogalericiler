package com.example.anadolugalericilersit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.data.repository.NotificationRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<Resource<List<NotificationItem>>>(Resource.Empty())
    val notificationsState: StateFlow<Resource<List<NotificationItem>>> = _notificationsState.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            _notificationsState.value = Resource.Loading()
            val result = notificationRepository.getNotifications(userId)
            _notificationsState.value = result
        }
    }

    fun markAsRead(notificationId: String, userId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadNotifications(userId)
        }
    }
}
