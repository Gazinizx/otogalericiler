package com.example.anadolugalericilersit.utils

expect object SessionManager {
    fun saveSession(uid: String, context: Any? = null)
    fun getSavedUid(context: Any? = null): String?
    fun clearSession(context: Any? = null)
}
