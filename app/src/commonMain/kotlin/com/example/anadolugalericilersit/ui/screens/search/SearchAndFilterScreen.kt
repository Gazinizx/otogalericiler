package com.example.anadolugalericilersit.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.data.model.VehicleFilter
import com.example.anadolugalericilersit.data.model.VehicleSort
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.FavoriteViewModel
import com.example.anadolugalericilersit.ui.viewmodel.VehicleViewModel
import com.example.anadolugalericilersit.utils.Constants
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilterScreen(
    vehicleViewModel: VehicleViewModel,
    favoriteViewModel: FavoriteViewModel,
    currentUserId: String?,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val searchListState by vehicleViewModel.searchListState.collectAsState()
    val favoriteIds by favoriteViewModel.favoriteIds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    var selectedModel by remember { mutableStateOf<String?>(null) }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var minYear by remember { mutableStateOf("") }
    var maxYear by remember { mutableStateOf("") }
    var minKm by remember { mutableStateOf("") }
    var maxKm by remember { mutableStateOf("") }
    var selectedFuel by remember { mutableStateOf<String?>(null) }
    var selectedTransmission by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf(VehicleSort.NEWEST) }

    var showFilterSheet by remember { mutableStateOf(false) }

    fun triggerSearch() {
        val filter = VehicleFilter(
            searchQuery = searchQuery,
            brand = selectedBrand,
            model = selectedModel,
            minPrice = minPrice.toDoubleOrNull(),
            maxPrice = maxPrice.toDoubleOrNull(),
            minYear = minYear.toIntOrNull(),
            maxYear = maxYear.toIntOrNull(),
            minKm = minKm.toIntOrNull(),
            maxKm = maxKm.toIntOrNull(),
            fuelType = selectedFuel,
            transmission = selectedTransmission,
            city = selectedCity
        )
        vehicleViewModel.searchVehicles(filter, selectedSort)
    }

    LaunchedEffect(Unit) {
        triggerSearch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Araç Arama & Filtreleme") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtrele")
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
            // Search Text Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    triggerSearch()
                },
                placeholder = { Text("Marka, model, ilan başlığı ara...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            triggerSearch()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Temizle")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Sort & Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Detaylı Filtre")
                }

                // Sort Dropdown
                var sortExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { sortExpanded = true }) {
                        Text("Sırala: ${selectedSort.label}")
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        VehicleSort.values().forEach { sort ->
                            DropdownMenuItem(
                                text = { Text(sort.label) },
                                onClick = {
                                    selectedSort = sort
                                    sortExpanded = false
                                    triggerSearch()
                                }
                            )
                        }
                    }
                }
            }

            // Results List
            when (searchListState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = searchListState.message ?: "Arama hatası", color = MaterialTheme.colorScheme.error)
                    }
                }

                is Resource.Success -> {
                    val list = searchListState.data ?: emptyList()
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Arama kriterlerine uygun araç bulunamadı.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { vehicle: Vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    isFavorite = favoriteIds.contains(vehicle.id),
                                    onFavoriteClick = if (currentUserId != null) {
                                        { favoriteViewModel.toggleFavorite(vehicle.id, currentUserId) }
                                    } else null,
                                    onClick = { onNavigateToVehicleDetail(vehicle.id) }
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    // Filter Bottom Sheet Dialog
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Detaylı Filtreleme", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = {
                            selectedBrand = null
                            selectedModel = null
                            minPrice = ""
                            maxPrice = ""
                            minYear = ""
                            maxYear = ""
                            minKm = ""
                            maxKm = ""
                            selectedFuel = null
                            selectedTransmission = null
                            selectedCity = null
                            triggerSearch()
                            showFilterSheet = false
                        }
                    ) {
                        Text("Filtreleri Temizle")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Range
                Text("Fiyat Aralığı (₺)", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Min Fiyat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Maks Fiyat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Year Range
                Text("Yıl Aralığı", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minYear,
                        onValueChange = { minYear = it },
                        label = { Text("Min Yıl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxYear,
                        onValueChange = { maxYear = it },
                        label = { Text("Maks Yıl") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KM Range
                Text("Kilometre (KM) Aralığı", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minKm,
                        onValueChange = { minKm = it },
                        label = { Text("Min KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxKm,
                        onValueChange = { maxKm = it },
                        label = { Text("Maks KM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        triggerSearch()
                        showFilterSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sonuçları Göster")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
