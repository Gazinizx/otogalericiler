package com.example.anadolugalericilersit.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun AppBottomBar(
    navController: NavController,
    isDealer: Boolean = false,
    isAdmin: Boolean = false
) {
    val items = mutableListOf(
        BottomNavItem("Ana Sayfa", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Ara", Screen.Search.route, Icons.Default.Search),
        BottomNavItem("Favoriler", Screen.Favorites.route, Icons.Default.Favorite)
    )

    if (isAdmin) {
        items.add(BottomNavItem("Yönetim", Screen.AdminDashboard.route, Icons.Default.AdminPanelSettings))
    } else if (isDealer) {
        items.add(BottomNavItem("İlanlarım", Screen.MyListings.route, Icons.Default.DirectionsCar))
    } else {
        items.add(BottomNavItem("Giriş Yap", Screen.Login.route, Icons.Default.Person))
    }

    items.add(BottomNavItem("Profil", Screen.Profile.route, Icons.Default.Person))

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
