package com.example.anadolugalericilersit.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object PendingApproval : Screen("pending_approval")
    
    object Home : Screen("home")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object MyListings : Screen("my_listings")
    object Profile : Screen("profile")
    
    object VehicleDetail : Screen("vehicle_detail/{vehicleId}") {
        fun createRoute(vehicleId: String) = "vehicle_detail/$vehicleId"
    }
    
    object AddEditVehicle : Screen("add_edit_vehicle?vehicleId={vehicleId}") {
        fun createRoute(vehicleId: String? = null) = if (vehicleId != null) "add_edit_vehicle?vehicleId=$vehicleId" else "add_edit_vehicle"
    }
    
    object DealerDetail : Screen("dealer_detail/{dealerId}") {
        fun createRoute(dealerId: String) = "dealer_detail/$dealerId"
    }
    
    object Notifications : Screen("notifications")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    
    object AdminDashboard : Screen("admin_dashboard")
    object AdminDealers : Screen("admin_dealers")
    object AdminVehicles : Screen("admin_vehicles")
    object AdminReports : Screen("admin_reports")
    object AdminDamga : Screen("admin_damga")
    object AdminDebts : Screen("admin_debts")
    object DealerRewards : Screen("dealer_rewards")
    object DealerDebt : Screen("dealer_debt")
}
