package com.example.anadolugalericilersit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.example.anadolugalericilersit.AnadoluApp
import com.example.anadolugalericilersit.data.model.AppVersionConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

actual object AppVersionUtils {

    actual fun getCurrentVersionCode(context: Any?): Int {
        return try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    actual fun getCurrentVersionName(context: Any?): String {
        return try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    actual suspend fun checkAppVersion(): AppVersionConfig? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("app_config")
                .document("version")
                .get()
                .await()

            if (doc.exists()) {
                doc.toObject(AppVersionConfig::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    actual fun openUpdateUrl(context: Any?, url: String) {
        if (url.isBlank()) return
        try {
            val ctx = (context as? Context) ?: AnadoluApp.instance
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            // fallback
        }
    }
}
