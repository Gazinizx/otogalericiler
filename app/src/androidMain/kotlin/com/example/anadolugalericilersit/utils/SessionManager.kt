package com.example.anadolugalericilersit.utils

import android.content.Context
import android.util.Base64
import com.example.anadolugalericilersit.AnadoluApp

actual object SessionManager {
    private const val PREFS_NAME = "anadolu_galericiler_auth_prefs_secure"
    private const val KEY_UID = "logged_in_uid_enc"
    private const val SECRET_KEY = "AnadoluGaleriSecureSessionKey"

    actual fun saveSession(uid: String, context: Any?) {
        val ctx = (context as? Context) ?: AnadoluApp.instance
        val encryptedUid = encrypt(uid)
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_UID, encryptedUid)
            .apply()
    }

    actual fun getSavedUid(context: Any?): String? {
        return try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            val enc = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_UID, null) ?: return null
            decrypt(enc)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clearSession(context: Any?) {
        try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_UID)
                .apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun encrypt(input: String): String {
        return try {
            val xorBytes = input.toByteArray(Charsets.UTF_8).mapIndexed { i, byte ->
                (byte.toInt() xor SECRET_KEY[i % SECRET_KEY.length].code).toByte()
            }.toByteArray()
            Base64.encodeToString(xorBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            input
        }
    }

    private fun decrypt(input: String): String {
        return try {
            val decoded = Base64.decode(input, Base64.NO_WRAP)
            val xorBytes = decoded.mapIndexed { i, byte ->
                (byte.toInt() xor SECRET_KEY[i % SECRET_KEY.length].code).toByte()
            }.toByteArray()
            String(xorBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            input
        }
    }
}
