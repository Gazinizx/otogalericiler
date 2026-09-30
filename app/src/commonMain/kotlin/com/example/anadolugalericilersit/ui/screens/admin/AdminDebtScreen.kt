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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.DebtTransaction
import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.local.TransactionType
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.utils.NotificationUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDebtScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    val dealersList = remember(refreshTrigger) {
        LocalStore.dealers.values.toList()
    }

    val filteredDealers = remember(dealersList, searchQuery) {
        if (searchQuery.isBlank()) {
            dealersList
        } else {
            val q = searchQuery.trim().lowercase(Locale.getDefault())
            dealersList.sortedByDescending { dealer ->
                dealer.galleryName.lowercase(Locale.getDefault()).contains(q) ||
                        dealer.authorizedName.lowercase(Locale.getDefault()).contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galerici Borç & Cari Takip") },
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Galeri Adı veya Yetkili İle Ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredDealers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Kayıtlı galerici bulunamadı.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredDealers, key = { it.id }) { dealer ->
                        AdminDealerDebtCard(
                            dealer = dealer,
                            onTransactionAdded = {
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
fun AdminDealerDebtCard(
    dealer: Dealer,
    onTransactionAdded: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(TransactionType.DEBT) } // DEBT or PAYMENT
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }

    val transactions = remember(dealer.id, LocalStore.debtTransactions[dealer.id]) {
        LocalStore.debtTransactions[dealer.id] ?: mutableListOf()
    }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

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
                    Text(text = dealer.galleryName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Yetkili: ${dealer.authorizedName} (${dealer.phone})", fontSize = 12.sp, color = Color.Gray)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (dealer.totalDebt > 0) MaterialTheme.colorScheme.error else Color(0xFF15803D)
                ) {
                    Text(
                        text = "Borç: ${dealer.totalDebt.toInt()} ₺",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(if (isExpanded) Icons.Default.History else Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isExpanded) "İşlem Formunu Gizle" else "Borç / Ödeme Ekle & Geçmişi Gör")
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Yeni İşlem Türünü Seçiniz:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Transaction Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == TransactionType.DEBT,
                            onClick = { selectedType = TransactionType.DEBT },
                            label = { Text("➕ Borç Ekle (+)") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = selectedType == TransactionType.PAYMENT,
                            onClick = { selectedType = TransactionType.PAYMENT },
                            label = { Text("➖ Ödendi / Borçtan Düş (-)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text(if (selectedType == TransactionType.DEBT) "Eklenen Borç Tutarı (₺)" else "Ödenen Tutar (Borçtan Düşülecek ₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Açıklama / İşlem Detayı (Örn: Banka Havalesi / Ekspertiz Bedeli)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val amount = amountInput.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                ToastUtils.showToast(message = "Lütfen geçerli bir tutar giriniz!")
                                return@Button
                            }
                            if (descriptionInput.isBlank()) {
                                ToastUtils.showToast(message = "Lütfen yapılan işlemi / açıklamayı yazınız!")
                                return@Button
                            }

                            val now = System.currentTimeMillis()
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

                            try {
                                FirebaseFirestore.getInstance()
                                    .collection("dealers").document(dealer.id)
                                    .update("totalDebt", updatedDealer.totalDebt)
                            } catch (e: Exception) {
                                // ignore offline
                            }

                            // Send Status Bar & In-App Notification
                            NotificationUtils.sendDebtOrPaymentNotification(
                                context,
                                updatedDealer,
                                selectedType.name,
                                amount
                            )

                            amountInput = ""
                            descriptionInput = ""
                            val msg = if (selectedType == TransactionType.DEBT) {
                                "Borç kaydı eklendi (+${amount.toInt()} ₺)"
                            } else {
                                "Ödeme kaydedildi (-${amount.toInt()} ₺). Galerinin borcundan düşüldü!"
                            }
                            ToastUtils.showToast(message = msg)
                            onTransactionAdded()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == TransactionType.DEBT) MaterialTheme.colorScheme.primary else Color(0xFF15803D)
                        )
                    ) {
                        Text(if (selectedType == TransactionType.DEBT) "Borcu Kaydet (+)" else "Ödemeyi Kaydet ve Borçtan Düş (-)")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("İşlem Geçmişi (${transactions.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (transactions.isEmpty()) {
                        Text("Henüz işlem kaydı bulunmuyor.", fontSize = 12.sp, color = Color.Gray)
                    } else {
                        transactions.forEach { tx ->
                            val isPayment = tx.type == TransactionType.PAYMENT
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPayment) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isPayment) "[ÖDENDİ] ${tx.description}" else "[BORÇ] ${tx.description}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isPayment) "-${tx.amount.toInt()} ₺" else "+${tx.amount.toInt()} ₺",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isPayment) Color(0xFF166534) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Tarih & Saat: ${dateFormat.format(Date(tx.timestamp))}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
