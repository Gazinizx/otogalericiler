package com.example.anadolugalericilersit.utils

actual object IntentUtils {
    actual fun openDialer(context: Any?, phone: String) {}
    actual fun openPhoneDialer(phone: String) {}
    actual fun openWhatsApp(context: Any?, phone: String, message: String) {}
    actual fun openMaps(context: Any?, latitude: Double, longitude: Double, label: String) {}
    actual fun openMap(latitude: Double, longitude: Double, label: String) {}
    actual fun shareVehicle(context: Any?, title: String, priceText: String, vehicleId: String) {}
    actual fun shareText(context: Any?, text: String) {}
}
