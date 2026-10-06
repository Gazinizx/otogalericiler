package com.example.anadolugalericilersit.ui.screens.vehicle

import com.example.anadolugalericilersit.utils.ToastUtils
import com.example.anadolugalericilersit.utils.rememberImagePickerLauncher
import com.example.anadolugalericilersit.utils.rememberMultipleImagePickerLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.local.LocalStore
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

    var wizardStep by rememberSaveable { mutableStateOf(if (vehicleIdToEdit.isNullOrBlank()) 0 else 5) }

    var category by rememberSaveable { mutableStateOf("Otomobil") }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var packageVariant by rememberSaveable { mutableStateOf("") }
    var licensePlate by rememberSaveable { mutableStateOf("") }
    var km by rememberSaveable { mutableStateOf("") }
    var fuelType by rememberSaveable { mutableStateOf("Benzin") }
    var transmission by rememberSaveable { mutableStateOf("Otomatik") }
    var bodyType by rememberSaveable { mutableStateOf("Sedan") }
    var engineSize by rememberSaveable { mutableStateOf("") }
    var enginePower by rememberSaveable { mutableStateOf("") }
    var drivetrain by rememberSaveable { mutableStateOf("Önden Çekiş") }
    var color by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var expertReportStatus by rememberSaveable { mutableStateOf("") }
    var expertReportDetails by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    val selectedFeatures = remember { mutableStateListOf<String>() }
    val newImageUris = remember { mutableStateListOf<Any>() }
    var selectedVideoUri by remember { mutableStateOf<Any?>(null) }
    var selectedReportUri by remember { mutableStateOf<Any?>(null) }
    val bodyPartsMap = remember { mutableStateMapOf<String, String>() }

    var searchQuery by remember { mutableStateOf("") }

    var isFuelTypeExpanded by remember { mutableStateOf(false) }
    var isTransmissionExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Otomobil", "Arazi, SUV & Pick-up", "Ticari Araçlar", "Motosiklet")
    val yearsList = (2025 downTo 1970).map { it.toString() }
    val packageOptions = remember(model) {
        Constants.MODEL_PACKAGES[model] ?: listOf(
            "Full Paket (Highline / AMG / M Sport / Elite)",
            "Orta Paket (Comfortline / Titanium / Style / Touch)",
            "Standart Paket (Joy / Trendline / Ambiente / Prime)"
        )
    }

    fun formatNumberWithDots(input: String): String {
        val digitsOnly = input.filter { it.isDigit() }
        if (digitsOnly.isBlank()) return ""
        val number = digitsOnly.toLongOrNull() ?: return digitsOnly
        val str = number.toString()
        val sb = StringBuilder()
        val len = str.length
        for (i in 0 until len) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append('.')
            }
            sb.append(str[i])
        }
        return sb.toString()
    }

    fun detectDefaultDrivetrain(selectedBrand: String, selectedModel: String): String {
        val b = selectedBrand.lowercase()
        val m = selectedModel.lowercase()
        return when {
            b.contains("bmw") && (m.contains("3") || m.contains("5") || m.contains("7") || m.contains("4") || m.contains("z4") || m.contains("6")) -> "Arkadan İtiş"
            b.contains("mercedes") && (m.contains("c-") || m.contains("e-") || m.contains("s-") || m.contains("cls") || m.contains("slk") || m.contains("slc")) -> "Arkadan İtiş"
            b.contains("alfa") && m.contains("giulia") -> "Arkadan İtiş"
            b.contains("porsche") && (m.contains("911") || m.contains("718") || m.contains("boxster") || m.contains("cayman")) -> "Arkadan İtiş"
            b.contains("subaru") || b.contains("jeep") || b.contains("land rover") || m.contains("x3") || m.contains("x5") || m.contains("x6") || m.contains("x7") || m.contains("quattro") || m.contains("4matic") || m.contains("xdrive") || m.contains("q5") || m.contains("q7") || m.contains("q8") || m.contains("4x4") || m.contains("4wd") || m.contains("awd") -> "4x4 / AWD (Dört Çeker)"
            else -> "Önden Çekiş"
        }
    }

    fun applyPackageFeatures(pkg: String) {
        selectedFeatures.clear()
        when {
            pkg.contains("Full", ignoreCase = true) || pkg.contains("Highline", ignoreCase = true) || pkg.contains("AMG", ignoreCase = true) -> {
                selectedFeatures.addAll(listOf(
                    "Sunroof",
                    "Panoramik Cam Tavan",
                    "Deri Döşeme",
                    "Isıtmalı Ön Koltuklar",
                    "Adaptif Hız Sabitleyici (ACC)",
                    "Geri Görüş Kamerası",
                    "360 Derece Kamera",
                    "Apple CarPlay",
                    "Android Auto",
                    "Hayalet Gösterge Paneli",
                    "Matrix LED Far",
                    "Çift Bölgeli Dijital Klima",
                    "Anahtarsız Giriş ve Çalıştırma",
                    "Yokuş Kalkış Desteği",
                    "Start & Stop"
                ))
            }
            pkg.contains("Orta", ignoreCase = true) || pkg.contains("Comfortline", ignoreCase = true) -> {
                selectedFeatures.addAll(listOf(
                    "Geri Görüş Kamerası",
                    "Ön ve Arka Park Sensörü",
                    "Apple CarPlay",
                    "Android Auto",
                    "Çift Bölgeli Dijital Klima",
                    "Yokuş Kalkış Desteği",
                    "Start & Stop"
                ))
            }
            else -> {
                selectedFeatures.addAll(listOf(
                    "Ön ve Arka Park Sensörü",
                    "Yokuş Kalkış Desteği",
                    "Start & Stop"
                ))
            }
        }
    }

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
                expertReportStatus = v.expertReportStatus.ifBlank { "Hatasız / Boyasız" }
                expertReportDetails = v.expertReportDetails
                if (v.bodyPartsCondition.isNotEmpty()) {
                    bodyPartsMap.clear()
                    bodyPartsMap.putAll(v.bodyPartsCondition)
                }
                description = v.description

                selectedFeatures.clear()
                selectedFeatures.addAll(v.features)

                if (v.imageUrls.isNotEmpty()) {
                    newImageUris.clear()
                    newImageUris.addAll(v.imageUrls)
                }
                wizardStep = 5
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
            ToastUtils.showToast(message = "İlan başarıyla kaydedildi!")
            vehicleViewModel.clearSaveState()
            onSaveSuccess()
        } else if (saveState is Resource.Error) {
            ToastUtils.showToast(message = saveState.message ?: "İlan kaydedilemedi")
            vehicleViewModel.clearSaveState()
        }
    }

    if (uploadProgress != null) {
        LoadingDialog(
            message = uploadProgress?.second ?: "Fotoğraflar yükleniyor...",
            progress = uploadProgress?.first
        )
    }

    val stepTitles = listOf(
        "Kategori Seçimi",
        "Marka Seçimi",
        "Model Seçimi",
        "Model Yılı Seçimi",
        "Donanım Paketi Seçimi",
        "İlan Detayları & İlanı Yayınla"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (vehicleIdToEdit == null) "Sahibinden İlan Verme" else "İlanı Düzenle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (vehicleIdToEdit == null && wizardStep < 5) {
                            Text(
                                text = "Adım ${wizardStep + 1} / 5: ${stepTitles[wizardStep]}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (wizardStep > 0 && vehicleIdToEdit == null) {
                            wizardStep--
                            searchQuery = ""
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            // STEP WIZARD CONTENT (Sahibinden.com Dinamik Tık-Tık Akışı)
            when (wizardStep) {
                // STEP 0: Category Selection
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Lütfen İlan Kategorisini Seçiniz",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        categories.forEach { cat ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        category = cat
                                        wizardStep = 1
                                        searchQuery = ""
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (category == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Category,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = cat,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 1: Brand Selection
                1 -> {
                    val allBrands = Constants.BRANDS_WITH_MODELS.keys.toList()
                    val filteredBrands = remember(searchQuery, allBrands) {
                        if (searchQuery.isBlank()) allBrands else allBrands.filter { it.contains(searchQuery, ignoreCase = true) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "$category ➔ Marka Seçimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Marka Ara...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filteredBrands) { b ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            brand = b
                                            model = ""
                                            packageVariant = ""
                                            wizardStep = 2
                                            searchQuery = ""
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = b, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 2: Model Selection
                2 -> {
                    val modelList = Constants.BRANDS_WITH_MODELS[brand] ?: Constants.BRANDS_WITH_MODELS.values.flatten().distinct()
                    val filteredModels = remember(searchQuery, modelList) {
                        if (searchQuery.isBlank()) modelList else modelList.filter { it.contains(searchQuery, ignoreCase = true) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "$category ➔ $brand ➔ Model Seçimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("$brand Modeli Ara...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filteredModels) { m ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            model = m
                                            drivetrain = detectDefaultDrivetrain(brand, m)
                                            packageVariant = ""
                                            wizardStep = 3
                                            searchQuery = ""
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = m, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 3: Model Year Selection
                3 -> {
                    val filteredYears = remember(searchQuery, yearsList) {
                        if (searchQuery.isBlank()) yearsList else yearsList.filter { it.contains(searchQuery) }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "$brand $model ➔ Model Yılı Seçimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Yıl Ara (Örn: 2023)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredYears) { y ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            year = y
                                            wizardStep = 4
                                            searchQuery = ""
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (year == y) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = y,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (year == y) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // STEP 4: Trim Package Selection
                4 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "$brand $model ($year) ➔ Donanım Paketi Seçimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Paket seçtiğinizde ilgili donanım özellikleri ilanınıza otomatik olarak eklenecektir.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        packageOptions.forEach { pkg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        packageVariant = pkg
                                        applyPackageFeatures(pkg)
                                        wizardStep = 5
                                        ToastUtils.showToast(message = "Donanım özellikleri otomatik eklendi!")
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (packageVariant == pkg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = pkg,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // STEP 5: Final Form Details, Media Upload & Unconditional PUBLISH BUTTON
                5 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Category Summary Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$category ➔ $brand $model",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Model Yılı: $year • Paket: ${packageVariant.ifBlank { "Standart" }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                TextButton(onClick = { wizardStep = 0 }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Değiştir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Media Section (Photos & Video)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Araç Fotoğrafları ve Video",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "İlk fotoğraf ilan kapağı olacaktır.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clickable { imagePickerLauncher() },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.AddAPhoto,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Fotoğraf Ekle",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    items(newImageUris) { uri ->
                                        Box(modifier = Modifier.size(96.dp)) {
                                            AppAsyncImage(
                                                model = uri,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))
                                            )
                                            IconButton(
                                                onClick = { newImageUris.remove(uri) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(11.dp))
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Sil",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = { videoPickerLauncher() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selectedVideoUri == null) "Araç Tanıtım Videosu Ekle (Opsiyonel)" else "Video Seçildi ✓",
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        // Price & Technical Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF15803D),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fiyat ve Teknik Detaylar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OutlinedTextField(
                                    value = price,
                                    onValueChange = { price = formatNumberWithDots(it) },
                                    label = { Text("Fiyat (TL)") },
                                    placeholder = { Text("Örn: 850.000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = km,
                                    onValueChange = { km = formatNumberWithDots(it) },
                                    label = { Text("Kilometre (KM)") },
                                    placeholder = { Text("Örn: 120.000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                // Fuel Type Selection Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isFuelTypeExpanded,
                                    onExpandedChange = { isFuelTypeExpanded = !isFuelTypeExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = fuelType,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Yakıt Tipi *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isFuelTypeExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isFuelTypeExpanded,
                                        onDismissRequest = { isFuelTypeExpanded = false }
                                    ) {
                                        Constants.FUEL_TYPES.forEach { f ->
                                            DropdownMenuItem(
                                                text = { Text(f) },
                                                onClick = {
                                                    fuelType = f
                                                    isFuelTypeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Transmission Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = isTransmissionExpanded,
                                    onExpandedChange = { isTransmissionExpanded = !isTransmissionExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = transmission,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Vites Tipi *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTransmissionExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isTransmissionExpanded,
                                        onDismissRequest = { isTransmissionExpanded = false }
                                    ) {
                                        Constants.TRANSMISSIONS.forEach { t ->
                                            DropdownMenuItem(
                                                text = { Text(t) },
                                                onClick = {
                                                    transmission = t
                                                    isTransmissionExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Drivetrain Dropdown
                                var isDrivetrainExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = isDrivetrainExpanded,
                                    onExpandedChange = { isDrivetrainExpanded = !isDrivetrainExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = drivetrain,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Çekiş Tipi (Otomatik Seçildi) *") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDrivetrainExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isDrivetrainExpanded,
                                        onDismissRequest = { isDrivetrainExpanded = false }
                                    ) {
                                        listOf("Önden Çekiş", "Arkadan İtiş", "4x4 / AWD (Dört Çeker)").forEach { dt ->
                                            DropdownMenuItem(
                                                text = { Text(dt) },
                                                onClick = {
                                                    drivetrain = dt
                                                    isDrivetrainExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = engineSize,
                                        onValueChange = { engineSize = it },
                                        label = { Text("Motor Hacmi / Tipi") },
                                        placeholder = { Text("Örn: 1.6 TSI") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = enginePower,
                                        onValueChange = { enginePower = it },
                                        label = { Text("Motor Gücü") },
                                        placeholder = { Text("Örn: 150 HP") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = licensePlate,
                                    onValueChange = { licensePlate = it.uppercase() },
                                    label = { Text("Araç Plakası (Plaka)") },
                                    singleLine = true,
                                    placeholder = { Text("Örn: 38 AB 123") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        // Body Parts & Expert Inspection Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Kaporta Durumu ve Ekspertiz Şeması",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = expertReportDetails,
                                    onValueChange = { expertReportDetails = it },
                                    label = { Text("Ekspertiz Detay Notları") },
                                    placeholder = { Text("Örn: Sol ön çamurluk boyalı...") },
                                    maxLines = 3,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedButton(
                                    onClick = { reportPickerLauncher() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (selectedReportUri == null) "Ekspertiz Belgesi / Raporu Yükle" else "Ekspertiz Belgesi Seçildi ✓", fontSize = 13.sp)
                                }

                                Text(
                                    text = "Kaporta Şeması (Parça Durumlarını Seçiniz):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp)
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
                                                        fontWeight = FontWeight.Bold,
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
                            }
                        }

                        // Equipment Features Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Donanım Özellikleri (${packageVariant.ifBlank { "Özel" }})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

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
                            }
                        }

                        // Detailed Description & UNCONDITIONAL PUBLISH / UPDATE BUTTON
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Detaylı İlan Açıklaması",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("İlan Açıklaması") },
                                    placeholder = { Text("Aracın bakım geçmişi, ekstra detaylar vb. yazınız...") },
                                    maxLines = 6,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                if (saveState is Resource.Error) {
                                    Text(
                                        text = saveState.message ?: "İlan kaydedilemedi",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // PROMINENT UNCONDITIONAL PUBLISH BUTTON
                                Button(
                                    onClick = {
                                        val cleanBrand = brand.trim().ifBlank { "Otomobil" }
                                        val cleanModel = model.trim().ifBlank { "Genel Model" }
                                        val cleanPrice = price.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
                                        val cleanKm = km.filter { it.isDigit() }.toIntOrNull() ?: 0

                                        val activeUid = LocalStore.currentLoggedInUid ?: ""
                                        val activeUser = LocalStore.users[activeUid]
                                        val activeDealer = LocalStore.dealers[activeUid] ?: dealer

                                        val dId = activeDealer?.id?.ifBlank { activeUid } ?: activeUid.ifBlank { "dealer_01" }
                                        val dName = activeDealer?.galleryName?.ifBlank { activeUser?.name?.ifBlank { activeUser.email } ?: "Galeri" }
                                            ?: activeUser?.name?.ifBlank { activeUser.email }
                                            ?: "Galeri İlanı"
                                        val dPhone = activeDealer?.phone ?: ""
                                        val dCity = activeDealer?.city ?: "Kayseri"
                                        val dDistrict = activeDealer?.district ?: "Melikgazi"
                                        val dLogo = activeDealer?.logoUrl ?: activeDealer?.shopPhotoUrl ?: activeDealer?.profilePhotoUrl ?: ""

                                        val v = Vehicle(
                                            id = vehicleIdToEdit ?: "",
                                            dealerId = dId,
                                            dealerName = dName,
                                            dealerPhone = dPhone,
                                            dealerCity = dCity,
                                            dealerDistrict = dDistrict,
                                            dealerLogoUrl = dLogo,
                                            brand = cleanBrand,
                                            model = cleanModel,
                                            licensePlate = licensePlate.trim(),
                                            year = year.toIntOrNull() ?: 2022,
                                            km = cleanKm,
                                            fuelType = fuelType,
                                            transmission = transmission,
                                            bodyType = bodyType,
                                            engineSize = engineSize,
                                            enginePower = enginePower,
                                            drivetrain = drivetrain,
                                            color = color,
                                            price = cleanPrice,
                                            negotiable = false,
                                            suitableForCredit = true,
                                            expertInspectionCost = 0.0,
                                            expertReportStatus = "",
                                            expertReportDetails = expertReportDetails,
                                            bodyPartsCondition = bodyPartsMap.toMap(),
                                            damgaStatus = DamgaStatus.NONE,
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = if (vehicleIdToEdit.isNullOrBlank()) "🚀 İlanı Yayınla" else "✏️ İlanı Güncelle",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
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
