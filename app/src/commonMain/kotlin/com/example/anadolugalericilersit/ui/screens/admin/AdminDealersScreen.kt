package com.example.anadolugalericilersit.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.ui.components.DealerCard
import com.example.anadolugalericilersit.ui.components.DealerStatusBadge
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.AdminViewModel
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.ToastUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDealersScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onNavigateToDealerDetail: (String) -> Unit
) {
    val dealersState by adminViewModel.dealersState.collectAsState()
    val actionState by adminViewModel.actionState.collectAsState()

    var selectedStatus by remember { mutableStateOf<DealerStatus?>(DealerStatus.PENDING) }

    var showRejectDialog by remember { mutableStateOf(false) }
    var targetDealer by remember { mutableStateOf<Dealer?>(null) }
    var rejectionReason by remember { mutableStateOf("") }
    var isSuspendingAction by remember { mutableStateOf(false) }

    LaunchedEffect(selectedStatus) {
        adminViewModel.loadDealers(selectedStatus)
    }

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            ToastUtils.showToast(message = "İşlem başarıyla gerçekleştirildi.")
            adminViewModel.clearActionState()
            showRejectDialog = false
            adminViewModel.loadDealers(selectedStatus)
        }
    }

    if (actionState is Resource.Loading) {
        LoadingDialog(message = "İşlem yapılıyor...")
    }

    // Reason Dialog
    if (showRejectDialog && targetDealer != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text(if (isSuspendingAction) "Galeriyi Askıya Al" else "Galeri Başvurusunu Reddet") },
            text = {
                Column {
                    Text("Lütfen gerekçeyi belirtiniz:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Açıklama / Sebep") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newStatus = if (isSuspendingAction) DealerStatus.SUSPENDED else DealerStatus.REJECTED
                        val d = targetDealer
                        if (d != null) {
                            adminViewModel.updateDealerStatus(d.id, d.ownerUid, newStatus, rejectionReason)
                        }
                    }
                ) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galerici Yönetimi") },
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
        ) {
            // Status Tabs
            val tabs = listOf(
                DealerStatus.PENDING to "Bekleyenler",
                DealerStatus.APPROVED to "Onaylılar",
                DealerStatus.SUSPENDED to "Askıdakiler",
                DealerStatus.REJECTED to "Reddedilenler",
                null to "Tümü"
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { (status, label) ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(label) }
                    )
                }
            }

            when (dealersState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = dealersState.message ?: "Hata oluştu", color = MaterialTheme.colorScheme.error)
                    }
                }

                is Resource.Success -> {
                    val list = dealersState.data ?: emptyList()
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Bu filtrede galerici kaydı bulunmuyor.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { dealer: Dealer ->
                                Column {
                                    DealerCard(
                                        dealer = dealer,
                                        onClick = { onNavigateToDealerDetail(dealer.id) }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (dealer.accountStatus == DealerStatus.PENDING) {
                                            Button(
                                                onClick = {
                                                    adminViewModel.updateDealerStatus(dealer.id, dealer.ownerUid, DealerStatus.APPROVED)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                                            ) {
                                                Text("Onayla")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    targetDealer = dealer
                                                    isSuspendingAction = false
                                                    rejectionReason = ""
                                                    showRejectDialog = true
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Reddet")
                                            }
                                        } else if (dealer.accountStatus == DealerStatus.APPROVED) {
                                            OutlinedButton(
                                                onClick = {
                                                    targetDealer = dealer
                                                    isSuspendingAction = true
                                                    rejectionReason = ""
                                                    showRejectDialog = true
                                                }
                                            ) {
                                                Text("Askıya Al")
                                            }
                                        } else if (dealer.accountStatus == DealerStatus.SUSPENDED || dealer.accountStatus == DealerStatus.REJECTED) {
                                            Button(
                                                onClick = {
                                                    adminViewModel.updateDealerStatus(dealer.id, dealer.ownerUid, DealerStatus.APPROVED)
                                                }
                                            ) {
                                                Text("Tekrar Aktif Et")
                                            }
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
}
