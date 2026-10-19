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

    suspend fun syncDealersSuspend() {
        fetchDealersFromCloud().forEach { LocalStore.dealers[it.id] = it }
    }

    suspend fun syncVehiclesSuspend() {
        fetchVehiclesFromCloud().forEach { LocalStore.vehicles[it.id] = it }
    }

    suspend fun fetchDealersFromCloud(): List<Dealer> {
        val json = fetchCollection("dealers") ?: return emptyList()
        return documentChunks(json, "dealers").mapNotNull { parseDealer(it) }
    }

    suspend fun fetchVehiclesFromCloud(): List<Vehicle> {
        val json = fetchCollection("vehicles") ?: return emptyList()
        return documentChunks(json, "vehicles").mapNotNull { parseVehicle(it) }
    }

    private suspend fun fetchCollection(collection: String): String? =
        suspendCancellableCoroutine { continuation ->
            val url = NSURL.URLWithString("$BASE_URL/$collection?pageSize=1000")
            if (url == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val request = NSMutableURLRequest.requestWithURL(url)
            request.HTTPMethod = "GET"
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
                if (continuation.isCancelled) return@dataTaskWithRequest
                val status = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                if (error != null || status !in 200..299 || data == null) {
                    continuation.resume(null)
                    return@dataTaskWithRequest
                }
                continuation.resume(NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String)
            }
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }

    private fun documentChunks(json: String, collection: String): List<String> {
        val documentPattern = Regex("\"name\"\\s*:\\s*\"[^\"]*/" + Regex.escape(collection) + "/[^\"]+\"")
        return json.split(Regex("(?=\\{\\s*\"name\"\\s*:)"))
            .filter { documentPattern.containsMatchIn(it) }
    }

    private fun documentId(chunk: String, collection: String): String? {
        val pattern = Regex("\"name\"\\s*:\\s*\"[^\"]*/" + Regex.escape(collection) + "/([^\"/]+)\"")
        return pattern.find(chunk)?.groupValues?.get(1)
    }

    private fun parseDealer(chunk: String): Dealer? {
        val id = documentId(chunk, "dealers") ?: return null
        return Dealer(
            id = id,
            ownerUid = stringField(chunk, "ownerUid").ifBlank { id },
            galleryName = stringField(chunk, "galleryName"),
            authorizedName = stringField(chunk, "authorizedName"),
            phone = stringField(chunk, "phone"),
            email = stringField(chunk, "email"),
            city = stringField(chunk, "city"),
            district = stringField(chunk, "district"),
            address = stringField(chunk, "address"),
            taxNumber = stringField(chunk, "taxNumber"),
            iban = stringField(chunk, "iban"),
            ibanOwnerName = stringField(chunk, "ibanOwnerName"),
            description = stringField(chunk, "description"),
            logoUrl = stringField(chunk, "logoUrl"),
            profilePhotoUrl = stringField(chunk, "profilePhotoUrl"),
            shopPhotoUrl = stringField(chunk, "shopPhotoUrl"),
            latitude = doubleField(chunk, "latitude", 39.9334),
            longitude = doubleField(chunk, "longitude", 32.8597),
            workingHours = stringField(chunk, "workingHours").ifBlank { "09:00 - 18:00" },
            accountStatus = enumValue(stringField(chunk, "accountStatus"), DealerStatus.PENDING),
            rejectionReason = stringField(chunk, "rejectionReason"),
            createdAt = longField(chunk, "createdAt"),
            updatedAt = longField(chunk, "updatedAt"),
            totalListings = intField(chunk, "totalListings"),
            activeListings = intField(chunk, "activeListings"),
            soldListings = intField(chunk, "soldListings"),
            pendingListings = intField(chunk, "pendingListings"),
            totalDebt = doubleField(chunk, "totalDebt"),
            damgaCount = intField(chunk, "damgaCount"),
            rewardCycleCount = intField(chunk, "rewardCycleCount")
        )
    }

    private fun parseVehicle(chunk: String): Vehicle? {
        val id = documentId(chunk, "vehicles") ?: return null
        return Vehicle(
            id = id,
            dealerId = stringField(chunk, "dealerId"),
            dealerName = stringField(chunk, "dealerName").ifBlank { stringField(chunk, "galleryName") },
            dealerPhone = stringField(chunk, "dealerPhone"),
            dealerCity = stringField(chunk, "dealerCity").ifBlank { stringField(chunk, "city") },
            dealerDistrict = stringField(chunk, "dealerDistrict"),
            dealerLogoUrl = stringField(chunk, "dealerLogoUrl"),
            brand = stringField(chunk, "brand"),
            model = stringField(chunk, "model"),
            year = intField(chunk, "year", 2024),
            km = intField(chunk, "km"),
            fuelType = stringField(chunk, "fuelType"),
            transmission = stringField(chunk, "transmission"),
            bodyType = stringField(chunk, "bodyType"),
            engineSize = stringField(chunk, "engineSize"),
            enginePower = stringField(chunk, "enginePower"),
            drivetrain = stringField(chunk, "drivetrain"),
            color = stringField(chunk, "color"),
            price = doubleField(chunk, "price"),
            negotiable = booleanField(chunk, "negotiable"),
            suitableForCredit = booleanField(chunk, "suitableForCredit"),
            description = stringField(chunk, "description"),
            licensePlate = stringField(chunk, "licensePlate"),
            features = stringArrayField(chunk, "features"),
            imageUrls = stringArrayField(chunk, "imageUrls"),
            mainImageUrl = stringField(chunk, "mainImageUrl"),
            videoUrl = stringField(chunk, "videoUrl"),
            status = enumValue(stringField(chunk, "status"), VehicleStatus.PUBLISHED),
            viewCount = longField(chunk, "viewCount"),
            favoriteCount = longField(chunk, "favoriteCount"),
            rejectionReason = stringField(chunk, "rejectionReason"),
            hasDamga = booleanField(chunk, "hasDamga"),
            expertInspectionCost = doubleField(chunk, "expertInspectionCost"),
            expertReportStatus = stringField(chunk, "expertReportStatus"),
            expertReportDetails = stringField(chunk, "expertReportDetails"),
            expertReportImageUrl = stringField(chunk, "expertReportImageUrl"),
            bodyPartsCondition = stringMapField(chunk, "bodyPartsCondition"),
            damgaStatus = enumValue(stringField(chunk, "damgaStatus"), DamgaStatus.NONE),
            createdAt = longField(chunk, "createdAt"),
            updatedAt = longField(chunk, "updatedAt")
        )
    }

    private fun stringField(chunk: String, field: String): String {
        val pattern = "\"" + Regex.escape(field) + "\"\\s*:\\s*\\{\\s*\"stringValue\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        val value = Regex(pattern).find(chunk)?.groupValues?.get(1) ?: return ""
        return unescapeJson(value)
    }

    private fun longField(chunk: String, field: String, default: Long = 0L): Long {
        val pattern = "\"" + Regex.escape(field) + "\"\\s*:\\s*\\{\\s*\"(?:integerValue|doubleValue)\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?"
        return Regex(pattern).find(chunk)?.groupValues?.get(1)?.toDoubleOrNull()?.toLong() ?: default
    }

    private fun intField(chunk: String, field: String, default: Int = 0): Int =
        longField(chunk, field, default.toLong()).toInt()

    private fun doubleField(chunk: String, field: String, default: Double = 0.0): Double {
        val pattern = "\"" + Regex.escape(field) + "\"\\s*:\\s*\\{\\s*\"(?:integerValue|doubleValue)\"\\s*:\\s*\"?(-?\\d+(?:\\.\\d+)?)\"?"
        return Regex(pattern).find(chunk)?.groupValues?.get(1)?.toDoubleOrNull() ?: default
    }

    private fun booleanField(chunk: String, field: String): Boolean {
        val pattern = "\"" + Regex.escape(field) + "\"\\s*:\\s*\\{\\s*\"booleanValue\"\\s*:\\s*(true|false)"
        return Regex(pattern).find(chunk)?.groupValues?.get(1)?.toBoolean() ?: false
    }

    private fun stringArrayField(chunk: String, field: String): List<String> {
        val section = chunk.substringAfter("\"$field\"", "").substringAfter("\"values\"", "").substringBefore("]")
        val pattern = Regex("\"stringValue\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        return pattern.findAll(section).map { unescapeJson(it.groupValues[1]) }.toList()
    }

    private fun stringMapField(chunk: String, field: String): Map<String, String> {
        val section = chunk.substringAfter("\"$field\"", "").substringAfter("\"mapValue\"", "").substringBefore("}")
        val pattern = Regex("\"([^\"]+)\"\\s*:\\s*\\{\\s*\"stringValue\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
        return pattern.findAll(section).associate { it.groupValues[1] to unescapeJson(it.groupValues[2]) }
    }

    private inline fun <reified T : Enum<T>> enumValue(value: String, default: T): T =
        try { enumValueOf<T>(value) } catch (_: Exception) { default }

    private fun unescapeJson(value: String): String =
        value.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
            .replace("\\r", "\r").replace("\\t", "\t")
}
