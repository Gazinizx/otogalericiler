package com.example.anadolugalericilersit.utils

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.*
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.*
import platform.darwin.dispatch_semaphore_create
import platform.darwin.dispatch_semaphore_signal
import platform.darwin.dispatch_semaphore_wait
import platform.darwin.dispatch_time

@OptIn(BetaInteropApi::class)
object FirestoreRestClient {
    private const val PROJECT_ID = "galeri-6fa72"
    private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    fun syncDealers() {
        try {
            val url = NSURL.URLWithString("$BASE_URL/dealers") ?: return
            val sem = dispatch_semaphore_create(0L)
            val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data, _, _ ->
                if (data != null) {
                    val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
                    if (jsonStr != null) {
                        parseAndStoreDealers(jsonStr)
                    }
                }
                dispatch_semaphore_signal(sem)
            }
            task.resume()
            dispatch_semaphore_wait(sem, dispatch_time(0.toULong(), 3_000_000_000L))
        } catch (_: Exception) {}
    }

    fun syncVehicles() {
        try {
            val url = NSURL.URLWithString("$BASE_URL/vehicles") ?: return
            val sem = dispatch_semaphore_create(0L)
            val task = NSURLSession.sharedSession.dataTaskWithURL(url) { data, _, _ ->
                if (data != null) {
                    val jsonStr = NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
                    if (jsonStr != null) {
                        parseAndStoreVehicles(jsonStr)
                    }
                }
                dispatch_semaphore_signal(sem)
            }
            task.resume()
            dispatch_semaphore_wait(sem, dispatch_time(0.toULong(), 3_000_000_000L))
        } catch (_: Exception) {}
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
                val statusStr = extractStringField(chunk, "status").ifBlank { "PUBLISHED" }
                val status = try { VehicleStatus.valueOf(statusStr) } catch (_: Exception) { VehicleStatus.PUBLISHED }

                val vehicle = Vehicle(
                    id = idMatch,
                    brand = brand,
                    model = model,
                    dealerId = dealerId,
                    dealerName = dealerName,
                    dealerCity = dealerCity,
                    status = status
                )
                LocalStore.vehicles[idMatch] = vehicle
            }
        } catch (_: Exception) {}
    }

    private fun extractStringField(chunk: String, fieldName: String): String {
        try {
            val fieldSection = chunk.substringAfter("\"$fieldName\" : {").substringBefore("}")
            if (fieldSection.contains("\"stringValue\" :")) {
                return fieldSection.substringAfter("\"stringValue\" : \"").substringBefore("\"")
            }
        } catch (_: Exception) {}
        return ""
    }
}
