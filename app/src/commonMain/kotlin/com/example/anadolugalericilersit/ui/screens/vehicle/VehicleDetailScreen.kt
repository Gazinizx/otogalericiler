package com.example.anadolugalericilersit.ui.screens.vehicle

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.FavoriteViewModel
import com.example.anadolugalericilersit.ui.viewmodel.VehicleViewModel
import com.example.anadolugalericilersit.utils.IntentUtils
import com.example.anadolugalericilersit.utils.Resource
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    vehicleId: String,
    vehicleViewModel: VehicleViewModel,
    favoriteViewModel: FavoriteViewModel,
    currentUserId: String?,
    userEmail: String?,
    onBackClick: () -> Unit,
    onNavigateToDealerDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val detailState by vehicleViewModel.detailState.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()
    val reportState by vehicleViewModel.reportState.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Yanlış bilgi") }
    var reportNote by remember { mutableStateOf("") }

    LaunchedEffect(vehicleId) {
        vehicleViewModel.loadVehicleDetail(vehicleId, currentUserId)
        if (currentUserId != null) {
            favoriteViewModel.loadFavorites(currentUserId)
        }
    }

    LaunchedEffect(reportState) {
        if (reportState is Resource.Success) {
            ToastUtils.showToast(message = "Şikayetiniz admine iletildi.")
            vehicleViewModel.clearReportState()
            showReportDialog = false
        }
    }

    if (reportState is Resource.Loading) {
        LoadingDialog(message = "Şikayet bildiriliyor...")
    }

    // Report Modal Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("İlanı Bildir / Şikayet Et") },
            text = {
                Column {
                    Text("Lütfen bildirim sebebini seçiniz:")
                    Spacer(modifier = Modifier.height(8.dp))

                    val reasons = listOf("Yanlış bilgi", "Yanlış fiyat", "Sahte ilan", "Uygunsuz içerik", "Diğer")
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = r }
                        ) {
                            RadioButton(selected = reportReason == r, onClick = { reportReason = r })
                            Text(r)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reportNote,
                        onValueChange = { reportNote = it },
                        label = { Text("Açıklama (Opsiyonel)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = detailState.data
                        if (v != null) {
                            vehicleViewModel.submitReport(
                                vehicleId = v.id,
                                vehicleTitle = "${v.brand} ${v.model}",
                                reporterUid = currentUserId ?: "ANONYMOUS",
                                reporterEmail = userEmail ?: "Bilinmiyor",
                                reason = reportReason,
                                note = reportNote
                            )
                        }
                    }
                ) {
                    Text("Gönder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlan Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    val isFav = favoriteIds.contains(vehicleId)
                    if (currentUserId != null) {
                        IconButton(onClick = { favoriteViewModel.toggleFavorite(vehicleId, currentUserId) }) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favori",
                                tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = {
                        val v = detailState.data
                        if (v != null) {
                            val formattedPrice = NumberFormat.getInstance(Locale("tr", "TR")).format(v.price)
                            IntentUtils.shareVehicle(context, "${v.brand} ${v.model}", formattedPrice, v.id)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Paylaş")
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Report, contentDescription = "Şikayet Et", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            val v = detailState.data
            if (v != null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { IntentUtils.openDialer(context, v.dealerPhone) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ara")
                        }

                        Button(
                            onClick = { IntentUtils.openWhatsApp(context, v.dealerPhone, "Merhaba, ${v.brand} ${v.model} ilanınızla ilgileniyorum.") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("WhatsApp", color = Color.White)
                        }

                        Button(
                            onClick = { IntentUtils.openMaps(context, 39.9334, 32.8597, v.dealerName) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Yol Tarifi")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (detailState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = detailState.message ?: "İlan yüklenemedi", color = MaterialTheme.colorScheme.error)
                }
            }

            is Resource.Success -> {
                val vehicle = detailState.data
                if (vehicle == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("İlan verisi bulunamadı.")
                    }
                } else {
                    val images = vehicle.imageUrls.ifEmpty { listOf(vehicle.mainImageUrl) }
                    val pagerState = rememberPagerState(pageCount = { images.size })

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Image Pager
                        Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = images[page],
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Image count indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(Color(0xAA000000), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1}/${images.size}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Thumbnail Row
                        if (images.size > 1) {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(images) { index, url ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        // Vehicle Main Info Header
                        Column(modifier = Modifier.padding(16.dp)) {
                            val formattedPrice = try {
                                "${NumberFormat.getInstance(Locale("tr", "TR")).format(vehicle.price)} ₺"
                            } catch (e: Exception) {
                                "${vehicle.price} ₺"
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formattedPrice,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (vehicle.negotiable) {
                                        Badge(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF15803D)) {
                                            Text("Pazarlık Var", modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                    if (vehicle.suitableForCredit) {
                                        Badge(containerColor = Color(0xFFDBEAFE), contentColor = Color(0xFF1D4ED8)) {
                                            Text("Krediye Uygun", modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${vehicle.brand} ${vehicle.model}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "İlan No: ${vehicle.id} • Görüntülenme: ${vehicle.viewCount}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Video Link Card if available
                            if (vehicle.videoUrl.isNotBlank()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        ToastUtils.showToast(message = "Araç videosu oynatılıyor")
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Araç Tanıtım Videosunu İzle", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Specs Grid
                            Text(text = "Araç Özellikleri", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SpecRow("Marka", vehicle.brand)
                                SpecRow("Model", vehicle.model)
                                SpecRow("Yıl", vehicle.year.toString())
                                SpecRow("Kilometre", "${NumberFormat.getInstance(Locale("tr", "TR")).format(vehicle.km)} KM")
                                SpecRow("Yakıt Tipi", vehicle.fuelType)
                                SpecRow("Vites Tipi", vehicle.transmission)
                                SpecRow("Kasa Tipi", vehicle.bodyType)
                                SpecRow("Motor Hacmi", vehicle.engineSize)
                                SpecRow("Motor Gücü", vehicle.enginePower)
                                SpecRow("Çekiş", vehicle.drivetrain)
                                SpecRow("Renk", vehicle.color)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Expert Report Card
                            if (vehicle.expertReportStatus.isNotBlank() || vehicle.expertReportDetails.isNotBlank() || vehicle.expertReportImageUrl.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📋 Ekspertiz Raporu & Araç Durumu",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            if (vehicle.expertReportStatus.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = vehicle.expertReportStatus,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (vehicle.expertReportDetails.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Detaylar: ${vehicle.expertReportDetails}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (vehicle.expertInspectionCost > 0) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Ekspertiz Ücreti: ${vehicle.expertInspectionCost.toInt()} ₺",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }

                                        // Kaporta ve Parça Durum Şeması Tablosu
                                        if (vehicle.bodyPartsCondition.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Kaporta ve Parça Durum Şeması:",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                vehicle.bodyPartsCondition.forEach { (part, cond) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(text = part, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = when (cond) {
                                                                "Orijinal" -> Color(0xFF15803D)
                                                                "Lokal Boyalı" -> Color(0xFFD97706)
                                                                "Boyalı" -> Color(0xFF2563EB)
                                                                "Değişen" -> Color(0xFFDC2626)
                                                                else -> Color.Gray
                                                            }
                                                        ) {
                                                            Text(
                                                                text = cond,
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (vehicle.expertReportImageUrl.isNotBlank()) {

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = "Ekspertiz Belgesi / Rapor Fotoğrafı:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            ) {
                                                AsyncImage(
                                                    model = vehicle.expertReportImageUrl,
                                                    contentDescription = "Ekspertiz Raporu Belgesi",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))


                            // Equipment Features
                            if (vehicle.features.isNotEmpty()) {
                                Text(text = "Donanım Özellikleri (${vehicle.features.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Column {
                                    vehicle.features.chunked(2).forEach { pair ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                            pair.forEach { feat ->
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(text = feat, fontSize = 13.sp)
                                                }
                                            }
                                            if (pair.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            // Description
                            Text(text = "Açıklama", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = vehicle.description.ifBlank { "Satıcı açıklama belirtmemiş." },
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Dealer Card
                            Text(text = "İlan Sahibi Galeri", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onNavigateToDealerDetail(vehicle.dealerId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (vehicle.dealerLogoUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = vehicle.dealerLogoUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Store, contentDescription = null)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = vehicle.dealerName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(text = "${vehicle.dealerCity}, ${vehicle.dealerDistrict}", fontSize = 12.sp, color = Color.Gray)
                                        Text(text = vehicle.dealerPhone, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
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

@Composable
fun SpecRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 13.sp, color = Color.Gray)
        Text(text = value.ifBlank { "-" }, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
