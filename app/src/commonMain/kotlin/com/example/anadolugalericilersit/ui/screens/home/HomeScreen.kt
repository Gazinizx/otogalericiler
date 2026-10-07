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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
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
import com.example.anadolugalericilersit.ui.components.BrandChip
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
    val announcementsState by homeViewModel.announcementsState.collectAsState()
    val selectedBrand by homeViewModel.selectedBrand.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()
    val versionUpdateState by homeViewModel.versionUpdateState.collectAsState()

    val dynamicAnnouncements = (announcementsState as? Resource.Success)?.data ?: emptyList()

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
                        Text(
                            text = "Anadolu Galericiler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Bildirimler")
                    }
                }
            )
        },
        floatingActionButton = {
            if (isApprovedDealer) {
                FloatingActionButton(
                    onClick = onNavigateToAddVehicle,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "İlan Ekle", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar Header Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSearch() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .fillMaxWidth(),
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

            // Announcements Board / Duyurular Panosu (Dynamic from Admin + Static fallback)
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
                        if (dynamicAnnouncements.isNotEmpty()) {
                            items(dynamicAnnouncements) { announcement ->
                                Card(
                                    modifier = Modifier
                                        .width(340.dp)
                                        .height(170.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (announcement.imageUrl.isNotBlank()) {
                                        AppAsyncImage(
                                            model = announcement.imageUrl,
                                            contentDescription = "Duyuru Görseli",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = announcement.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = announcement.message,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
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

            // Recommended Vehicles / Beğenebileceğiniz Araçlar
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popüler / Öne Çıkan Araçlar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = onNavigateToSearch) {
                            Text("Tümü")
                        }
                    }

                    when (recommendedState) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is Resource.Success -> {
                            val list = recommendedState.data ?: emptyList()
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    Text("Henüz araç ilanı bulunmuyor", color = Color.Gray)
                                }
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(list) { vehicle: Vehicle ->
                                        VehicleCard(
                                            vehicle = vehicle,
                                            isFavorite = favoriteIds.contains(vehicle.id),
                                            onFavoriteClick = {
                                                if (currentUserId != null) {
                                                    favoriteViewModel.toggleFavorite(vehicle.id, currentUserId)
                                                }
                                            },
                                            onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                        )
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text(text = recommendedState.message ?: "Hata oluştu", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {}
                    }
                }
            }

            // New Arrivals / Son Eklenen İlanlar
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Son Eklenen İlanlar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = onNavigateToSearch) {
                            Text("Tümü +")
                        }
                    }

                    when (newArrivalsState) {
                        is Resource.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        is Resource.Success -> {
                            val list = newArrivalsState.data ?: emptyList()
                            if (list.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    Text("Henüz yeni ilan yok", color = Color.Gray)
                                }
                            } else {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(list) { vehicle: Vehicle ->
                                        VehicleCard(
                                            vehicle = vehicle,
                                            isFavorite = favoriteIds.contains(vehicle.id),
                                            onFavoriteClick = {
                                                if (currentUserId != null) {
                                                    favoriteViewModel.toggleFavorite(vehicle.id, currentUserId)
                                                }
                                            },
                                            onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                        )
                                    }
                                }
                            }
                        }
                        is Resource.Error -> {
                            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                Text(text = newArrivalsState.message ?: "HData oluştu", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
