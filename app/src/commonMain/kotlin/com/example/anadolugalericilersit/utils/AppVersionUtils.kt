package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.model.AppVersionConfig

expect object AppVersionUtils {
    fun getCurrentVersionCode(context: Any? = null): Int
    fun getCurrentVersionName(context: Any? = null): String
    suspend fun checkAppVersion(): AppVersionConfig?
    fun openUpdateUrl(context: Any? = null, url: String)
}
