package com.example.anadolugalericilersit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.repository.FavoriteRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository = FavoriteRepository()
) : ViewModel() {

    private val _favoritesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Empty())
    val favoritesState: StateFlow<Resource<List<Vehicle>>> = _favoritesState.asStateFlow()

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    fun loadFavorites(userId: String) {
        viewModelScope.launch {
            _favoritesState.value = Resource.Loading()
            val result = favoriteRepository.getFavoriteVehicles(userId)
            _favoritesState.value = result
            if (result is Resource.Success && result.data != null) {
                _favoriteIds.value = result.data.map { it.id }.toSet()
            }
        }
    }

    fun toggleFavorite(vehicleId: String, userId: String) {
        viewModelScope.launch {
            val isFavResult = favoriteRepository.toggleFavorite(vehicleId, userId)
            if (isFavResult is Resource.Success) {
                val currentSet = _favoriteIds.value.toMutableSet()
                if (isFavResult.data == true) {
                    currentSet.add(vehicleId)
                } else {
                    currentSet.remove(vehicleId)
                }
                _favoriteIds.value = currentSet
                loadFavorites(userId)
            }
        }
    }
}
