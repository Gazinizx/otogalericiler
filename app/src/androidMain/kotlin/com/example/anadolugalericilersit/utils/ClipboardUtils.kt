package com.example.anadolugalericilersit.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual object ClipboardUtils {
    actual fun copyToClipboard(context: Any?, label: String, text: String) {
        val ctx = context as? Context
        if (ctx != null) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
        }
    }
}
