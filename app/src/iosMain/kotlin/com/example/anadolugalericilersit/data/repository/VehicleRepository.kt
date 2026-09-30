package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.generateUuid

actual class VehicleRepository actual constructor() {
    actual suspend fun getVehicles(
        filter: VehicleFilter,
        sort: VehicleSort,
        limit: Int
    ): Resource<List<Vehicle>> {
        var list = LocalStore.vehicles.values.toList()
        if (filter.brand != null) list = list.filter { it.brand.equals(filter.brand, true) }
        return Resource.Success(list.take(limit))
    }

    actual suspend fun getVehicleById(vehicleId: String): Resource<Vehicle> {
        val v = LocalStore.vehicles[vehicleId] ?: return Resource.Error("Araç bulunamadı")
        return Resource.Success(v)
    }

    actual suspend fun getVehiclesByDealer(dealerId: String, statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        var list = LocalStore.vehicles.values.filter { it.dealerId == dealerId }
        if (statusFilter != null) list = list.filter { it.status == statusFilter }
        return Resource.Success(list)
    }

    actual suspend fun getVehiclesByStatusForAdmin(statusFilter: VehicleStatus?): Resource<List<Vehicle>> {
        var list = LocalStore.vehicles.values.toList()
        if (statusFilter != null) list = list.filter { it.status == statusFilter }
        return Resource.Success(list)
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
        LocalStore.vehicles[id] = saved
        return Resource.Success(saved)
    }

    actual suspend fun updateVehicleStatus(
        vehicleId: String,
        status: VehicleStatus,
        rejectionReason: String
    ): Resource<Unit> {
        val v = LocalStore.vehicles[vehicleId] ?: return Resource.Error("Araç bulunamadı")
        LocalStore.vehicles[vehicleId] = v.copy(status = status, rejectionReason = rejectionReason)
        return Resource.Success(Unit)
    }

    actual suspend fun updateVehicleDamgaStatus(
        vehicleId: String,
        damgaStatus: String,
        approved: Boolean
    ): Resource<Unit> {
        val v = LocalStore.vehicles[vehicleId] ?: return Resource.Error("Araç bulunamadı")
        LocalStore.vehicles[vehicleId] = v.copy(hasDamga = approved)
        return Resource.Success(Unit)
    }

    actual suspend fun incrementViewCount(vehicleId: String, currentUserId: String?): Resource<Unit> {
        val v = LocalStore.vehicles[vehicleId] ?: return Resource.Error("Araç bulunamadı")
        LocalStore.vehicles[vehicleId] = v.copy(viewCount = v.viewCount + 1)
        return Resource.Success(Unit)
    }

    actual suspend fun deleteVehicle(vehicleId: String): Resource<Unit> {
        LocalStore.vehicles.remove(vehicleId)
        return Resource.Success(Unit)
    }
}
