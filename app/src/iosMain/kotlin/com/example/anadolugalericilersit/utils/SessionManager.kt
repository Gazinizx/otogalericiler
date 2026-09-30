package com.example.anadolugalericilersit.utils

actual object SessionManager {
    actual fun saveSession(uid: String, context: Any?) {}
    actual fun getSavedUid(context: Any?): String? = null
    actual fun clearSession(context: Any?) {}
}
