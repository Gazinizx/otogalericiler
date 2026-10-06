package com.example.anadolugalericilersit.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.anadolugalericilersit.AnadoluApp

actual object ToastUtils {
    actual fun showToast(context: Any?, message: String) {
        val ctx = (context as? Context) ?: try { AnadoluApp.instance } catch (_: Exception) { null }
        if (ctx != null) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
