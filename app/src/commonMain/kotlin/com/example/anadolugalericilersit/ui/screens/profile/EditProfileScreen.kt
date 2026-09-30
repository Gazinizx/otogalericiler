package com.example.anadolugalericilersit.ui.screens.profile

import com.example.anadolugalericilersit.utils.ToastUtils
import com.example.anadolugalericilersit.utils.rememberImagePickerLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.DealerViewModel
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    dealerViewModel: DealerViewModel,
    dealer: Dealer?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val updateState by dealerViewModel.updateProfileState.collectAsState()

    var galleryName by remember { mutableStateOf(dealer?.galleryName ?: "") }
    var authorizedName by remember { mutableStateOf(dealer?.authorizedName ?: "") }
    var phone by remember { mutableStateOf(dealer?.phone ?: "") }
    var city by remember { mutableStateOf(dealer?.city ?: "") }
    var district by remember { mutableStateOf(dealer?.district ?: "") }
    var address by remember { mutableStateOf(dealer?.address ?: "") }
    var taxNumber by remember { mutableStateOf(dealer?.taxNumber ?: "") }
    var iban by remember { mutableStateOf(dealer?.iban ?: "") }
    var ibanOwnerName by remember { mutableStateOf(dealer?.ibanOwnerName ?: "") }
    var description by remember { mutableStateOf(dealer?.description ?: "") }
    var workingHours by remember { mutableStateOf(dealer?.workingHours ?: "09:00 - 18:00") }

    val logoPicker = rememberImagePickerLauncher { uriStr ->
        if (uriStr != null && dealer != null) {
            dealerViewModel.updateDealerLogo(context, dealer.id, uriStr)
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            ToastUtils.showToast(context, "Profil güncellendi!")
            dealerViewModel.clearUpdateState()
            onBackClick()
        }
    }

    if (updateState is Resource.Loading) {
        LoadingDialog(message = "Profil güncelleniyor...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri Profilini Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Change
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { logoPicker() },
                contentAlignment = Alignment.Center
            ) {
                if (dealer?.logoUrl.isNullOrBlank().not()) {
                    AsyncImage(
                        model = dealer?.logoUrl,
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.AddAPhoto, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = galleryName,
                onValueChange = { galleryName = it },
                label = { Text("Galeri Adı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = authorizedName,
                onValueChange = { authorizedName = it },
                label = { Text("Yetkili Ad Soyad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefon") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("İl") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("İlçe") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Açık Adres") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = workingHours,
                onValueChange = { workingHours = it },
                label = { Text("Çalışma Saatleri") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = iban,
                onValueChange = { iban = it },
                label = { Text("Banka IBAN Numarası") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = ibanOwnerName,
                onValueChange = { ibanOwnerName = it },
                label = { Text("IBAN Sahibinin Adı Soyadı (Kimlikteki Gibi)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Galeri Hakkında") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            if (updateState is Resource.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = updateState.message ?: "Güncelleme hatası",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (dealer != null) {
                        val updated = dealer.copy(
                            galleryName = galleryName,
                            authorizedName = authorizedName,
                            phone = phone,
                            city = city,
                            district = district,
                            address = address,
                            iban = iban,
                            ibanOwnerName = ibanOwnerName,
                            description = description,
                            workingHours = workingHours
                        )
                        dealerViewModel.updateDealerProfile(updated)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Değişiklikleri Kaydet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
