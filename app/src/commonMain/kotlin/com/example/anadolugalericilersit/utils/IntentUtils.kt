package com.example.anadolugalericilersit.utils

expect object IntentUtils {
    fun openDialer(context: Any? = null, phone: String)
    fun openPhoneDialer(phone: String)
    fun openWhatsApp(context: Any? = null, phone: String, message: String = "")
    fun openMaps(context: Any? = null, latitude: Double, longitude: Double, label: String)
    fun openMap(latitude: Double, longitude: Double, label: String)
    fun shareVehicle(context: Any? = null, title: String, priceText: String, vehicleId: String)
    fun shareText(context: Any? = null, text: String)
}
