package com.example.anadolugalericilersit.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.AppVersionConfig
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.data.repository.DealerRepository
import com.example.anadolugalericilersit.data.repository.NotificationRepository
import com.example.anadolugalericilersit.data.repository.VehicleRepository
import com.example.anadolugalericilersit.utils.AppVersionUtils
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateDialogState(
    val showDialog: Boolean = false,
    val config: AppVersionConfig = AppVersionConfig(),
    val isForce: Boolean = false
)

class HomeViewModel(
    private val vehicleRepository: VehicleRepository = VehicleRepository(),
    private val dealerRepository: DealerRepository = DealerRepository()
) : ViewModel() {

    private val notificationRepository = NotificationRepository()

    private val _vehiclesState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Loading())
    val vehiclesState: StateFlow<Resource<List<Vehicle>>> = _vehiclesState.asStateFlow()

    private val _dealersState = MutableStateFlow<Resource<List<Dealer>>>(Resource.Loading())
    val dealersState: StateFlow<Resource<List<Dealer>>> = _dealersState.asStateFlow()

    private val _recommendedState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Loading())
    val recommendedState: StateFlow<Resource<List<Vehicle>>> = _recommendedState.asStateFlow()

    private val _newArrivalsState = MutableStateFlow<Resource<List<Vehicle>>>(Resource.Loading())
    val newArrivalsState: StateFlow<Resource<List<Vehicle>>> = _newArrivalsState.asStateFlow()

    private val _announcementsState = MutableStateFlow<Resource<List<NotificationItem>>>(Resource.Loading())
    val announcementsState: StateFlow<Resource<List<NotificationItem>>> = _announcementsState.asStateFlow()

    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()

    private val _versionUpdateState = MutableStateFlow(UpdateDialogState())
    val versionUpdateState: StateFlow<UpdateDialogState> = _versionUpdateState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _vehiclesState.value = Resource.Loading()
            _dealersState.value = Resource.Loading()
            _recommendedState.value = Resource.Loading()
            _newArrivalsState.value = Resource.Loading()

            try {
                val notifRes = notificationRepository.getNotifications("all")
                if (notifRes is Resource.Success) {
                    _announcementsState.value = notifRes
                } else if (notifRes is Resource.Error) {
                    _announcementsState.value = notifRes
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _announcementsState.value = Resource.Error(e.message ?: "Duyurular yüklenemedi")
            }

            val filter = VehicleFilter(brand = _selectedBrand.value)
            try {
                _vehiclesState.value = vehicleRepository.getVehicles(filter = filter, sort = VehicleSort.NEWEST)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _vehiclesState.value = Resource.Error(e.message ?: "Araçlar yüklenemedi")
            }

            try {
                _dealersState.value = dealerRepository.getAllDealers(statusFilter = DealerStatus.APPROVED)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _dealersState.value = Resource.Error(e.message ?: "Galeriler yüklenemedi")
            }

            try {
                _recommendedState.value = vehicleRepository.getVehicles(sort = VehicleSort.PRICE_DESC, limit = 10)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _recommendedState.value = Resource.Error(e.message ?: "Önerilen araçlar yüklenemedi")
            }

            try {
                _newArrivalsState.value = vehicleRepository.getVehicles(sort = VehicleSort.NEWEST, limit = 10)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                _newArrivalsState.value = Resource.Error(e.message ?: "Yeni araçlar yüklenemedi")
            }
        }
    }

    fun checkVersion(context: Any? = null) {
        viewModelScope.launch {
            val config = AppVersionUtils.checkAppVersion() ?: return@launch
            val currentCode = AppVersionUtils.getCurrentVersionCode(context)

            val isForce = config.isForceUpdate || currentCode < config.minVersionCode
            val isUpdateAvailable = currentCode < config.latestVersionCode

            if (isUpdateAvailable) {
                _versionUpdateState.value = UpdateDialogState(
                    showDialog = true,
                    config = config,
                    isForce = isForce
                )
            }
        }
    }

    fun dismissUpdateDialog() {
        _versionUpdateState.value = _versionUpdateState.value.copy(showDialog = false)
    }

    fun selectBrand(brand: String?) {
        if (_selectedBrand.value == brand) {
            _selectedBrand.value = null
        } else {
            _selectedBrand.value = brand
        }
        loadData()
    }
}
