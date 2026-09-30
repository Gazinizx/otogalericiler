package com.example.anadolugalericilersit.ui.screens.dealer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import com.example.anadolugalericilersit.data.local.TransactionType
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.utils.IntentUtils
import com.example.anadolugalericilersit.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerDebtScreen(
    dealer: Dealer?,
    onBackClick: () -> Unit
) {
    val currentDealer = remember(dealer) {
        if (dealer != null) {
            LocalStore.dealers[dealer.id] ?: dealer
        } else {
            null
        }
    }

    val transactions = remember(currentDealer) {
        if (currentDealer != null) {
            LocalStore.debtTransactions[currentDealer.id] ?: emptyList()
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Borç ve Cari Durum") },
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
            if (currentDealer == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Galerici profili bulunamadı.")
                }
            } else {
                // Debt Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentDealer.totalDebt > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = if (currentDealer.totalDebt > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Güncel Borç Tutarınız",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (currentDealer.totalDebt > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${currentDealer.totalDebt.toInt()} ₺",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentDealer.totalDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // WhatsApp Payment Button to 05054543099
                        Button(
                            onClick = {
                                val galleryName = currentDealer.galleryName
                                val debt = currentDealer.totalDebt.toInt()
                                val message = "Merhaba, $galleryName galeri hesabımdan borç ödemesi yapmak istiyorum. Güncel Borç Tutarım: $debt TL"
                                IntentUtils.openWhatsApp(phone = "05054543099", message = message)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Borç Ödemesi Yap (WhatsApp - 05054543099)", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Yapılan İşlemler ve Borç Geçmişi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz borç veya işlem kaydınız bulunmuyor.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(transactions) { tx ->
                            val isPayment = tx.type == TransactionType.PAYMENT
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPayment) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isPayment) "[ÖDENDİ] ${tx.description}" else "[BORÇ] ${tx.description}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isPayment) "-${tx.amount.toInt()} ₺" else "+${tx.amount.toInt()} ₺",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tarih & Saat: ${formatDate(tx.timestamp)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
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
