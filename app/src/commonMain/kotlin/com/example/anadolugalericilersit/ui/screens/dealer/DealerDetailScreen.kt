package com.example.anadolugalericilersit.ui.screens.dealer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.ui.components.AppAsyncImage
import com.example.anadolugalericilersit.ui.components.DealerStatusBadge
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.DealerViewModel
import com.example.anadolugalericilersit.ui.viewmodel.FavoriteViewModel
import com.example.anadolugalericilersit.utils.IntentUtils
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerDetailScreen(
    dealerId: String,
    dealerViewModel: DealerViewModel,
    favoriteViewModel: FavoriteViewModel,
    currentUserId: String?,
    onBackClick: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val dealerDetailState by dealerViewModel.dealerDetailState.collectAsState()
    val dealerVehiclesState by dealerViewModel.dealerVehiclesState.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()

    LaunchedEffect(dealerId) {
        dealerViewModel.loadDealerDetail(dealerId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri Profili") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        when (dealerDetailState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = dealerDetailState.message ?: "Galeri bulunamadı", color = MaterialTheme.colorScheme.error)
                }
            }

            is Resource.Success -> {
                val dealer = dealerDetailState.data
                if (dealer == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Galeri verisi bulunamadı.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Shop Storefront Cover Image & Profile Avatar Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column {
                                    // Shop / Storefront Photo Banner Header
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val shopImg = dealer.shopPhotoUrl.ifBlank { dealer.logoUrl }
                                        if (shopImg.isNotBlank()) {
                                            AppAsyncImage(
                                                model = shopImg,
                                                contentDescription = "Dükkan Fotoğrafı",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    Icons.Default.Store,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(48.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Galeri Dükkan Fotoğrafı", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Authorized Person / Profile Photo Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val profileImg = dealer.profilePhotoUrl
                                                if (profileImg.isNotBlank()) {
                                                    AppAsyncImage(
                                                        model = profileImg,
                                                        contentDescription = "Profil Fotoğrafı",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = dealer.galleryName, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                                Text(text = "Yetkili: ${dealer.authorizedName}", fontSize = 13.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                DealerStatusBadge(status = dealer.accountStatus)
                                            }
                                        }

                                        if (dealer.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = dealer.description,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "${dealer.address}, ${dealer.district} / ${dealer.city}", fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "Çalışma Saatleri: ${dealer.workingHours}", fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { IntentUtils.openPhoneDialer(dealer.phone) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Galeriyi Ara")
                                            }

                                            Button(
                                                onClick = { IntentUtils.openMap(dealer.latitude, dealer.longitude, dealer.galleryName) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Harita")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Vehicles Section Header
                        item {
                            Text(
                                text = "Galerinin Yayındaki Araçları",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        // Vehicle List
                        when (dealerVehiclesState) {
                            is Resource.Loading -> {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            is Resource.Success -> {
                                val vehicles = dealerVehiclesState.data ?: emptyList()
                                if (vehicles.isEmpty()) {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                            Text("Bu galerinin henüz yayında bir aracı bulunmuyor.")
                                        }
                                    }
                                } else {
                                    items(vehicles) { vehicle: Vehicle ->
                                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                            VehicleCard(
                                                vehicle = vehicle,
                                                isFavorite = favoriteIds.contains(vehicle.id),
                                                onFavoriteClick = if (currentUserId != null) {
                                                    { favoriteViewModel.toggleFavorite(vehicle.id, currentUserId) }
                                                } else null,
                                                onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                            )
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }

            else -> {}
        }
    }
}
