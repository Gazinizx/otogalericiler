package com.example.anadolugalericilersit.ui.screens.auth

import com.example.anadolugalericilersit.utils.ToastUtils
import com.example.anadolugalericilersit.utils.rememberImagePickerLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.ui.components.AppAsyncImage
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.AuthViewModel
import com.example.anadolugalericilersit.utils.Constants
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerRegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {

    var galleryName by remember { mutableStateOf("") }
    var authorizedName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Kayseri") }
    var district by remember { mutableStateOf("Kocasinan") }
    var address by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var iban by remember { mutableStateOf("") }
    var ibanOwnerName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Any?>(null) }
    var kvkkAccepted by remember { mutableStateOf(false) }

    var isCityExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePickerLauncher { uri ->
        if (uri != null) {
            logoUri = uri
        }
    }

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is Resource.Success) {
            ToastUtils.showToast(message = "Başvurunuz alındı! Admin onayı bekleniyor.")
            authViewModel.clearRegisterState()
            onRegisterSuccess()
        }
    }

    if (registerState is Resource.Loading) {
        LoadingDialog(message = "Galerici hesabı oluşturuluyor ve belgeler yükleniyor...")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galerici Kayıt Formu") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { imagePicker() },
                contentAlignment = Alignment.Center
            ) {
                if (logoUri != null) {
                    AppAsyncImage(
                        model = logoUri,
                        contentDescription = "Galeri Logosu",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Logo Ekle",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = galleryName,
                onValueChange = { galleryName = it },
                label = { Text("Galeri Adı *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = authorizedName,
                onValueChange = { authorizedName = it },
                label = { Text("Galeri Yetkilisi Ad Soyad *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefon Numarası *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta Adresi *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre *") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // City Dropdown
            ExposedDropdownMenuBox(
                expanded = isCityExpanded,
                onExpandedChange = { isCityExpanded = !isCityExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("İl *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = isCityExpanded,
                    onDismissRequest = { isCityExpanded = false }
                ) {
                    Constants.CITIES.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = {
                                city = c
                                isCityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = district,
                onValueChange = { district = it },
                label = { Text("İlçe") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Açık Adres") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = taxNumber,
                onValueChange = { taxNumber = it },
                label = { Text("Vergi Numarası *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = iban,
                onValueChange = { iban = it },
                label = { Text("Banka IBAN Numarası *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = ibanOwnerName,
                onValueChange = { ibanOwnerName = it },
                label = { Text("IBAN Sahibinin Adı Soyadı (Kimlikteki Gibi) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Galeri Hakkında Açıklama") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Registration & Preliminary Information Contract Checkbox
            var showContractDialog by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (kvkkAccepted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { kvkkAccepted = !kvkkAccepted }
                    ) {
                        Checkbox(
                            checked = kvkkAccepted,
                            onCheckedChange = { kvkkAccepted = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kayıt ve Üyelik Ön Bilgilendirme Sözleşmesi'ni okudum, kabul ediyorum. *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(
                        onClick = { showContractDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "📄 Sözleşme Metnini Oku",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Contract Dialog
            if (showContractDialog) {
                AlertDialog(
                    onDismissRequest = { showContractDialog = false },
                    title = {
                        Text(
                            text = "Kayıt ve Üyelik Ön Bilgilendirme Sözleşmesi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = """
                                ANADOLU OTOMOTİV GALERİCİLER SİTESİ
                                GALERİCİ ÜYELİK VE ÖN BİLGİLENDİRME SÖZLEŞMESİ

                                1. TARAFLAR VE AMAÇ:
                                İşbu sözleşme Anadolu Otomotiv Galericiler Sitesi dijital platformu ile platforma kaydolan yetkili oto galeri işletmesi (ÜYE GALERİ) arasında akdedilmiştir.

                                2. ÜYELİK VE İLAN ŞARTLARI:
                                - Üye galeri, beyan ettiği vergi numarası, yetkili adı, telefon ve IBAN bilgilerinin eksiksiz ve doğru olduğunu kabul ve taahhüt eder.
                                - Sistemde yayınlanan araç ilanlarının, ekspertiz bilgilerinin, kilometre ve araç durumunun hukuki, mali ve cezai tüm sorumluluğu doğrudan ilanı oluşturan üyeye aittir.
                                - Üyelik başvurusu Admin (Yönetici) onayından geçtikten sonra aktifleşir.

                                3. HUKUKİ VE MADDİ SORUMLULUK REDDİ (YASAL MUAFİYET MADDESİ):
                                - Anadolu Otomotiv Galericiler Sitesi platformu, platform geliştiricileri, sistem yöneticileri ve hak sahipleri (Gazi Taşdemir, Dennis İçtem ve platform yetkilileri), alıcılar, satıcılar, galericiler veya üçüncü şahıslar arasında gerçekleşecek hiçbir ticari uyuşmazlığın, alım-satım işleminin, ekspertiz beyanının, ayıplı mal iddiasının veya hukuki/cezai davanın TARAFI VEYA MUHATABI DEĞİLDİR.
                                - Platform yalnızca yer sağlayıcı ilan hizmeti sunmaktadır. Olası her türlü adli, idari, icrai veya hukuki süreçte platform sahipleri hiçbir şekilde sorumlu tutulamaz, davaya dahil edilemez ve taraf gösterilemez. Tüm hukuki, idari ve mali sorumluluk münhasıran ilanı veren galericiye ve alıcıya aittir.

                                4. 6 DAMGA VE MÜKÂFAT SİSTEMİ:
                                - Platform üzerindeki damgalama, puan, borç takip ve 6 damga ödül haklarında sistem yöneticisinin belirlediği kurallar geçerlidir.

                                5. GİZLİLİK VE KVKK:
                                - Kişisel verileriniz 6698 sayılı KVKK kapsamında sistem güvenliği ve üyelik doğrulama amacıyla işlenmektedir.

                                İşbu sözleşmeyi onaylayarak yukarıdaki tüm şartları ve yasal sorumluluk reddi beyanını kayıtsız şartsız kabul etmiş sayılırsınız.
                                """.trimIndent(),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                kvkkAccepted = true
                                showContractDialog = false
                            }
                        ) {
                            Text("Okudum & Kabul Ediyorum")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showContractDialog = false }) {
                            Text("Kapat")
                        }
                    }
                )
            }

            if (registerState is Resource.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = registerState.message ?: "Kayıt hatası",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!kvkkAccepted) {
                        ToastUtils.showToast(message = "Lütfen Kayıt ve Ön Bilgilendirme Sözleşmesini okuyup onaylayınız!")
                        return@Button
                    }
                    authViewModel.registerDealer(
                        null, galleryName, authorizedName, phone, email, password,
                        city, district, address, taxNumber, iban, ibanOwnerName, description, logoUri
                    )
                },
                enabled = kvkkAccepted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Başvuruyu Gönder (PENDING)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

