package com.example.anadolugalericilersit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.data.repository.DealerRepository
import com.example.anadolugalericilersit.data.repository.VehicleRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DealerViewModel(
    private val dealerRepository: DealerRepository = DealerRepository(),
    private val vehicleRepository: VehicleRepository = VehicleRepository()
) : ViewModel() {

    private val _dealerDetailState = MutableStateFlow<Resource<Dealer>>(Resource.Empty())
    val dealerDetailState: StateFlow<Resource<Dealer>> = _dealerDetailState.asStateFlow()

    private val _dealerVehiclesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Empty())
    val dealerVehiclesState: StateFlow<Resource<List<Vehicle>>> = _dealerVehiclesState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val updateProfileState: StateFlow<Resource<Unit>> = _updateProfileState.asStateFlow()

    fun loadDealerDetail(dealerId: String) {
        viewModelScope.launch {
            _dealerDetailState.value = Resource.Loading()
            _dealerVehiclesState.value = Resource.Loading()

            val dealerResult = dealerRepository.getDealerById(dealerId)
            _dealerDetailState.value = dealerResult

            val vehiclesResult = vehicleRepository.getVehiclesByDealer(dealerId, VehicleStatus.PUBLISHED)
            _dealerVehiclesState.value = vehiclesResult
        }
    }

    fun updateDealerProfile(dealer: Dealer) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            val result = dealerRepository.updateDealerProfile(dealer)
            _updateProfileState.value = result
            if (result is Resource.Success) {
                loadDealerDetail(dealer.id)
            }
        }
    }

    fun updateDealerLogo(context: Any? = null, dealerId: String, logoUri: Any) {
        viewModelScope.launch {
            _updateProfileState.value = Resource.Loading()
            val result = dealerRepository.updateDealerLogo(context, dealerId, logoUri)
            if (result is Resource.Success) {
                _updateProfileState.value = Resource.Success(Unit)
                loadDealerDetail(dealerId)
            } else {
                _updateProfileState.value = Resource.Error(result.message ?: "Logo güncellenemedi")
            }
        }
    }

    fun clearUpdateState() {
        _updateProfileState.value = Resource.Empty()
    }
}
