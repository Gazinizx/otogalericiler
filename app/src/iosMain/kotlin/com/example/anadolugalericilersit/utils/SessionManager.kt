package com.example.anadolugalericilersit.utils

import platform.Foundation.NSUserDefaults

actual object SessionManager {
    private const val UID_KEY = "anadolu_galericiler_saved_uid"

    actual fun saveSession(uid: String, context: Any?) {
        NSUserDefaults.standardUserDefaults.setObject(uid, forKey = UID_KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    actual fun getSavedUid(context: Any?): String? =
        NSUserDefaults.standardUserDefaults.stringForKey(UID_KEY)

    actual fun clearSession(context: Any?) {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(UID_KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
