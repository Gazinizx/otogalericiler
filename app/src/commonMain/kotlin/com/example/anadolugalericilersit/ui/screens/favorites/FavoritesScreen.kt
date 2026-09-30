package com.example.anadolugalericilersit.ui.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.anadolugalericilersit.data.model.Vehicle
import com.example.anadolugalericilersit.ui.components.VehicleCard
import com.example.anadolugalericilersit.ui.viewmodel.FavoriteViewModel
import com.example.anadolugalericilersit.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favoriteViewModel: FavoriteViewModel,
    currentUserId: String?,
    onNavigateToLogin: () -> Unit,
    onNavigateToVehicleDetail: (String) -> Unit
) {
    val favoritesState by favoriteViewModel.favoritesState.collectAsState()

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            favoriteViewModel.loadFavorites(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favori Araçlarım") }
            )
        }
    ) { padding ->
        if (currentUserId == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Favorilerinizi Görmek İçin Giriş Yapın",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Beğendiğiniz araçları favorilerinize eklemek ve takip etmek için hesabınıza giriş yapmalısınız.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onNavigateToLogin) {
                    Text("Giriş Yap")
                }
            }
        } else {
            when (favoritesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = favoritesState.message ?: "Favoriler yüklenemedi", color = MaterialTheme.colorScheme.error)
                    }
                }

                is Resource.Success -> {
                    val list = favoritesState.data ?: emptyList()
                    if (list.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Henüz favori araç eklemediniz.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { vehicle: Vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    isFavorite = true,
                                    onFavoriteClick = { favoriteViewModel.toggleFavorite(vehicle.id, currentUserId) },
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
}
