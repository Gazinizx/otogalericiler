package com.example.anadolugalericilersit.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.RewardRequest
import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.data.repository.NotificationRepository
import com.example.anadolugalericilersit.ui.components.AppAsyncImage
import com.example.anadolugalericilersit.ui.viewmodel.AdminViewModel
import com.example.anadolugalericilersit.utils.IntentUtils
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.rememberImagePickerLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onNavigateToDealers: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToDamga: () -> Unit,
    onNavigateToDebts: () -> Unit = {}
) {
    val statsState by adminViewModel.statsState.collectAsState()

    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var showManageAnnouncementsDialog by remember { mutableStateOf(false) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementMessage by remember { mutableStateOf("") }
    var announcementImageUrl by remember { mutableStateOf("") }
    var isSendingAnnouncement by remember { mutableStateOf(false) }

    var activeAnnouncements by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    val notificationRepository = remember { NotificationRepository() }

    val announcementImagePicker = rememberImagePickerLauncher { uriStr ->
        if (uriStr != null) {
            announcementImageUrl = uriStr
        }
    }

    LaunchedEffect(Unit) {
        adminViewModel.loadStats()
    }

    LaunchedEffect(showManageAnnouncementsDialog) {
        if (showManageAnnouncementsDialog) {
            val res = notificationRepository.getNotifications("all")
            if (res is Resource.Success) {
                activeAnnouncements = res.data ?: emptyList()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Yönetim Paneli") },
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
                .padding(16.dp)
        ) {
            Text(text = "Sistem İstatistikleri", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            when (statsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Success -> {
                    val stats = statsState.data
                    if (stats != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminStatBox(title = "Toplam Galeri", value = stats.totalDealers.toString(), modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Onay Bekleyen Galeri", value = stats.pendingDealers.toString(), valueColor = Color(0xFFA16207), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminStatBox(title = "Yayındaki İlan", value = stats.publishedVehicles.toString(), valueColor = Color(0xFF15803D), modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Onay Bekleyen İlan", value = stats.pendingVehicles.toString(), valueColor = Color(0xFFA16207), modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AdminStatBox(title = "Satılan Araç", value = stats.soldVehicles.toString(), valueColor = Color(0xFF1D4ED8), modifier = Modifier.weight(1f))
                            AdminStatBox(title = "Şikayetler", value = stats.totalReports.toString(), valueColor = Color(0xFFB91C1C), modifier = Modifier.weight(1f))
                        }
                    }
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "Yönetim Menüsü", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            AdminNavigationCard(
                icon = Icons.AutoMirrored.Filled.Send,
                title = "Aylık Raporu ve İBAN'ları WhatsApp'tan Gönder",
                subtitle = "Borçlu galerileri ve damga ödülü kazananların İBAN'larını WhatsApp ile ilet",
                onClick = {
                    val report = generateWhatsAppReport()
                    IntentUtils.openWhatsApp(phone = "05054543099", message = report)
                }
            )

            AdminNavigationCard(
                icon = Icons.Default.Notifications,
                title = "Duyuru ve Kampanya Gönder",
                subtitle = "Tüm kullanıcılara ve galerilere görselli duyuru yayınla",
                onClick = { showAnnouncementDialog = true }
            )

            AdminNavigationCard(
                icon = Icons.Default.Delete,
                title = "Aktif Duyuruları Sil / Yönet",
                subtitle = "Yayınlanan duyuru ve kampanyaları görüntüle veya sil",
                onClick = { showManageAnnouncementsDialog = true }
            )

            AdminNavigationCard(
                icon = Icons.Default.Store,
                title = "Galerici Başvuruları ve Yönetimi",
                subtitle = "Galerici onaylama, reddetme ve askıya alma işlemleri",
                onClick = onNavigateToDealers
            )

            AdminNavigationCard(
                icon = Icons.Default.DirectionsCar,
                title = "Araç İlan Yönetimi",
                subtitle = "İlanları onaylama, pasife alma veya silme",
                onClick = onNavigateToVehicles
            )

            AdminNavigationCard(
                icon = Icons.Default.Report,
                title = "Gelen İlan Şikayetleri",
                subtitle = "Kullanıcıların bildirdiği sahte ve hatalı ilanlar",
                onClick = onNavigateToReports
            )

            AdminNavigationCard(
                icon = Icons.Default.Check,
                title = "Damga ve 6 Damga Ödül Onayları",
                subtitle = "5.000 TL üzeri ekspertiz yaptıran galericilerin damgaları ve ödül onayları",
                onClick = onNavigateToDamga
            )

            AdminNavigationCard(
                icon = Icons.Default.Store,
                title = "Galerici Borç & Cari Takip",
                subtitle = "Arama ile galericilerin borç durumlarını gör, tutar ve işlem geçmişi ekle",
                onClick = onNavigateToDebts
            )
        }
    }

    if (showAnnouncementDialog) {
        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = { Text("Duyuru / Kampanya Yayınla") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = announcementTitle,
                        onValueChange = { announcementTitle = it },
                        label = { Text("Duyuru Başlığı") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = announcementMessage,
                        onValueChange = { announcementMessage = it },
                        label = { Text("Duyuru İçeriği / Mesaj") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Button(
                        onClick = { announcementImagePicker() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (announcementImageUrl.isBlank()) "Galeriden Görsel Seç (1080x540)" else "Görseli Değiştir")
                    }

                    if (announcementImageUrl.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            AppAsyncImage(
                                model = announcementImageUrl,
                                contentDescription = "Seçilen Görsel",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Text(
                        text = "İpucu: Görsel ölçüsü en ideal 1080x540 piksel olmalıdır.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (announcementTitle.isNotBlank() && announcementMessage.isNotBlank()) {
                            isSendingAnnouncement = true
                            adminViewModel.sendAnnouncement(
                                title = announcementTitle,
                                message = announcementMessage,
                                imageUrl = announcementImageUrl
                            ) { success, _ ->
                                isSendingAnnouncement = false
                                if (success) {
                                    announcementTitle = ""
                                    announcementMessage = ""
                                    announcementImageUrl = ""
                                    showAnnouncementDialog = false
                                }
                            }
                        }
                    },
                    enabled = !isSendingAnnouncement
                ) {
                    if (isSendingAnnouncement) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Yayınla")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showManageAnnouncementsDialog) {
        AlertDialog(
            onDismissRequest = { showManageAnnouncementsDialog = false },
            title = { Text("Aktif Duyuruları Yönet / Sil") },
            text = {
                if (activeAnnouncements.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Yayında aktif duyuru bulunmuyor.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeAnnouncements) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = item.title.ifBlank { "Görsel Duyuru" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (item.message.isNotBlank()) {
                                            Text(text = item.message, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            adminViewModel.deleteAnnouncement(item.id) { success, _ ->
                                                if (success) {
                                                    activeAnnouncements = activeAnnouncements.filter { it.id != item.id }
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageAnnouncementsDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

private fun generateWhatsAppReport(): String {
    val dealers = LocalStore.dealers.values.toList()
    val rewardReqs = LocalStore.rewardRequests.values.toList()

    val sb = StringBuilder()
    sb.append("📊 *ANADOLU GALERICILER SITESI - AYLIK RAPOR*\n\n")
    
    sb.append("💰 *1. GALERICI BORÇ DURUMLARI*\n")
    val debtors = dealers.filter { it.totalDebt > 0.0 }
    if (debtors.isEmpty()) {
        sb.append("• Borçlu galerici bulunmuyor.\n")
    } else {
        debtors.forEach { d ->
            sb.append("• ${d.galleryName}: ${d.totalDebt} TL (Tel: ${d.phone})\n")
        }
    }

    sb.append("\n🏆 *2. DAMGA ÖDÜLÜ TAMAMLAYANLAR VE İBANLAR*\n")
    val completedRewards = if (rewardReqs.isNotEmpty()) {
        rewardReqs
    } else {
        dealers.filter { it.damgaCount >= 6 || it.rewardCycleCount > 0 }.map { d ->
            RewardRequest(
                dealerId = d.id,
                galleryName = d.galleryName,
                iban = d.iban,
                ibanOwnerName = d.ibanOwnerName
            )
        }
    }

    if (completedRewards.isEmpty()) {
        sb.append("• Ödül bekleyen galeri bulunmuyor.\n")
    } else {
        completedRewards.forEach { r ->
            sb.append("• ${r.galleryName}\n  IBAN: ${r.iban}\n  Alıcı: ${r.ibanOwnerName}\n\n")
        }
    }

    return sb.toString()
}

@Composable
fun AdminStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun AdminNavigationCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
