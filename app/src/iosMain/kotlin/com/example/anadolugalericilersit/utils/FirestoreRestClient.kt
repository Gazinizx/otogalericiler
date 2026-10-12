package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.*
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume

@OptIn(BetaInteropApi::class)
object FirestoreRestClient {
    private const val PROJECT_ID = "galeri-6fa72"
    private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    suspend fun syncDealersSuspend() = suspendCancellableCoroutine { continuation ->
        try {
            val url = NSURL.URLWithString("$BASE_URL/dealers")
            if (url == null) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
            val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data, _, _ ->
                if (data != null) {
                    val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
                    if (jsonStr != null) {
                        parseAndStoreDealers(jsonStr)
                    }
                }
                continuation.resume(Unit)
            }
            task.resume()
        } catch (_: Exception) {
            continuation.resume(Unit)
        }
    }

    suspend fun syncVehiclesSuspend() = suspendCancellableCoroutine { continuation ->
        try {
            val url = NSURL.URLWithString("$BASE_URL/vehicles")
            if (url == null) {
                continuation.resume(Unit)
                return@suspendCancellableCoroutine
            }
            val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data, _, _ ->
                if (data != null) {
                    val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
                    if (jsonStr != null) {
                        parseAndStoreVehicles(jsonStr)
                    }
                }
                continuation.resume(Unit)
            }
            task.resume()
        } catch (_: Exception) {
            continuation.resume(Unit)
        }
    }

    private fun parseAndStoreDealers(json: String) {
        try {
            val docChunks = json.split("\"name\" : \"")
            for (chunk in docChunks.drop(1)) {
                val idMatch = chunk.substringAfter("dealers/").substringBefore("\"")
                if (idMatch.isBlank()) continue
                
                val galleryName = extractStringField(chunk, "galleryName")
                val ownerUid = extractStringField(chunk, "ownerUid").ifBlank { idMatch }
                val phone = extractStringField(chunk, "phone")
                val email = extractStringField(chunk, "email")
                val city = extractStringField(chunk, "city")
                val district = extractStringField(chunk, "district")
                val address = extractStringField(chunk, "address")
                val taxNumber = extractStringField(chunk, "taxNumber")
                val iban = extractStringField(chunk, "iban")
                val statusStr = extractStringField(chunk, "accountStatus").ifBlank { "PENDING" }
                val status = try { DealerStatus.valueOf(statusStr) } catch (_: Exception) { DealerStatus.PENDING }

                val dealer = Dealer(
                    id = idMatch,
                    ownerUid = ownerUid,
                    galleryName = galleryName,
                    phone = phone,
                    email = email,
                    city = city,
                    district = district,
                    address = address,
                    taxNumber = taxNumber,
                    iban = iban,
                    accountStatus = status
                )
                LocalStore.dealers[idMatch] = dealer
            }
        } catch (_: Exception) {}
    }

    private fun parseAndStoreVehicles(json: String) {
        try {
            val docChunks = json.split("\"name\" : \"")
            for (chunk in docChunks.drop(1)) {
                val idMatch = chunk.substringAfter("vehicles/").substringBefore("\"")
                if (idMatch.isBlank()) continue

                val brand = extractStringField(chunk, "brand")
                val model = extractStringField(chunk, "model")
                val dealerId = extractStringField(chunk, "dealerId")
                val dealerName = extractStringField(chunk, "galleryName")
                val dealerCity = extractStringField(chunk, "city")
                val priceStr = extractStringField(chunk, "price")
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val statusStr = extractStringField(chunk, "status").ifBlank { "PUBLISHED" }
                val status = try { VehicleStatus.valueOf(statusStr) } catch (_: Exception) { VehicleStatus.PUBLISHED }

                val vehicle = Vehicle(
                    id = idMatch,
                    brand = brand,
                    model = model,
                    dealerId = dealerId,
                    dealerName = dealerName,
                    dealerCity = dealerCity,
                    price = price,
                    status = status
                )
                LocalStore.vehicles[idMatch] = vehicle
            }
        } catch (_: Exception) {}
    }

    private fun extractStringField(chunk: String, fieldName: String): String {
        try {
            val keyPattern = "\"$fieldName\""
            val index = chunk.indexOf(keyPattern)
            if (index == -1) return ""
            val sub = chunk.substring(index)
            if (sub.contains("\"stringValue\"")) {
                return sub.substringAfter("\"stringValue\"").substringAfter("\"").substringBefore("\"")
            }
            if (sub.contains("\"integerValue\"")) {
                return sub.substringAfter("\"integerValue\"").substringAfter("\"").substringBefore("\"")
            }
            if (sub.contains("\"doubleValue\"")) {
                return sub.substringAfter("\"doubleValue\"").substringAfter(":").substringBefore(",").trim().replace("\"", "")
            }
        } catch (_: Exception) {}
        return ""
    }
}
