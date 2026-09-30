package com.example.anadolugalericilersit.ui.screens.profile

import com.example.anadolugalericilersit.utils.ToastUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings

import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.data.model.User
import com.example.anadolugalericilersit.ui.components.DealerStatusBadge
import com.example.anadolugalericilersit.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    user: User?,
    dealer: Dealer?,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToDealerRewards: () -> Unit,
    onNavigateToDealerDebt: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hesabımı Sil") },
            text = { Text("Hesabınızı ve galeri verilerinizi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteAccount { error ->
                            if (error != null) {
                                ToastUtils.showToast(message = error)
                            } else {
                                ToastUtils.showToast(message = "Hesabınız silindi")
                                onNavigateToLogin()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim & Hesap Ayarları") }
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
            if (user == null) {
                // Unauthenticated visitor profile card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hoş Geldiniz",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Galerici hesabınızla giriş yaparak araç ilanı verebilir ve yönetebilirsiniz.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
                            Text("Giriş Yap / Kaydol")
                        }
                    }
                }
            } else {
                // Dealer / Admin Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dealer?.logoUrl.isNull_orBlank().not()) {
                                AsyncImage(
                                    model = dealer?.logoUrl,
                                    contentDescription = dealer?.galleryName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = dealer?.galleryName ?: user.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = user.email,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            if (dealer != null) {
                                DealerStatusBadge(status = dealer.accountStatus)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Admin Panel Option if user has ADMIN or SUPER_ADMIN role
                val showAdminButton = user?.role == Role.ADMIN ||
                        user?.role == Role.SUPER_ADMIN ||
                        user?.canIssueDamga == true ||
                        user?.email == "europexpert38@gmail.com" ||
                        user?.email == "gazitasdemir46@gmail.com" ||
                        user?.uid?.startsWith("admin") == true

                if (showAdminButton) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdminDashboard() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Yönetici (Admin) Paneli",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = "Galericileri ve ilanları yönet, onay/red işlemlerini yap.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Profile Menu Options
                Text(
                    text = "Hesap İşlemleri",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ProfileMenuItem(
                    icon = Icons.Default.Edit,
                    title = "Galeri Profilini Düzenle",
                    onClick = onNavigateToEditProfile
                )

                ProfileMenuItem(
                    icon = Icons.Default.Verified,
                    title = "Damgalarım & Ödüllerim",
                    onClick = onNavigateToDealerRewards
                )

                if (dealer != null) {
                    ProfileMenuItem(
                        icon = Icons.Default.Store,
                        title = "Borç & Cari Durumum",
                        onClick = onNavigateToDealerDebt
                    )
                }

                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimlerim",
                    onClick = onNavigateToNotifications
                )

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Uygulama Ayarları",
                    onClick = onNavigateToSettings
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Güvenlik & Gizlilik",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "KVKK ve Gizlilik Politikası",
                    onClick = {
                        ToastUtils.showToast(message = "KVKK ve Gizlilik Politikası metni günceldir.")
                    }
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Account Danger Zone
                ProfileMenuItem(
                    icon = Icons.Default.Delete,
                    title = "Hesabımı Sil",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteConfirmDialog = true }
                )

                ProfileMenuItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Çıkış Yap",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        authViewModel.logout()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontSize = 15.sp, color = textColor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

private fun String?.isNull_orBlank(): Boolean = this == null || this.isBlank()
