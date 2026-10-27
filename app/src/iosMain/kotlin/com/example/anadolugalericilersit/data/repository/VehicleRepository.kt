package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.FirestoreRestClient
import com.example.anadolugalericilersit.utils.FirestoreSync
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.generateUuid

actual class VehicleRepository actual constructor() {
    actual suspend fun getVehicles(
        filter: VehicleFilter,
        sort: VehicleSort,
        limit: Int
    ): Resource<List<Vehicle>> {
        return try {
            var list = FirestoreRestClient.fetchVehiclesFromCloud()
            if (filter.searchQuery.isNotBlank()) {
                val query = filter.searchQuery.trim()
                list = list.filter {
                    it.brand.contains(query, ignoreCase = true) ||
                        it.model.contains(query, ignoreCase = true) ||
                        it.dealerName.contains(query, ignoreCase = true) ||
                        it.dealerCity.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
                }
            }
            filter.brand?.takeIf { it.isNotBlank() }?.let { brand ->
                list = list.filter { it.brand.equals(brand, ignoreCase = true) }
            }
            filter.model?.takeIf { it.isNotBlank() }?.let { model ->
                list = list.filter { it.model.contains(model, ignoreCase = true) }
            }
            filter.minPrice?.let { min -> list = list.filter { it.price >= min } }
            filter.maxPrice?.let { max -> list = list.filter { it.price <= max } }
            filter.minYear?.let { min -> list = list.filter { it.year >= min } }
            filter.maxYear?.let { max -> list = list.filter { it.year <= max } }
            filter.minKm?.let { min -> list = list.filter { it.km >= min } }
            filter.maxKm?.let { max -> list = list.filter { it.km <= max } }
            filter.fuelType?.takeIf { it.isNotBlank() }?.let { fuel ->
                list = list.filter { it.fuelType.equals(fuel, ignoreCase = true) }
            }
            filter.transmission?.takeIf { it.isNotBlank() }?.let { transmission ->
                list = list.filter { it.transmission.equals(transmission, ignoreCase = true) }
            }
            filter.bodyType?.takeIf { it.isNotBlank() }?.let { body ->
                list = list.filter { it.bodyType.equals(body, ignoreCase = true) }
            }
            filter.city?.takeIf { it.isNotBlank() }?.let { city ->
                list = list.filter { it.dealerCity.contains(city, ignoreCase = true) }
            }
            filter.color?.takeIf { it.isNotBlank() }?.let { color ->
                list = list.filter { it.color.contains(color, ignoreCase = true) }
            }
            list = when (sort) {
                VehicleSort.NEWEST -> list.sortedByDescending { it.createdAt }
                VehicleSort.OLDEST -> list.sortedBy { it.createdAt }
                VehicleSort.PRICE_ASC -> list.sortedBy { it.price }
                VehicleSort.PRICE_DESC -> list.sortedByDescending { it.price }
                VehicleSort.KM_ASC -> list.sortedBy { it.km }
            }
            Resource.Success(list.take(limit.coerceAtLeast(0)))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İlanlar sunucudan alınamadı")
        }
    }

    actual suspend fun getVehicleById(vehicleId: String): Resource<Vehicle> {
        return try {
            val list = FirestoreRestClient.fetchVehiclesFromCloud()
            val v = list.find { it.id == vehicleId } ?: return Resource.Error("Araç bulunamadı")
            Resource.Success(v)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Araç bilgileri sunucudan alınamadı")
        }
    }

    actual suspend fun getVehiclesByDealer(dealerId: String, statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        return try {
            var list = FirestoreRestClient.fetchVehiclesFromCloud().filter { it.dealerId == dealerId }
            if (statusFilter != null) list = list.filter { it.status == statusFilter }
            Resource.Success(list)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Galerinin ilanları sunucudan alınamadı")
        }
    }

    actual suspend fun getVehiclesByStatusForAdmin(statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        return try {
            var list = FirestoreRestClient.fetchVehiclesFromCloud()
            if (statusFilter != null) list = list.filter { it.status == statusFilter }
            Resource.Success(list)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Yönetim ilanları sunucudan alınamadı")
        }
    }

    actual suspend fun saveVehicle(
        context: Any?,
        vehicle: Vehicle,
        newImageUris: List<Any>,
        videoUri: Any?,
        expertReportUri: Any?,
        onProgress: (Int, String) -> Unit
    ): Resource<Vehicle> {
        val id = vehicle.id.ifBlank { generateUuid() }
        val saved = vehicle.copy(id = id)

        return try {
            val fields = """
                {
                  "id": { "stringValue": ${quoteJson(saved.id)} },
                  "dealerId": { "stringValue": ${quoteJson(saved.dealerId)} },
                  "dealerName": { "stringValue": ${quoteJson(saved.dealerName)} },
                  "dealerPhone": { "stringValue": ${quoteJson(saved.dealerPhone)} },
                  "dealerCity": { "stringValue": ${quoteJson(saved.dealerCity)} },
                  "dealerDistrict": { "stringValue": ${quoteJson(saved.dealerDistrict)} },
                  "dealerLogoUrl": { "stringValue": ${quoteJson(saved.dealerLogoUrl)} },
                  "brand": { "stringValue": ${quoteJson(saved.brand)} },
                  "model": { "stringValue": ${quoteJson(saved.model)} },
                  "year": { "integerValue": "${saved.year}" },
                  "km": { "integerValue": "${saved.km}" },
                  "fuelType": { "stringValue": ${quoteJson(saved.fuelType)} },
                  "transmission": { "stringValue": ${quoteJson(saved.transmission)} },
                  "bodyType": { "stringValue": ${quoteJson(saved.bodyType)} },
                  "engineSize": { "stringValue": ${quoteJson(saved.engineSize)} },
                  "enginePower": { "stringValue": ${quoteJson(saved.enginePower)} },
                  "drivetrain": { "stringValue": ${quoteJson(saved.drivetrain)} },
                  "color": { "stringValue": ${quoteJson(saved.color)} },
                  "price": { "doubleValue": ${saved.price} },
                  "negotiable": { "booleanValue": ${saved.negotiable} },
                  "suitableForCredit": { "booleanValue": ${saved.suitableForCredit} },
                  "description": { "stringValue": ${quoteJson(saved.description)} },
                  "licensePlate": { "stringValue": ${quoteJson(saved.licensePlate)} },
                  "features": { "arrayValue": { "values": ${stringArrayJson(saved.features)} } },
                  "imageUrls": { "arrayValue": { "values": ${stringArrayJson(saved.imageUrls)} } },
                  "mainImageUrl": { "stringValue": ${quoteJson(saved.mainImageUrl)} },
                  "videoUrl": { "stringValue": ${quoteJson(saved.videoUrl)} },
                  "status": { "stringValue": ${quoteJson(saved.status.name)} },
                  "viewCount": { "integerValue": "${saved.viewCount}" },
                  "favoriteCount": { "integerValue": "${saved.favoriteCount}" },
                  "rejectionReason": { "stringValue": ${quoteJson(saved.rejectionReason)} },
                  "hasDamga": { "booleanValue": ${saved.hasDamga} },
                  "expertInspectionCost": { "doubleValue": ${saved.expertInspectionCost} },
                  "expertReportStatus": { "stringValue": ${quoteJson(saved.expertReportStatus)} },
                  "expertReportDetails": { "stringValue": ${quoteJson(saved.expertReportDetails)} },
                  "expertReportImageUrl": { "stringValue": ${quoteJson(saved.expertReportImageUrl)} },
                  "bodyPartsCondition": { "mapValue": { "fields": ${stringMapJson(saved.bodyPartsCondition)} } },
                  "damgaStatus": { "stringValue": ${quoteJson(saved.damgaStatus.name)} },
                  "createdAt": { "integerValue": "${saved.createdAt}" },
                  "updatedAt": { "integerValue": "${saved.updatedAt}" }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("vehicles", saved.id, fields)
            LocalStore.vehicles[id] = saved
            Resource.Success(saved)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İlan buluta kaydedilemedi")
        }
    }

    actual suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        rejectionReason: String
    ): Resource<Unit> {
        val v = LocalStore.vehicles[vehicleId] ?: Vehicle(id = vehicleId)
        val updated = v.copy(status = status, rejectionReason = rejectionReason)
        return try {
            val fields = """
                {
                  "status": { "stringValue": ${quoteJson(updated.status.name)} },
                  "rejectionReason": { "stringValue": ${quoteJson(updated.rejectionReason)} }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("vehicles", vehicleId, fields)
            LocalStore.vehicles[vehicleId] = updated
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İlan durumu buluta kaydedilemedi")
        }
    }

    actual suspend fun updateVehicleDamgaStatus(
        vehicleId: String,
        damgaStatus: String,
        approved: Boolean
    ): Resource<Unit> {
        val v = LocalStore.vehicles[vehicleId] ?: Vehicle(id = vehicleId)
        val updated = v.copy(hasDamga = approved)
        return try {
            val fields = """
                {
                  "hasDamga": { "booleanValue": $approved }
                }
            """.trimIndent()
            FirestoreSync.patchDocument("vehicles", vehicleId, fields)
            LocalStore.vehicles[vehicleId] = updated
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Damga durumu buluta kaydedilemedi")
        }
    }

    actual suspend fun incrementViewCount(vehicleId: String, currentUserId: String?): Resource<Unit> {
        return Resource.Success(Unit)
    }

    actual suspend fun deleteVehicle(vehicleId: String): Resource<Unit> {
        return try {
            FirestoreSync.deleteDocument("vehicles", vehicleId)
            LocalStore.vehicles.remove(vehicleId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "İlan buluttan silinemedi")
        }
    }

    actual suspend fun deleteAllVehicles(): Resource<Unit> {
        LocalStore.vehicles.clear()
        return Resource.Success(Unit)
    }
    private fun quoteJson(value: String): String = buildString {
        append('"')
        value.forEach { ch ->
            when (ch) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (ch.code < 0x20) {
                    append("\\u")
                    append(ch.code.toString(16).padStart(4, '0'))
                } else append(ch)
            }
        }
        append('"')
    }

    private fun stringArrayJson(values: List<String>): String =
        values.joinToString(prefix = "[", postfix = "]") { """{"stringValue": ${quoteJson(it)}}""" }

    private fun stringMapJson(values: Map<String, String>): String =
        values.entries.joinToString(prefix = "{", postfix = "}") {
            "${quoteJson(it.key)}: { \"stringValue\": ${quoteJson(it.value)} }"
        }

}
