package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.utils.Resource

expect class FavoriteRepository() {
    suspend fun getFavoriteVehicleIds(userId: String): Resource<List<String>>
    suspend fun addFavorite(userId: String, vehicleId: String): Resource<Unit>
    suspend fun removeFavorite(userId: String, vehicleId: String): Resource<Unit>
    suspend fun toggleFavorite(vehicleId: String, userId: String): Resource<Boolean>
    suspend fun getFavoriteVehicles(userId: String): Resource<List<Vehicle>>
}
