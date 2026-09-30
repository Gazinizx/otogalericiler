package com.example.anadolugalericilersit.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.data.repository.AuthRepository
import com.example.anadolugalericilersit.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<User>>(Resource.Empty())
    val authState: StateFlow<Resource<User>> = _authState.asStateFlow()

    private val _dealerState = MutableStateFlow<Resource<Dealer>>(Resource.Empty())
    val dealerState: StateFlow<Resource<Dealer>> = _dealerState.asStateFlow()

    private val _registerState = MutableStateFlow<Resource<Dealer>>(Resource.Empty())
    val registerState: StateFlow<Resource<Dealer>> = _registerState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        if (!authRepository.isUserLoggedIn()) {
            _authState.value = Resource.Error("Oturum açık değil")
            return
        }

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val userResult = authRepository.getCurrentUser()
            _authState.value = userResult

            if (userResult is Resource.Success && userResult.data != null) {
                val user = userResult.data
                if (user.role != Role.SUPER_ADMIN && user.role != Role.ADMIN) {
                    loadDealerProfile()
                } else {
                    _dealerState.value = Resource.Empty()
                }
            }
        }
    }

    fun loadDealerProfile() {
        viewModelScope.launch {
            _dealerState.value = Resource.Loading()
            val result = authRepository.getCurrentDealerProfile()
            _dealerState.value = result
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("E-posta ve şifre boş bırakılamaz")
            return
        }

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = authRepository.login(email, password)
            _authState.value = result
            if (result is Resource.Success && result.data != null) {
                val user = result.data
                if (user.role != Role.SUPER_ADMIN && user.role != Role.ADMIN) {
                    loadDealerProfile()
                } else {
                    _dealerState.value = Resource.Empty()
                }
            }
        }
    }

    fun registerDealer(
        context: Any? = null,
        galleryName: String,
        authorizedName: String,
        phone: String,
        email: String,
        password: String,
        city: String,
        district: String,
        address: String,
        taxNumber: String,
        iban: String,
        ibanOwnerName: String,
        description: String,
        logoUri: Any? = null
    ) {
        if (galleryName.isBlank() || email.isBlank() || password.isBlank() || phone.isBlank()) {
            _registerState.value = Resource.Error("Lütfen gerekli alanları doldurun")
            return
        }

        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            val result = authRepository.registerDealer(
                context, galleryName, authorizedName, phone, email, password,
                city, district, address, taxNumber, iban, ibanOwnerName, description, logoUri
            )
            _registerState.value = result
            if (result is Resource.Success && result.data != null) {
                _dealerState.value = Resource.Success(result.data)
                checkSession()
            }
        }
    }

    fun resetPassword(email: String, onResult: (String?) -> Unit) {
        if (email.isBlank()) {
            onResult("Lütfen e-posta adresinizi girin")
            return
        }

        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            if (result is Resource.Success) {
                onResult(null)
            } else {
                onResult(result.message ?: "Hata oluştu")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = Resource.Empty()
        _dealerState.value = Resource.Empty()
        _registerState.value = Resource.Empty()
    }

    fun deleteAccount(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.deleteAccount()
            if (res is Resource.Success) {
                logout()
                onResult(null)
            } else {
                onResult(res.message ?: "Hesap silinemedi")
            }
        }
    }

    fun clearRegisterState() {
        _registerState.value = Resource.Empty()
    }
}
