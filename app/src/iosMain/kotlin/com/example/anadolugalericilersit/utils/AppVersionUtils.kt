package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.model.AppVersionConfig

actual object AppVersionUtils {
    actual fun getCurrentVersionCode(context: Any?): Int = 1
    actual fun getCurrentVersionName(context: Any?): String = "1.0"
    actual suspend fun checkAppVersion(): AppVersionConfig? = null
    actual fun openUpdateUrl(context: Any?, url: String) {}
}
