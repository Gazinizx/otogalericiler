package com.example.anadolugalericilersit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.anadolugalericilersit.data.model.DealerStatus
import com.example.anadolugalericilersit.data.model.Role
import com.example.anadolugalericilersit.ui.screens.admin.AdminDamgaScreen
import com.example.anadolugalericilersit.ui.screens.admin.AdminDashboardScreen
import com.example.anadolugalericilersit.ui.screens.admin.AdminDealersScreen
import com.example.anadolugalericilersit.ui.screens.admin.AdminDebtScreen
import com.example.anadolugalericilersit.ui.screens.admin.AdminReportsScreen
import com.example.anadolugalericilersit.ui.screens.admin.AdminVehiclesScreen
import com.example.anadolugalericilersit.ui.screens.auth.DealerRegisterScreen
import com.example.anadolugalericilersit.ui.screens.auth.LoginScreen
import com.example.anadolugalericilersit.ui.screens.auth.PendingApprovalScreen
import com.example.anadolugalericilersit.ui.screens.dealer.DealerDetailScreen
import com.example.anadolugalericilersit.ui.screens.dealer.DealerDebtScreen
import com.example.anadolugalericilersit.ui.screens.dealer.DealerRewardsScreen
import com.example.anadolugalericilersit.ui.screens.favorites.FavoritesScreen
import com.example.anadolugalericilersit.ui.screens.home.HomeScreen
import com.example.anadolugalericilersit.ui.screens.mylistings.MyListingsScreen
import com.example.anadolugalericilersit.ui.screens.notifications.NotificationsScreen
import com.example.anadolugalericilersit.ui.screens.profile.EditProfileScreen
import com.example.anadolugalericilersit.ui.screens.profile.ProfileScreen
import com.example.anadolugalericilersit.ui.screens.profile.SettingsScreen
import com.example.anadolugalericilersit.ui.screens.search.SearchAndFilterScreen
import com.example.anadolugalericilersit.ui.screens.vehicle.AddEditVehicleScreen
import com.example.anadolugalericilersit.ui.screens.vehicle.VehicleDetailScreen
import com.example.anadolugalericilersit.ui.viewmodel.*

