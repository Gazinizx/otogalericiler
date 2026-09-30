package com.example.anadolugalericilersit.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.data.repository.ReportRepository
import com.example.anadolugalericilersit.data.repository.VehicleRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _detailState = MutableStateFlow<Resource<Vehicle>>(Resource.Empty())
    val detailState: StateFlow<Resource<Vehicle>> = _detailState.asStateFlow()

    private val _searchListState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Empty())
    val searchListState: StateFlow<Resource<List<Vehicle>>> = _searchListState.asStateFlow()

    private val _myListingsState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Empty())
    val myListingsState: StateFlow<Resource<List<Vehicle>>> = _myListingsState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Vehicle>>(Resource.Empty())
    val saveState: StateFlow<Resource<Vehicle>> = _saveState.asStateFlow()

    private val _uploadProgress = MutableStateFlow<Pair<Int, String>?>(null)
    val uploadProgress: StateFlow<Pair<Int, String>?> = _uploadProgress.asStateFlow()

    private val _reportState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val reportState: StateFlow<Resource<Unit>> = _reportState.asStateFlow()

    private val _currentFilter = MutableStateFlow(VehicleFilter())
    val currentFilter: StateFlow<VehicleFilter> = _currentFilter.asStateFlow()

    private val _currentSort = MutableStateFlow(VehicleSort.NEWEST)
    val currentSort: StateFlow<VehicleSort> = _currentSort.asStateFlow()

    fun loadVehicleDetail(vehicleId: String, currentUserId: String? = null) {
        viewModelScope.launch {
            _detailState.value = Resource.Loading()
            val result = vehicleRepository.getVehicleById(vehicleId)
            _detailState.value = result

            if (result is Resource.Success && result.data != null) {
                vehicleRepository.incrementViewCount(vehicleId, currentUserId)
            }
        }
    }

    fun searchVehicles(filter: VehicleFilter = _currentFilter.value, sort: VehicleSort = _currentSort.value) {
        _currentFilter.value = filter
        _currentSort.value = sort
        viewModelScope.launch {
            _searchListState.value = Resource.Loading()
            val result = vehicleRepository.getVehicles(filter = filter, sort = sort)
            _searchListState.value = result
        }
    }

    fun loadMyListings(dealerId: String, statusFilter: VehicleStatus? = null) {
        viewModelScope.launch {
            _myListingsState.value = Resource.Loading()
            val result = vehicleRepository.getVehiclesByDealer(dealerId, statusFilter)
            _myListingsState.value = result
        }
    }

    fun saveVehicle(
        context: Any? = null,
        vehicle: Vehicle,
        newImageUris: List<Any> = emptyList(),
        videoUri: Any? = null,
        expertReportUri: Any? = null
    ) {
        if (vehicle.brand.isBlank() || vehicle.model.isBlank() || vehicle.price <= 0) {
            _saveState.value = Resource.Error("Lütfen marka, model ve geçerli fiyat giriniz")
            return
        }

        viewModelScope.launch {
            _saveState.value = Resource.Loading()
            val result = vehicleRepository.saveVehicle(
                context,
                vehicle,
                newImageUris,
                videoUri,
                expertReportUri
            ) { percent, message ->
                _uploadProgress.value = Pair(percent, message)
            }
            _saveState.value = result
            _uploadProgress.value = null
        }
    }


    fun updateVehicleStatus(vehicleId: String, dealerId: String, newStatus: VehicleStatus) {
        viewModelScope.launch {
            val result = vehicleRepository.updateVehicleStatus(vehicleId, newStatus)
            if (result is Resource.Success) {
                loadMyListings(dealerId)
            }
        }
    }

    fun deleteVehicle(vehicleId: String, dealerId: String) {
        viewModelScope.launch {
            val result = vehicleRepository.deleteVehicle(vehicleId)
            if (result is Resource.Success) {
                loadMyListings(dealerId)
            }
        }
    }

    fun submitReport(vehicleId: String, vehicleTitle: String, reporterUid: String, reporterEmail: String, reason: String, note: String) {
        viewModelScope.launch {
            _reportState.value = Resource.Loading()
            val report = Report(
                vehicleId = vehicleId,
                vehicleTitle = vehicleTitle,
                reporterUid = reporterUid,
                reporterEmail = reporterEmail,
                reason = reason,
                note = note
            )
            val result = reportRepository.submitReport(report)
            _reportState.value = result
        }
    }

    fun clearSaveState() {
        _saveState.value = Resource.Empty()
    }

    fun clearReportState() {
        _reportState.value = Resource.Empty()
    }
}
