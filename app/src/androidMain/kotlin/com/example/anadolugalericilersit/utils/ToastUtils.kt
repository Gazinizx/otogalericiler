package com.example.anadolugalericilersit.utils

import android.content.Context
import android.widget.Toast

actual object ToastUtils {
    actual fun showToast(context: Any?, message: String) {
        val ctx = context as? Context
        if (ctx != null) {
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show()
        }
    }
}
