package com.example.anadolugalericilersit.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.AdminViewModel
import com.example.anadolugalericilersit.utils.Resource
import com.example.anadolugalericilersit.utils.ToastUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val reportsState by adminViewModel.reportsState.collectAsState()
    val actionState by adminViewModel.actionState.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.loadReports()
    }

    LaunchedEffect(actionState) {
        if (actionState is Resource.Success) {
            ToastUtils.showToast(message = "Şikayet çözüldü olarak işaretlendi.")
            adminViewModel.clearActionState()
            adminViewModel.loadReports()
        }
    }

    if (actionState is Resource.Loading) {
        LoadingDialog(message = "Güncelleniyor...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İlan Şikayetleri") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        when (reportsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = reportsState.message ?: "Şikayetler yüklenemedi", color = MaterialTheme.colorScheme.error)
                }
            }

            is Resource.Success -> {
                val list = reportsState.data ?: emptyList()
                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("Henüz bildirilmiş şikayet bulunmuyor.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(list) { report: Report ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (report.isResolved) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sebep: ${report.reason}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )

                                        val formattedDate = try {
                                            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(report.createdAt))
                                        } catch (e: Exception) {
                                            ""
                                        }
                                        Text(text = formattedDate, fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "İlan: ${report.vehicleTitle}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { onNavigateToVehicleDetail(report.vehicleId) }
                                    )

                                    if (report.note.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Not: ${report.note}", fontSize = 13.sp)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Bildiren: ${report.reporterEmail}", fontSize = 12.sp, color = Color.Gray)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        if (!report.isResolved) {
                                            Button(
                                                onClick = { adminViewModel.resolveReport(report.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Çözüldü İşaretle")
                                            }
                                        } else {
                                            Text("Çözüldü ✓", color = Color(0xFF15803D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