@Composable
fun AnadoluNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    vehicleViewModel: VehicleViewModel = viewModel(),
    dealerViewModel: DealerViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val dealerState by authViewModel.dealerState.collectAsState()

    val currentUser = authState.data
    val currentDealer = dealerState.data

    val isDealer = currentUser?.dealerId != null || currentDealer != null
    val isApprovedDealer = currentDealer?.accountStatus == DealerStatus.APPROVED
    val isAdmin = currentUser?.role == Role.ADMIN ||
            currentUser?.role == Role.SUPER_ADMIN ||
            currentUser?.canIssueDamga == true

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Favorites.route,
        Screen.MyListings.route,
        Screen.Profile.route,
        Screen.AdminDashboard.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    navController = navController,
                    isDealer = isDealer,
                    isAdmin = isAdmin
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        val user = authViewModel.authState.value.data
                        val adminCheck = user?.role == Role.ADMIN ||
                                user?.role == Role.SUPER_ADMIN ||
                                user?.canIssueDamga == true
                        if (adminCheck) {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onContinueAsGuest = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }


            composable(Screen.Register.route) {
                DealerRegisterScreen(
                    authViewModel = authViewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.PendingApproval.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.PendingApproval.route) {
                PendingApprovalScreen(
                    authViewModel = authViewModel,
                    onApproved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.PendingApproval.route) { inclusive = true }
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    favoriteViewModel = favoriteViewModel,
                    isDealer = isDealer,
                    isApprovedDealer = isApprovedDealer,
                    currentUserId = currentUser?.uid,
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) },
                    onNavigateToDealerDetail = { id -> navController.navigate(Screen.DealerDetail.createRoute(id)) },
                    onNavigateToAddVehicle = { navController.navigate(Screen.AddEditVehicle.createRoute(null)) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
                )
            }

            composable(Screen.Search.route) {
                SearchAndFilterScreen(
                    vehicleViewModel = vehicleViewModel,
                    favoriteViewModel = favoriteViewModel,
                    currentUserId = currentUser?.uid,
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    favoriteViewModel = favoriteViewModel,
                    currentUserId = currentUser?.uid,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                )
            }

            composable(Screen.MyListings.route) {
                if (currentDealer?.accountStatus == DealerStatus.PENDING) {
                    navController.navigate(Screen.PendingApproval.route) {
                        popUpTo(Screen.MyListings.route) { inclusive = true }
                    }
                } else {
                    MyListingsScreen(
                        vehicleViewModel = vehicleViewModel,
                        dealer = currentDealer,
                        onNavigateToAddVehicle = { navController.navigate(Screen.AddEditVehicle.createRoute(null)) },
                        onNavigateToEditVehicle = { id -> navController.navigate(Screen.AddEditVehicle.createRoute(id)) },
                        onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                    )
                }
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    user = currentUser,
                    dealer = currentDealer,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToAdminDashboard = { navController.navigate(Screen.AdminDashboard.route) },
                    onNavigateToDealerRewards = { navController.navigate(Screen.DealerRewards.route) },
                    onNavigateToDealerDebt = { navController.navigate(Screen.DealerDebt.route) }
                )
            }

            composable(
                route = Screen.VehicleDetail.route,
                arguments = listOf(navArgument("vehicleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId") ?: ""
                VehicleDetailScreen(
                    vehicleId = vehicleId,
                    vehicleViewModel = vehicleViewModel,
                    favoriteViewModel = favoriteViewModel,
                    currentUserId = currentUser?.uid,
                    userEmail = currentUser?.email,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToDealerDetail = { id -> navController.navigate(Screen.DealerDetail.createRoute(id)) }
                )
            }

            composable(
                route = Screen.AddEditVehicle.route,
                arguments = listOf(navArgument("vehicleId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getString("vehicleId")
                AddEditVehicleScreen(
                    vehicleIdToEdit = vehicleId,
                    vehicleViewModel = vehicleViewModel,
                    dealer = currentDealer,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.DealerDetail.route,
                arguments = listOf(navArgument("dealerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val dealerId = backStackEntry.arguments?.getString("dealerId") ?: ""
                DealerDetailScreen(
                    dealerId = dealerId,
                    dealerViewModel = dealerViewModel,
                    favoriteViewModel = favoriteViewModel,
                    currentUserId = currentUser?.uid,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                )
            }

            composable(Screen.Notifications.route) {
                if (currentUser != null) {
                    NotificationsScreen(
                        notificationViewModel = notificationViewModel,
                        currentUserId = currentUser.uid,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    dealerViewModel = dealerViewModel,
                    dealer = currentDealer,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToDealers = { navController.navigate(Screen.AdminDealers.route) },
                    onNavigateToVehicles = { navController.navigate(Screen.AdminVehicles.route) },
                    onNavigateToReports = { navController.navigate(Screen.AdminReports.route) },
                    onNavigateToDamga = { navController.navigate(Screen.AdminDamga.route) },
                    onNavigateToDebts = { navController.navigate(Screen.AdminDebts.route) }
                )
            }

            composable(Screen.AdminDamga.route) {
                AdminDamgaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminDebts.route) {
                AdminDebtScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.DealerRewards.route) {
                DealerRewardsScreen(
                    dealer = currentDealer,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.DealerDebt.route) {
                DealerDebtScreen(
                    dealer = currentDealer,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.AdminDealers.route) {
                AdminDealersScreen(
                    adminViewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToDealerDetail = { id -> navController.navigate(Screen.DealerDetail.createRoute(id)) }
                )
            }

            composable(Screen.AdminVehicles.route) {
                AdminVehiclesScreen(
                    adminViewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                )
            }

            composable(Screen.AdminReports.route) {
                AdminReportsScreen(
                    adminViewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToVehicleDetail = { id -> navController.navigate(Screen.VehicleDetail.createRoute(id)) }
                )
            }
        }
    }
}
