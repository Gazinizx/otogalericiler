package com.example.anadolugalericilersit.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.utils.ClipboardUtils
import com.example.anadolugalericilersit.utils.SecurityRules
import com.example.anadolugalericilersit.utils.ToastUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var showFirestoreRulesDialog by remember { mutableStateOf(false) }

    if (showFirestoreRulesDialog) {
        AlertDialog(
            onDismissRequest = { showFirestoreRulesDialog = false },
            title = { Text("Firestore Security Rules") },
            text = {
                Box(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = SecurityRules.FIRESTORE_RULES,
                        color = Color(0xFF38BDF8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    ClipboardUtils.copyToClipboard(label = "Firestore Rules", text = SecurityRules.FIRESTORE_RULES)
                    ToastUtils.showToast(message = "Kurallar panoya kopyalandı")
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kopyala")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFirestoreRulesDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uygulama Ayarları") },
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Push Bildirimleri", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = pushNotificationsEnabled,
                    onCheckedChange = { pushNotificationsEnabled = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = { showFirestoreRulesDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Firebase Security Rules Gör")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Sürüm: 1.0.0 (Compose Multiplatform iOS & Android)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Anadolu Oto Galericiler Sitesi Mobil Uygulaması", fontSize = 12.sp)
                }
            }
        }
    }
}
