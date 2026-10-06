package com.example.anadolugalericilersit.ui.screens.home


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.ui.components.BrandChip
import com.example.anadolugalericilersit.ui.components.DealerCard
import com.example.anadolugalericilersit.ui.components.UpdateDialog
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.FavoriteViewModel
import com.example.anadolugalericilersit.ui.viewmodel.HomeViewModel
import com.example.anadolugalericilersit.utils.Constants
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    favoriteViewModel: FavoriteViewModel,
    isDealer: Boolean,
    isApprovedDealer: Boolean,
    currentUserId: String?,
    onNavigateToSearch: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit,
    onNavigateToDealerDetail: (String) -> Unit,
    onNavigateToAddVehicle: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val recommendedState by homeViewModel.recommendedState.collectAsState()
    val newArrivalsState by homeViewModel.newArrivalsState.collectAsState()
    val dealersState by homeViewModel.dealersState.collectAsState()
    val selectedBrand by homeViewModel.selectedBrand.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()
    val versionUpdateState by homeViewModel.versionUpdateState.collectAsState()

    LaunchedEffect(Unit) {
        homeViewModel.loadData()
        homeViewModel.checkVersion()
    }

    if (versionUpdateState.showDialog) {
        UpdateDialog(
            config = versionUpdateState.config,
            isForce = versionUpdateState.isForce,
            onDismiss = { homeViewModel.dismissUpdateDialog() }
        )
    }


    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            favoriteViewModel.loadFavorites(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Anadolu Oto Galericiler Sitesi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { homeViewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                    if (currentUserId != null) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Bildirimler")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isApprovedDealer) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddVehicle,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("İlan Ekle", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Search Bar Fake Clickable Input
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onNavigateToSearch() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Marka, model veya şehir ara...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Announcements Board / Duyurular Panosu
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Duyurular Panosu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.width(310.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🏆 6 Damga Ödül Kampanyası", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Ekspertizli ve onaylı 6 damgalı araç ekleyen galericilerimize anında 5.000 TL Nakit Ödül! QR kodunuzu yöneticimize taratın.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.width(310.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📢 Anadolu Galericiler Sitesi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Türkiye'nin en hızlı ve güvenilir galerici ağında ilanlarınız anında yayında. Tüm cihazlarda canlı ve kesintisiz ilan yönetimi.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.width(310.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🚀 Fotoğraflı HD İlanlar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Yenilenen bulut altyapımız ile araç fotoğraflarınız yüksek kalitede, bellek hatası olmadan anında yüklenir ve açılır.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Brands Horizontal Row
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Marka Kategorileri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            BrandChip(
                                brandName = "Tümü",
                                isSelected = selectedBrand == null,
                                onClick = { homeViewModel.selectBrand(null) }
                            )
                        }
                        items(Constants.BRANDS_WITH_MODELS.keys.toList()) { brand ->
                            BrandChip(
                                brandName = brand,
                                isSelected = selectedBrand == brand,
                                onClick = { homeViewModel.selectBrand(brand) }
                            )
                        }
                    }
                }
            }

            // Featured Section: Yeni Gelen Araçlar (Öne Çıkanlar)
            item {
                if (newArrivalsState is Resource.Success && newArrivalsState.data.isNullOrEmpty().not()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Öne Çıkanlar: Yeni Gelen Araçlar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tümünü Gör",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onNavigateToSearch() }
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(newArrivalsState.data ?: emptyList()) { vehicle: Vehicle ->
                                Box(modifier = Modifier.width(300.dp)) {
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
                }
            }

            // Approved Dealers Row
            item {
                if (dealersState is Resource.Success && dealersState.data.isNullOrEmpty().not()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = "Öne Çıkan Galeriler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(dealersState.data ?: emptyList()) { dealer: Dealer ->
                                Box(modifier = Modifier.width(280.dp)) {
                                    DealerCard(
                                        dealer = dealer,
                                        onClick = { onNavigateToDealerDetail(dealer.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recommended Section Header: Beğenebileceğiniz Araçlar (Replaces Son Eklenenler)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Beğenebileceğiniz Araçlar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "İlgi alanlarınıza, incelemelerinize ve favorilerinize göre filtrelendi",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Recommended Vehicles List State Handling
            when (recommendedState) {
                is Resource.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is Resource.Error -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = recommendedState.message ?: "Önerilen araçlar yüklenemedi",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                is Resource.Success -> {
                    val list = recommendedState.data ?: emptyList()
                    if (list.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz önerilen bir araç ilanı bulunmuyor.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(list) { vehicle: Vehicle ->
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
