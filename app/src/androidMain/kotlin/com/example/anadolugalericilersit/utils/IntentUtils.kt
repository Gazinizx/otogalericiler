package com.example.anadolugalericilersit.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.anadolugalericilersit.AnadoluApp

actual object IntentUtils {
    actual fun openDialer(context: Any?, phone: String) {
        openPhoneDialer(phone)
    }

    actual fun openPhoneDialer(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AnadoluApp.instance.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun openWhatsApp(context: Any?, phone: String, message: String) {
        try {
            var formatted = phone.replace("[^0-9]".toRegex(), "")
            if (formatted.startsWith("0")) {
                formatted = "9" + formatted
            } else if (!formatted.startsWith("90")) {
                formatted = "90" + formatted
            }
            val encodedMsg = Uri.encode(message)
            val url = "https://api.whatsapp.com/send?phone=$formatted" + if (message.isNotBlank()) "&text=$encodedMsg" else ""
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AnadoluApp.instance.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun openMaps(context: Any?, latitude: Double, longitude: Double, label: String) {
        openMap(latitude, longitude, label)
    }

    actual fun openMap(latitude: Double, longitude: Double, label: String) {
        try {
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            AnadoluApp.instance.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun shareVehicle(context: Any?, title: String, priceText: String, vehicleId: String) {
        val text = "Anadolu Galericiler Sitesi'nde harika bir araç buldum!\n\n$title - $priceText TL\nİlan No: $vehicleId\n\nDetaylar için uygulamaya göz atın."
        shareText(context, text)
    }

    actual fun shareText(context: Any?, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Paylaş").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            AnadoluApp.instance.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
