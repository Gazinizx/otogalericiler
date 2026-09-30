package com.example.anadolugalericilersit.ui.screens.dealer

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.StampCodeRequest
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.utils.QRCodeUtils
import com.example.anadolugalericilersit.utils.generateUuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerRewardsScreen(
    dealer: Dealer?,
    onBackClick: () -> Unit
) {
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Damgalarım & Ödüller, 1: Damga QR Kodu Üret

    val dealerVehicles = remember(dealer, refreshTrigger) {
        if (dealer != null) {
            LocalStore.vehicles.values.filter { it.dealerId == dealer.id && it.hasDamga }
        } else {
            emptyList()
        }
    }

    val dealerStampCodes = remember(dealer, refreshTrigger) {
        if (dealer != null) {
            LocalStore.stampCodes.values.filter { it.dealerId == dealer.id }
        } else {
            emptyList()
        }
    }

    val damgaCount = dealerVehicles.size
    val targetDamga = 6
    val progress = (damgaCount.toFloat() / targetDamga.toFloat()).coerceIn(0f, 1f)

    var currentQrCodeData by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Damgalarım ve QR Ödül Yönetimi") },
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
                    label = { Text("Damgalarım & 6 Damga Ödülü") }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Damga QR Kodu Üret") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "6 Damga Ödül İlerlemesi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = "$damgaCount / $targetDamga Damgalı Araç",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (damgaCount >= targetDamga) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tebrikler! 6 Damga Hedefini Tamamladınız 🎉",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "IBAN bilgilerinizi 08:00 - 18:00 saatleri arasında tek tıkla WhatsApp yönetici hattına (05054543099) iletebilirsiniz.",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (dealer != null) {
                                                QRCodeUtils.sendIbanToWhatsApp(dealer = dealer)
                                            } else {
                                                ToastUtils.showToast(message = "Galerici bilgisi yüklenemedi")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("IBAN Bilgisini WhatsApp'a Gönder (08:00-18:00)")
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "6 damga ödülünü kazanmak için 5.000 TL üzeri ekspertizli ve onaylı ${targetDamga - damgaCount} damgalı araç daha eklemelisiniz.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Damga Alınan Araçlar ve Plakaları",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dealerVehicles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Henüz onaylanmış damgalı aracınız bulunmuyor.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dealerVehicles) { vehicle ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${vehicle.brand} ${vehicle.model} (${vehicle.year})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "Plaka: ${vehicle.licensePlate.takeIf { !it.isBlank() } ?: "Belirtilmemiş"}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                        Text(text = "Ekspertiz: ${vehicle.expertInspectionCost.toInt()} ₺", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: Damga QR Kodu Üret
                Text(
                    text = "Damga İçin QR Kod Oluştur",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Aşağıdaki butona basarak damga basılacak aracınız için QR Kod oluşturunuz. Damga onayı SADECE yöneticinin bu QR kodu taratması ile gerçekleşir.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val randomCode = "AGS-DAMGA-${generateUuid().take(8).uppercase()}"
                        val payload = "AGS-DAMGA:${dealer?.id ?: "dealer_01"}:$randomCode"

                        val req = StampCodeRequest(
                            code = payload,
                            dealerId = dealer?.id ?: "dealer_01",
                            galleryName = dealer?.galleryName ?: "Galeri",
                            vehicleId = "general_vehicle",
                            vehicleTitle = "QR Damga Talebi"
                        )
                        LocalStore.stampCodes[payload] = req
                        currentQrCodeData = payload
                        refreshTrigger++
                        ToastUtils.showToast(message = "QR Kod oluşturuldu!")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("QR Kod Oluştur")
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!currentQrCodeData.isNullOrBlank()) {
                    val qrBitmap = remember(currentQrCodeData) {
                        QRCodeUtils.generateQrCodeBitmap(currentQrCodeData!!, 500)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Damga Onay QR Kodunuz",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bu QR kodu yöneticinin telefon kamerasından taratınız. Yönetici tarattığı anda damga onaylanacaktır.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (qrBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap,
                                        contentDescription = "Damga QR Kodu",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Kod: ${currentQrCodeData?.takeLast(12)}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = "Geçmiş QR Damga Kodlarınız",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (dealerStampCodes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Henüz QR damga kodu oluşturulmamış.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dealerStampCodes) { sc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (sc.isUsed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Damga QR Kod ID: ${sc.code.takeLast(12)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = if (sc.isUsed) "Durum: Kullanıldı (Damga Onaylandı ✅)" else "Durum: Bekliyor (Yönetici Taraması Bekleniyor ⏳)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (sc.isUsed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
