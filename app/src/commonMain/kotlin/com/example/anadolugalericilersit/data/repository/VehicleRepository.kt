package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.utils.Resource

expect class VehicleRepository() {
    suspend fun getVehicles(filter: VehicleFilter = VehicleFilter(), sort: VehicleSort = VehicleSort.NEWEST, limit: Int = 50): Resource<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): Resource<Vehicle>
    suspend fun getVehiclesByDealer(dealerId: String, statusFilter: VehicleStatus? = null): Resource<List<Vehicle>>
    suspend fun getVehiclesByStatusForAdmin(statusFilter: VehicleStatus? = null): Resource<List<Vehicle>>
    suspend fun saveVehicle(
        context: Any? = null,
        vehicle: Vehicle,
        newImageUris: List<Any> = emptyList(),
        videoUri: Any? = null,
        expertReportUri: Any? = null,
        onProgress: (Int, String) -> Unit = { _, _ -> }
    ): Resource<Vehicle>
    suspend fun updateVehicleStatus(vehicleId: String, status: VehicleStatus, rejectionReason: String = ""): Resource<Unit>
    suspend fun updateVehicleDamgaStatus(vehicleId: String, damgaStatus: String, approved: Boolean): Resource<Unit>
    suspend fun incrementViewCount(vehicleId: String, currentUserId: String? = null): Resource<Unit>
    suspend fun deleteVehicle(vehicleId: String): Resource<Unit>
}
