package com.example.anadolugalericilersit.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.ui.viewmodel.AdminViewModel
import com.example.anadolugalericilersit.utils.Resource

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

    LaunchedEffect(Unit) {
        adminViewModel.loadStats()
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
    onClick: () -> Unit
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
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
