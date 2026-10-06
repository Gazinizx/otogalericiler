package com.example.anadolugalericilersit.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.DamgaStatus

import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.AdminViewModel
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.ToastUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminVehiclesScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val vehiclesState by adminViewModel.vehiclesState.collectAsState()
    val actionState by adminViewModel.actionState.collectAsState()

    var selectedStatus by remember { mutableStateOf<VehicleStatus?>(VehicleStatus.PENDING) }
    var isDamgaTab by remember { mutableStateOf(false) }

    var showRejectDialog by remember { mutableStateOf(false) }
    var targetVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    LaunchedEffect(selectedStatus, isDamgaTab) {
        if (isDamgaTab) {
            adminViewModel.loadVehicles(null)
        } else {
            adminViewModel.loadVehicles(selectedStatus)
        }
    }

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            ToastUtils.showToast(message = "İşlem başarılı.")
            adminViewModel.clearActionState()
            showRejectDialog = false
            adminViewModel.loadVehicles(selectedStatus)
        }
    }

    if (actionState is Resource.Loading) {
        LoadingDialog(message = "İşlem uygulanıyor...")
    }

    if (showRejectDialog && targetVehicle != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("İlanı Reddet") },
            text = {
                Column {
                    Text("Lütfen red sebebini yazınız:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Açıklama") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = targetVehicle
                        if (v != null) {
                            adminViewModel.updateVehicleStatus(v.id, VehicleStatus.REJECTED, rejectionReason)
                        }
                    }
                ) {
                    Text("Reddet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Tüm İlanları Sil") },
            text = { Text("Veritabanındaki ve uygulamadaki BÜTÜN ilanlar kalıcı olarak silinecektir. Emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteAllVehicles()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Hepsini Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlan Yönetimi (Admin)") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    TextButton(onClick = { showDeleteAllDialog = true }) {
                        Text("Tümünü Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val tabs = listOf(
                VehicleStatus.PENDING to "Bekleyenler",
                VehicleStatus.PUBLISHED to "Yayındakiler",
                VehicleStatus.REJECTED to "Reddedilenler",
                VehicleStatus.SOLD to "Satılanlar",
                VehicleStatus.PASSIVE to "Pasifler",
                null to "Tümü"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { (status, label) ->
                    FilterChip(
                        selected = !isDamgaTab && selectedStatus == status,
                        onClick = {
                            isDamgaTab = false
                            selectedStatus = status
                        },
                        label = { Text(label) }
                    )
                }
                item {
                    FilterChip(
                        selected = isDamgaTab,
                        onClick = { isDamgaTab = true },
                        label = { Text("⭐ Damga Bekleyenler (5K+ TL)") }
                    )
                }
            }

            when (vehiclesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = vehiclesState.message ?: "Hata oluştu", color = MaterialTheme.colorScheme.error)
                    }
                }

                is Resource.Success -> {
                    val rawList = vehiclesState.data ?: emptyList()
                    val list = if (isDamgaTab) {
                        rawList.filter { it.damgaStatus == DamgaStatus.PENDING && it.expertInspectionCost >= 5000.0 }
                    } else {
                        rawList
                    }
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (isDamgaTab) "Bekleyen damga talebi bulunmuyor." else "Bu filtrede ilan kaydı bulunmuyor.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { vehicle: Vehicle ->
                                val dealer = LocalStore.dealers[vehicle.dealerId]
                                Column {
                                    VehicleCard(
                                        vehicle = vehicle,
                                        onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                    )

                                    if (isDamgaTab) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("Ekspertiz Ücreti: ${vehicle.expertInspectionCost} TL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Galeri: ${vehicle.dealerName}", fontSize = 12.sp)
                                                Text("IBAN Bilgisi: ${dealer?.iban ?: "Belirtilmemiş"}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isDamgaTab) {
                                            Button(
                                                onClick = {
                                                    adminViewModel.updateVehicleDamgaStatus(vehicle.id, true)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                                            ) {
                                                Text("Damgayı Onayla")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    adminViewModel.updateVehicleDamgaStatus(vehicle.id, false)
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Damgayı Reddet")
                                            }
                                        } else if (vehicle.status == VehicleStatus.PENDING) {
                                            Button(
                                                onClick = {
                                                    adminViewModel.updateVehicleStatus(vehicle.id, VehicleStatus.PUBLISHED)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                                            ) {
                                                Text("Onayla & Yayınla")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    targetVehicle = vehicle
                                                    rejectionReason = ""
                                                    showRejectDialog = true
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Reddet")
                                            }
                                        } else if (vehicle.status == VehicleStatus.PUBLISHED) {
                                            OutlinedButton(
                                                onClick = {
                                                    adminViewModel.updateVehicleStatus(vehicle.id, VehicleStatus.PASSIVE)
                                                }
                                            ) {
                                                Text("Pasife Al")
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    adminViewModel.updateVehicleStatus(vehicle.id, VehicleStatus.PUBLISHED)
                                                }
                                            ) {
                                                Text("Yayınla")
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                adminViewModel.deleteVehicle(vehicle.id)
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Sil")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}
