package com.example.anadolugalericilersit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.AdminStats
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.data.repository.AdminRepository
import com.example.anadolugalericilersit.data.repository.DealerRepository
import com.example.anadolugalericilersit.data.repository.NotificationRepository
import com.example.anadolugalericilersit.data.repository.ReportRepository
import com.example.anadolugalericilersit.data.repository.VehicleRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
    private val dealerRepository: DealerRepository = DealerRepository(),
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val reportRepository: ReportRepository = ReportRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _statsState = MutableStateFlow<Resource<AdminStats>>(Resource.Empty())
    val statsState: StateFlow<Resource<AdminStats>> = _statsState.asStateFlow()

    private val _dealersState = MutableStateFlow<Resource<List<Dealer>>>(Resource.Empty())
    val dealersState: StateFlow<Resource<List<Dealer>>> = _dealersState.asStateFlow()

    private val _vehiclesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Empty())
    val vehiclesState: StateFlow<Resource<List<Vehicle>>> = _vehiclesState.asStateFlow()

    private val _reportsState = MutableStateFlow<Resource<List<Report>>>(Resource.Empty())
    val reportsState: StateFlow<Resource<List<Report>>> = _reportsState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    fun loadStats() {
        viewModelScope.launch {
            _statsState.value = Resource.Loading()
            val result = adminRepository.getAdminStats()
            _statsState.value = result
        }
    }

    fun loadDealers(statusFilter: DealerStatus? = null) {
        viewModelScope.launch {
            _dealersState.value = Resource.Loading()
            val result = dealerRepository.getAllDealers(statusFilter)
            _dealersState.value = result
        }
    }

    fun loadVehicles(statusFilter: VehicleStatus? = null) {
        viewModelScope.launch {
            _vehiclesState.value = Resource.Loading()
            val result = vehicleRepository.getVehiclesByStatusForAdmin(statusFilter)
            _vehiclesState.value = result
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _reportsState.value = Resource.Loading()
            val result = reportRepository.getAllReports()
            _reportsState.value = result
        }
    }

    fun updateDealerStatus(dealerId: String, ownerUid: String, newStatus: DealerStatus, rejectionReason: String = "") {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = dealerRepository.updateDealerStatus(dealerId, newStatus, rejectionReason)
            _actionState.value = result
            if (result is Resource.Success) {
                // Send notification to dealer
                val notifTitle = when (newStatus) {
                    DealerStatus.APPROVED -> "Hesabınız Onaylandı!"
                    DealerStatus.REJECTED -> "Hesabınız Reddedildi"
                    DealerStatus.SUSPENDED -> "Hesabınız Askıya Alındı"
                    DealerStatus.PENDING -> "Hesabınız İnceleniyor"
                }
                val notifMsg = when (newStatus) {
                    DealerStatus.APPROVED -> "Tebrikler! Galeri başvurunuz onaylandı. Artık ilan yayınlayabilirsiniz."
                    DealerStatus.REJECTED -> "Başvurunuz reddedildi. Sebep: $rejectionReason"
                    DealerStatus.SUSPENDED -> "Hesabınız askıya alındı. Sebep: $rejectionReason"
                    DealerStatus.PENDING -> "Hesabınız incelemeye alındı."
                }
                notificationRepository.sendNotification(
                    NotificationItem(
                        userId = ownerUid,
                        title = notifTitle,
                        message = notifMsg,
                        type = "ACCOUNT_STATUS"
                    )
                )
                loadDealers()
                loadStats()
            }
        }
    }

    fun updateVehicleStatus(vehicleId: String, newStatus: VehicleStatus, rejectionReason: String = "") {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = vehicleRepository.updateVehicleStatus(vehicleId, newStatus, rejectionReason)
            _actionState.value = result
            if (result is Resource.Success) {
                loadVehicles()
                loadStats()
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = vehicleRepository.deleteVehicle(vehicleId)
            _actionState.value = result
            if (result is Resource.Success) {
                loadVehicles()
                loadStats()
            }
        }
    }

    fun deleteAllVehicles() {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = vehicleRepository.deleteAllVehicles()
            _actionState.value = result
            if (result is Resource.Success) {
                loadVehicles()
                loadStats()
            }
        }
    }

    fun updateVehicleDamgaStatus(vehicleId: String, approved: Boolean) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val damgaStatus = if (approved) "APPROVED" else "REJECTED"
            val result = vehicleRepository.updateVehicleDamgaStatus(vehicleId, damgaStatus, approved)
            _actionState.value = result
            if (result is Resource.Success) {
                loadVehicles()
            }
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading()
            val result = reportRepository.resolveReport(reportId)
            _actionState.value = result
            if (result is Resource.Success) {
                loadReports()
            }
        }
    }

    fun clearActionState() {
        _actionState.value = Resource.Empty()
    }
}
