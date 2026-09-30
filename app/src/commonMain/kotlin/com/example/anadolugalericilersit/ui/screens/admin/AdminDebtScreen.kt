package com.example.anadolugalericilersit.ui.screens.admin

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.DebtTransaction
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.TransactionType
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.utils.NotificationUtils
import com.example.anadolugalericilersit.utils.currentTimeMillis
import com.example.anadolugalericilersit.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDebtScreen(
    onBackClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val dealersList = remember(refreshTrigger) {
        LocalStore.dealers.values.toList()
    }

    val filteredDealers = remember(dealersList, searchQuery) {
        if (searchQuery.isBlank()) {
            dealersList
        } else {
            val q = searchQuery.trim().lowercase()
            dealersList.sortedByDescending { dealer ->
                dealer.galleryName.lowercase().contains(q) ||
                        dealer.authorizedName.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galeri Borç / Ödeme Yönetimi", fontWeight = FontWeight.Bold) },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Galeri Adı veya Yetkili Ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredDealers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Sistemde kayıtlı galeri bulunmuyor." else "Arama sonucu galeri bulunamadı.",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredDealers, key = { it.id }) { dealer ->
                        AdminDealerDebtItem(
                            dealer = dealer,
                            onTransactionRecorded = {
                                refreshTrigger++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDealerDebtItem(
    dealer: Dealer,
    onTransactionRecorded: () -> Unit
) {
    var showForm by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(TransactionType.DEBT) }
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    val transactions = remember(dealer.id, LocalStore.debtTransactions[dealer.id]) {
        LocalStore.debtTransactions[dealer.id] ?: mutableListOf()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dealer.totalDebt > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(dealer.galleryName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Yetkili: ${dealer.authorizedName} • ${dealer.phone}", fontSize = 12.sp, color = Color.Gray)
                }

                Surface(
                    color = if (dealer.totalDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF166534),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (dealer.totalDebt > 0) "Borç: ${dealer.totalDebt.toInt()} ₺" else "Borcu Yok",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showForm = !showForm
                        if (showForm) showHistory = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showForm) "Kapat" else "İşlem Ekle", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        showHistory = !showHistory
                        if (showHistory) showForm = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showHistory) "Kapat" else "Geçmiş (${transactions.size})", fontSize = 12.sp)
                }
            }

            AnimatedVisibility(visible = showForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("İşlem Tipi Seçin:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == TransactionType.DEBT,
                            onClick = { selectedType = TransactionType.DEBT },
                            label = { Text("Borç Ekle (+)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == TransactionType.PAYMENT,
                            onClick = { selectedType = TransactionType.PAYMENT },
                            label = { Text("Ödendi / Ödeme Alındı (-)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Tutar (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Açıklama / Not (Örn: Yıllık Aidat, Havale)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val amount = amountInput.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                ToastUtils.showToast(message = "Lütfen geçerli bir tutar girin!")
                                return@Button
                            }

                            val now = currentTimeMillis()
                            val tx = DebtTransaction(
                                dealerId = dealer.id,
                                amount = amount,
                                description = descriptionInput,
                                type = selectedType,
                                timestamp = now
                            )

                            val txList = LocalStore.debtTransactions.getOrPut(dealer.id) { mutableListOf() }
                            txList.add(0, tx)

                            val newTotalDebt = if (selectedType == TransactionType.DEBT) {
                                dealer.totalDebt + amount
                            } else {
                                (dealer.totalDebt - amount).coerceAtLeast(0.0)
                            }

                            val updatedDealer = dealer.copy(totalDebt = newTotalDebt)
                            LocalStore.dealers[dealer.id] = updatedDealer

                            NotificationUtils.sendDebtOrPaymentNotification(
                                dealer = updatedDealer,
                                type = selectedType.name,
                                amount = amount
                            )

                            amountInput = ""
                            descriptionInput = ""
                            showForm = false
                            ToastUtils.showToast(message = "İşlem başarıyla kaydedildi!")
                            onTransactionRecorded()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.DEBT) MaterialTheme.colorScheme.error else Color(0xFF166534)
                        )
                    ) {
                        Text("İşlemi Kaydet")
                    }
                }
            }

            AnimatedVisibility(visible = showHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("İşlem Geçmişi:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (transactions.isEmpty()) {
                        Text("Henüz kaydedilmiş bir borç/ödeme hareketi yok.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactions.forEach { tx ->
                                val isPayment = tx.type == TransactionType.PAYMENT
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (isPayment) "ÖDEME ALINDI (-)" else "BORÇ EKLENDİ (+)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.error
                                            )
                                            Text(
                                                text = if (isPayment) "-${tx.amount.toInt()} ₺" else "+${tx.amount.toInt()} ₺",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.error
                                            )
                                        }
                                        if (tx.description.isNotBlank()) {
                                            Text("Not: ${tx.description}", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Tarih & Saat: ${formatDate(tx.timestamp)}", fontSize = 11.sp, color = Color.Gray)
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
