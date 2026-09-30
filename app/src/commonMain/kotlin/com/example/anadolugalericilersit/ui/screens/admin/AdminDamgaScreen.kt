package com.example.anadolugalericilersit.ui.screens.admin

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.StampCodeRequest
import com.example.anadolugalericilersit.data.model.DamgaStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.utils.CameraQrScannerDialog
import com.example.anadolugalericilersit.utils.IntentUtils
import com.example.anadolugalericilersit.utils.QRCodeUtils
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDamgaScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) } // 0: QR Kod Taraması ile Damga Bas, 1: Bekleyen Damga Talepleri, 2: 10 Damga Ödül Yönetimi

    var showCameraScanner by remember { mutableStateOf(false) }
    var lastScannedQrResult by remember { mutableStateOf<String?>(null) }
    var verifiedStampRequest by remember { mutableStateOf<StampCodeRequest?>(null) }

    val currentLoggedInUid = LocalStore.currentLoggedInUid
    val currentUser = currentLoggedInUid?.let { LocalStore.users[it] }
    val canIssue = currentUser?.role == Role.ADMIN || currentUser?.role == Role.SUPER_ADMIN || currentUser?.canIssueDamga == true

    val pendingDamgaVehicles = remember(refreshTrigger) {
        LocalStore.vehicles.values.filter { it.damgaStatus == DamgaStatus.PENDING && it.expertInspectionCost >= 5000.0 }
    }

    if (showCameraScanner) {
        CameraQrScannerDialog(
            onQrCodeScanned = { scannedResult ->
                lastScannedQrResult = scannedResult
                var req = LocalStore.stampCodes[scannedResult]
                    ?: LocalStore.stampCodes.values.firstOrNull { it.code == scannedResult }

                if (req == null) {
                    // Extract dealerId from format AGS-DAMGA:dealerId:code if present
                    val parts = scannedResult.split(":")
                    val dealerId = if (parts.size >= 2) parts[1] else "dealer_01"
                    req = StampCodeRequest(
                        code = scannedResult,
                        dealerId = dealerId,
                        galleryName = LocalStore.dealers[dealerId]?.galleryName ?: "Galeri",
                        vehicleId = "general_vehicle",
                        vehicleTitle = "QR Damga Talebi"
                    )
                    LocalStore.stampCodes[scannedResult] = req
                }
                verifiedStampRequest = req
                ToastUtils.showToast(message = "QR Kod Başarıyla Tarandı!")
            },
            onDismissRequest = {
                showCameraScanner = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Damga ve QR Ödül Yönetimi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("QR Kod ile Onay") }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Damga Talepleri") }
                )
                FilterChip(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("6 Damga Ödülü Onayla") }
                )

            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Tab 0: Sadece QR Kod ile Onay
                Text(
                    text = "QR Kod Taraması ile Damga Basma",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Damga basma işlemi SADECE galericinin telefonundaki QR kodunun taranması ile onaylanır.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!canIssue) {
                            ToastUtils.showToast(message = "Damga basmak için yönetici yetkiniz bulunmuyor.")
                        } else {
                            showCameraScanner = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kamerayı Aç ve QR Kod Tarat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (verifiedStampRequest != null) {
                    val req = verifiedStampRequest!!

                    Spacer(modifier = Modifier.height(20.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Taranan QR Kod: ${req.code.takeLast(16)}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Galeri Adı: ${req.galleryName}", fontSize = 14.sp)
                            Text(
                                text = if (req.isUsed) "Durum: Bu QR kod DAHA ÖNCE KULLANILMIŞ!" else "Durum: Kullanıma Uygun (Bekliyor)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (req.isUsed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (req.isUsed) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "UYARI: Bu QR kod ile daha önce damga basılmıştır!",
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (!canIssue) {
                                            ToastUtils.showToast(message = "Yetkiniz bulunmuyor.")
                                        } else {
                                            // Find dealer's vehicles and approve damga
                                            val dealerVehiclesToStamp = LocalStore.vehicles.values.filter { it.dealerId == req.dealerId && !it.hasDamga }
                                            if (dealerVehiclesToStamp.isNotEmpty()) {
                                                for (v in dealerVehiclesToStamp) {
                                                    LocalStore.vehicles[v.id] = v.copy(
                                                        damgaStatus = DamgaStatus.APPROVED,
                                                        hasDamga = true
                                                    )
                                                }
                                            } else {
                                                val allDealerVehicles = LocalStore.vehicles.values.filter { it.dealerId == req.dealerId }
                                                for (v in allDealerVehicles) {
                                                    LocalStore.vehicles[v.id] = v.copy(
                                                        damgaStatus = DamgaStatus.APPROVED,
                                                        hasDamga = true
                                                    )
                                                }
                                            }
                                            req.isUsed = true
                                            ToastUtils.showToast(message = "QR Kod doğrulandı ve damga başarıyla basıldı!")
                                            refreshTrigger++
                                            verifiedStampRequest = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("QR Kodu Onayla ve Damga Bas")
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Damga Talepleri Listesi
                Text(
                    text = "5.000 TL Üzeri Ekspertizli Araçlar (Damga Talepleri)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (canIssue) "Damga basma yetkiniz aktif." else "Not: Yönetici izni gereklidir.",
                    fontSize = 12.sp,
                    color = if (canIssue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (pendingDamgaVehicles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Onay bekleyen Damga başvurusu bulunmuyor.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pendingDamgaVehicles) { vehicle ->
                            val dealer = LocalStore.dealers[vehicle.dealerId]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Plaka: ${vehicle.licensePlate.takeIf { !it.isBlank() } ?: "Belirtilmemiş"}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                    Text(text = "Galeri: ${vehicle.dealerName}", fontSize = 13.sp)
                                    Text(text = "Ekspertiz Ücreti: ${vehicle.expertInspectionCost.toInt()} ₺", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(text = "IBAN: ${dealer?.iban.takeIf { !it.isNullOrBlank() } ?: "Belirtilmemiş"}", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                if (!canIssue) {
                                                    ToastUtils.showToast(message = "Damga basmak için yönetici izniniz bulunmuyor.")
                                                } else {
                                                    val updatedV = vehicle.copy(
                                                        damgaStatus = DamgaStatus.APPROVED,
                                                        hasDamga = true
                                                    )
                                                    LocalStore.vehicles[vehicle.id] = updatedV
                                                    try {
                                                        FirebaseFirestore.getInstance()
                                                            .collection("vehicles").document(vehicle.id)
                                                            .update(mapOf("damgaStatus" to "APPROVED", "hasDamga" to true))
                                                    } catch (e: Exception) {
                                                        // ignore offline
                                                    }
                                                    ToastUtils.showToast(message = "Damga başarıyla onaylandı!")
                                                    refreshTrigger++
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Damga Bas")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (!canIssue) {
                                                    ToastUtils.showToast(message = "Yetkiniz bulunmuyor.")
                                                } else {
                                                    val updatedV = vehicle.copy(
                                                        damgaStatus = DamgaStatus.REJECTED,
                                                        hasDamga = false
                                                    )
                                                    LocalStore.vehicles[vehicle.id] = updatedV
                                                    try {
                                                        FirebaseFirestore.getInstance()
                                                            .collection("vehicles").document(vehicle.id)
                                                            .update(mapOf("damgaStatus" to "REJECTED", "hasDamga" to false))
                                                    } catch (e: Exception) {
                                                        // ignore offline
                                                    }
                                                    ToastUtils.showToast(message = "Damga reddedildi.")
                                                    refreshTrigger++
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reddet")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 2: 6 Damga Ödülü ve WhatsApp İşlemleri
                Text(
                    text = "6 Damga Ödül Yönetimi & WhatsApp İletişim",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "6 damgayı tamamlayan galericilerin IBAN ve galeri bilgileri saat 08:00 - 18:00 arasında 05054543099 numaralı WhatsApp hattına iletilmektedir.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                val rewardEligibleDealers = remember(refreshTrigger) {
                    LocalStore.dealers.values.filter { dealer ->
                        LocalStore.vehicles.values.count { it.dealerId == dealer.id && it.hasDamga } >= 6
                    }
                }

                if (rewardEligibleDealers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("6 Damga Tamamlayan Galerici Listesi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sistem kayıtlarında henüz 6 damgayı tamamlamış galerici bulunmuyor. Galerici 6 damgayı tamamladığında IBAN bilgisi WhatsApp hattına iletilebilir.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rewardEligibleDealers) { dealer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Galeri Adı: ${dealer.galleryName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Yetkili: ${dealer.authorizedName}", fontSize = 13.sp)
                                    Text("IBAN: ${dealer.iban}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("IBAN Sahibi: ${dealer.ibanOwnerName}", fontSize = 13.sp)

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            QRCodeUtils.sendIbanToWhatsApp(context, dealer)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("IBAN Bilgisini WhatsApp'a Gönder (08:00-18:00)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
