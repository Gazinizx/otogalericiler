package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.utils.Resource

actual class FavoriteRepository actual constructor() {
    actual suspend fun getFavoriteVehicleIds(userId: String): Resource<List<String>> {
        val ids = LocalStore.favorites[userId]?.toList() ?: emptyList()
        return Resource.Success(ids)
    }

    actual suspend fun addFavorite(userId: String, vehicleId: String): Resource<Unit> {
        val set = LocalStore.favorites.getOrPut(userId) { mutableSetOf() }
        set.add(vehicleId)
        return Resource.Success(Unit)
    }

    actual suspend fun removeFavorite(userId: String, vehicleId: String): Resource<Unit> {
        LocalStore.favorites[userId]?.remove(vehicleId)
        return Resource.Success(Unit)
    }

    actual suspend fun toggleFavorite(vehicleId: String, userId: String): Resource<Boolean> {
        val set = LocalStore.favorites.getOrPut(userId) { mutableSetOf() }
        val isFavNow: Boolean
        if (set.contains(vehicleId)) {
            set.remove(vehicleId)
            isFavNow = false
        } else {
            set.add(vehicleId)
            isFavNow = true
        }
        return Resource.Success(isFavNow)
    }

    actual suspend fun getFavoriteVehicles(userId: String): Resource<List<Vehicle>> {
        val ids = LocalStore.favorites[userId] ?: emptySet()
        val vehicles = ids.mapNotNull { LocalStore.vehicles[it] }
        return Resource.Success(vehicles)
    }
}
