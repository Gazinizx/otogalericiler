package com.example.anadolugalericilersit.ui.screens.mylistings

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.VehicleViewModel
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListingsScreen(
    vehicleViewModel: VehicleViewModel,
    dealer: Dealer?,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToEditVehicle: (String) -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val myListingsState by vehicleViewModel.myListingsState.collectAsState()

    var selectedStatusTab by remember { mutableStateOf<VehicleStatus?>(null) }

    LaunchedEffect(dealer) {
        if (dealer != null) {
            vehicleViewModel.loadMyListings(dealer.id, selectedStatusTab)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlanlarım & Galeri Paneli") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddVehicle,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("İlan Ekle", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        if (dealer == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Galeri profili bulunamadı.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Dashboard Summary Cards
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = dealer.galleryName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "Galeri Performans Özeti", fontSize = 12.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                title = "Toplam İlan",
                                value = dealer.totalListings.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Yayında",
                                value = dealer.activeListings.toString(),
                                modifier = Modifier.weight(1f),
                                valueColor = Color(0xFF15803D)
                            )
                            StatCard(
                                title = "Bekleyen",
                                value = dealer.pendingListings.toString(),
                                modifier = Modifier.weight(1f),
                                valueColor = Color(0xFFA16207)
                            )
                            StatCard(
                                title = "Satılan",
                                value = dealer.soldListings.toString(),
                                modifier = Modifier.weight(1f),
                                valueColor = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }

                // Filter Status Tabs
                item {
                    val tabs = listOf(
                        null to "Tümü",
                        VehicleStatus.PUBLISHED to "Yayında",
                        VehicleStatus.PENDING to "Bekleyen",
                        VehicleStatus.DRAFT to "Taslak",
                        VehicleStatus.SOLD to "Satıldı",
                        VehicleStatus.PASSIVE to "Pasif",
                        VehicleStatus.REJECTED to "Reddedilen"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        items(tabs) { (status, label) ->
                            FilterChip(
                                selected = selectedStatusTab == status,
                                onClick = {
                                    selectedStatusTab = status
                                    vehicleViewModel.loadMyListings(dealer.id, status)
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Listings List State
                when (myListingsState) {
                    is Resource.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is Resource.Error -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(text = myListingsState.message ?: "İlanlar yüklenemedi", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    is Resource.Success -> {
                        val list = myListingsState.data ?: emptyList()
                        if (list.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("Bu filtrede henüz ilan bulunmuyor.")
                                }
                            }
                        } else {
                            items(list) { vehicle: Vehicle ->
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                    VehicleCard(
                                        vehicle = vehicle,
                                        onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                    )

                                    // Action Buttons Bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { onNavigateToEditVehicle(vehicle.id) }) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Düzenle", fontSize = 12.sp)
                                        }

                                        if (vehicle.status == VehicleStatus.PUBLISHED) {
                                            TextButton(onClick = {
                                                vehicleViewModel.updateVehicleStatus(vehicle.id, dealer.id, VehicleStatus.SOLD)
                                                ToastUtils.showToast(message = "İlan Satıldı olarak işaretlendi")
                                            }) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Satıldı Yap", fontSize = 12.sp)
                                            }

                                            TextButton(onClick = {
                                                vehicleViewModel.updateVehicleStatus(vehicle.id, dealer.id, VehicleStatus.PASSIVE)
                                                ToastUtils.showToast(message = "İlan pasife alındı")
                                            }) {
                                                Icon(Icons.Default.PauseCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pasife Al", fontSize = 12.sp)
                                            }
                                        } else if (vehicle.status == VehicleStatus.PASSIVE) {
                                            TextButton(onClick = {
                                                vehicleViewModel.updateVehicleStatus(vehicle.id, dealer.id, VehicleStatus.PUBLISHED)
                                                ToastUtils.showToast(message = "İlan tekrar yayına alındı")
                                            }) {
                                                Text("Yayına Al", fontSize = 12.sp)
                                            }
                                        }

                                        IconButton(onClick = {
                                            vehicleViewModel.deleteVehicle(vehicle.id, dealer.id)
                                            ToastUtils.showToast(message = "İlan silindi")
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
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
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}
