package com.example.anadolugalericilersit.ui.screens.vehicle

import com.example.anadolugalericilersit.utils.ToastUtils
import com.example.anadolugalericilersit.utils.rememberImagePickerLauncher
import com.example.anadolugalericilersit.utils.rememberMultipleImagePickerLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.DamgaStatus
import com.example.anadolugalericilersit.data.model.Dealer
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleStatus
import com.example.anadolugalericilersit.data.repository.VehicleRepository
import com.example.anadolugalericilersit.ui.components.AppAsyncImage
import com.example.anadolugalericilersit.ui.components.LoadingDialog
import com.example.anadolugalericilersit.ui.viewmodel.VehicleViewModel
import com.example.anadolugalericilersit.utils.Constants
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleScreen(
    vehicleIdToEdit: String?,
    vehicleViewModel: VehicleViewModel,
    dealer: Dealer?,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val saveState by vehicleViewModel.saveState.collectAsState()
    val uploadProgress by vehicleViewModel.uploadProgress.collectAsState()

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2022") }
    var km by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Benzin") }
    var transmission by remember { mutableStateOf("Otomatik") }
    var bodyType by remember { mutableStateOf("Sedan") }
    var engineSize by remember { mutableStateOf("1.6") }
    var enginePower by remember { mutableStateOf("170 HP") }
    var drivetrain by remember { mutableStateOf("Arkadan İtiş") }
    var color by remember { mutableStateOf("Beyaz") }
    var price by remember { mutableStateOf("") }
    var negotiable by remember { mutableStateOf(false) }
    var suitableForCredit by remember { mutableStateOf(true) }
    var expertInspectionCost by remember { mutableStateOf("0") }
    var expertReportStatus by remember { mutableStateOf("Hatasız / Boyasız") }
    var expertReportDetails by remember { mutableStateOf("") }
    var requestDamga by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    val selectedFeatures = remember { mutableStateListOf<String>() }
    val newImageUris = remember { mutableStateListOf<Any>() }
    var selectedVideoUri by remember { mutableStateOf<Any?>(null) }
    var selectedReportUri by remember { mutableStateOf<Any?>(null) }
    val bodyPartsMap = remember { mutableStateMapOf<String, String>() }

    var isBrandExpanded by remember { mutableStateOf(false) }
    var isExpertStatusExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (bodyPartsMap.isEmpty()) {
            Constants.BODY_PARTS.forEach { part ->
                bodyPartsMap[part] = "Orijinal"
            }
        }
    }

    LaunchedEffect(vehicleIdToEdit) {
        if (!vehicleIdToEdit.isNullOrBlank()) {
            val res = VehicleRepository().getVehicleById(vehicleIdToEdit)
            if (res is Resource.Success && res.data != null) {
                val v = res.data
                brand = v.brand
                model = v.model
                licensePlate = v.licensePlate
                year = v.year.toString()
                km = v.km.toString()
                fuelType = v.fuelType
                transmission = v.transmission
                bodyType = v.bodyType
                engineSize = v.engineSize
                enginePower = v.enginePower
                drivetrain = v.drivetrain
                color = v.color
                price = v.price.toString()
                negotiable = v.negotiable
                suitableForCredit = v.suitableForCredit
                expertInspectionCost = v.expertInspectionCost.toString()
                expertReportStatus = v.expertReportStatus.ifBlank { "Hatasız / Boyasız" }
                expertReportDetails = v.expertReportDetails
                if (v.bodyPartsCondition.isNotEmpty()) {
                    bodyPartsMap.clear()
                    bodyPartsMap.putAll(v.bodyPartsCondition)
                }
                requestDamga = v.damgaStatus == DamgaStatus.PENDING || v.damgaStatus == DamgaStatus.APPROVED || v.hasDamga
                description = v.description

                selectedFeatures.clear()
                selectedFeatures.addAll(v.features)
            }
        }
    }

    val imagePickerLauncher = rememberMultipleImagePickerLauncher { uris ->
        newImageUris.addAll(uris)
    }

    val videoPickerLauncher = rememberImagePickerLauncher { uri ->
        selectedVideoUri = uri
    }

    val reportPickerLauncher = rememberImagePickerLauncher { uri ->
        selectedReportUri = uri
    }


    LaunchedEffect(saveState) {
        if (saveState is Resource.Success) {
            ToastUtils.showToast(message = "İlan kaydedildi!")
            vehicleViewModel.clearSaveState()
            onSaveSuccess()
        }
    }

    if (uploadProgress != null) {
        LoadingDialog(
            message = uploadProgress?.second ?: "Fotoğraflar yükleniyor...",
            progress = uploadProgress?.first
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vehicleIdToEdit == null) "Yeni İlan Ekle" else "İlanı Düzenle") },
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
                .padding(16.dp)
        ) {
            Text(text = "Araç Fotoğrafları ve Video", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Image Picker & List
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(
                        modifier = Modifier
                            .size(90.dp)
                            .clickable { imagePickerLauncher() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null)
                            Text("Fotoğraf Ekle", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                items(newImageUris) { uri ->
                    Box(modifier = Modifier.size(90.dp)) {
                        AppAsyncImage(
                            model = uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                        IconButton(
                            onClick = { newImageUris.remove(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Sil", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Video Picker
            OutlinedButton(
                onClick = { videoPickerLauncher() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.VideoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedVideoUri == null) "Araç Videosu Seç (Opsiyonel)" else "Video Seçildi ✓")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Temel Bilgiler", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Brand Dropdown
            ExposedDropdownMenuBox(
                expanded = isBrandExpanded,
                onExpandedChange = { isBrandExpanded = !isBrandExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marka *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBrandExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = isBrandExpanded,
                    onDismissRequest = { isBrandExpanded = false }
                ) {
                    Constants.BRANDS_WITH_MODELS.keys.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b) },
                            onClick = {
                                brand = b
                                isBrandExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text("Araç Plakası (Plaka) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Yıl") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it },
                    label = { Text("Kilometre") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Fiyat (TL) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = negotiable, onCheckedChange = { negotiable = it })
                Text("Pazarlık Var")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = suitableForCredit, onCheckedChange = { suitableForCredit = it })
                Text("Krediye Uygun")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Ekspertiz Raporu ve Araç Durumu", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Expert Status Dropdown
            ExposedDropdownMenuBox(
                expanded = isExpertStatusExpanded,
                onExpandedChange = { isExpertStatusExpanded = !isExpertStatusExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = expertReportStatus,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ekspertiz Durumu *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpertStatusExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = isExpertStatusExpanded,
                    onDismissRequest = { isExpertStatusExpanded = false }
                ) {
                    val statuses = listOf(
                        "Hatasız / Boyasız / Değişensiz",
                        "Lokal Boyalı",
                        "Boyalı Parçaları Var",
                        "Değişen Parçalı",
                        "Ekspertiz Raporu Mevcut",
                        "Ağır Hasar Kayıtlı"
                    )
                    statuses.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                expertReportStatus = s
                                isExpertStatusExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = expertReportDetails,
                onValueChange = { expertReportDetails = it },
                label = { Text("Ekspertiz Detayları ve Notları (Örn: Kaput boyasız, sağ çamurluk boyalı)") },
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { reportPickerLauncher() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedReportUri == null) "Ekspertiz Rapor Belgesi / Fotoğrafı Yükle" else "Ekspertiz Belgesi Seçildi ✓")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Parts Condition Selection List
            Text(text = "Kaporta Parçaları ve Ekspertiz Şeması", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = "Aracın her bir kaporta parçası için durumunu (Orijinal, Lokal, Boyalı, Değişen) işaretleyiniz:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Constants.BODY_PARTS.forEach { part ->
                        val currentCondition = bodyPartsMap[part] ?: "Orijinal"
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = part,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentCondition,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (currentCondition) {
                                        "Orijinal" -> Color(0xFF15803D)
                                        "Lokal Boyalı" -> Color(0xFFD97706)
                                        "Boyalı" -> Color(0xFF2563EB)
                                        "Değişen" -> Color(0xFFDC2626)
                                        else -> Color.Gray
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(Constants.BODY_PART_CONDITIONS) { condition ->
                                    val isSelected = currentCondition == condition
                                    val chipBg = when (condition) {
                                        "Orijinal" -> if (isSelected) Color(0xFF15803D) else MaterialTheme.colorScheme.surface
                                        "Lokal Boyalı" -> if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.surface
                                        "Boyalı" -> if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.surface
                                        "Değişen" -> if (isSelected) Color(0xFFDC2626) else MaterialTheme.colorScheme.surface
                                        else -> if (isSelected) Color(0xFF6B7280) else MaterialTheme.colorScheme.surface
                                    }

                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { bodyPartsMap[part] = condition },
                                        label = {
                                            Text(
                                                text = condition,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = chipBg
                                        )
                                    )
                                }
                            }
                        }

                        if (part != Constants.BODY_PARTS.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = expertInspectionCost,
                onValueChange = { expertInspectionCost = it },
                label = { Text("Ekspertiz Ücreti (TL)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            val expCost = expertInspectionCost.toDoubleOrNull() ?: 0.0
            if (expCost >= 5000.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = requestDamga,
                            onCheckedChange = { requestDamga = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Damga Talebi Oluştur (5000 TL+ Ekspertiz)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Yönetici onayı ve IBAN doğrulaması ile damga rozeti kazanın.", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Donanım Özellikleri", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Multi-select features
            Constants.EQUIPMENT_OPTIONS.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    row.forEach { feat ->
                        val isSelected = selectedFeatures.contains(feat)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isSelected) selectedFeatures.remove(feat) else selectedFeatures.add(feat)
                                }
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { if (it) selectedFeatures.add(feat) else selectedFeatures.remove(feat) }
                            )
                            Text(text = feat, fontSize = 12.sp)
                        }
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Açıklama") },
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            if (saveState is Resource.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = saveState.message ?: "İlan kaydedilemedi",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (dealer == null) return@Button
                    val v = Vehicle(
                        dealerId = dealer.id,
                        dealerName = dealer.galleryName,
                        dealerPhone = dealer.phone,
                        dealerCity = dealer.city,
                        dealerDistrict = dealer.district,
                        dealerLogoUrl = dealer.logoUrl,
                        brand = brand,
                        model = model,
                        licensePlate = licensePlate,
                        year = year.toIntOrNull() ?: 2022,
                        km = km.toIntOrNull() ?: 0,
                        fuelType = fuelType,
                        transmission = transmission,
                        bodyType = bodyType,
                        engineSize = engineSize,
                        enginePower = enginePower,
                        drivetrain = drivetrain,
                        color = color,
                        price = price.toDoubleOrNull() ?: 0.0,
                        negotiable = negotiable,
                        suitableForCredit = suitableForCredit,
                        expertInspectionCost = expCost,
                        expertReportStatus = expertReportStatus,
                        expertReportDetails = expertReportDetails,
                        bodyPartsCondition = bodyPartsMap.toMap(),
                        damgaStatus = if (expCost >= 5000.0 && requestDamga) DamgaStatus.PENDING else DamgaStatus.NONE,
                        description = description,
                        features = selectedFeatures.toList(),
                        status = VehicleStatus.PUBLISHED
                    )

                    vehicleViewModel.saveVehicle(
                        context = null,
                        vehicle = v,
                        newImageUris = newImageUris,
                        videoUri = selectedVideoUri,
                        expertReportUri = selectedReportUri
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("İlanı Yayınla", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

        }
    }
}
