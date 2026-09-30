package com.example.anadolugalericilersit.data.model

data class VehicleFilter(
    val searchQuery: String = "",
    val brand: String? = null,
    val model: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minKm: Int? = null,
    val maxKm: Int? = null,
    val fuelType: String? = null,
    val transmission: String? = null,
    val bodyType: String? = null,
    val city: String? = null,
    val color: String? = null
) {
    fun isFiltered(): Boolean {
        return searchQuery.isNotBlank() ||
                brand != null ||
                model != null ||
                minPrice != null ||
                maxPrice != null ||
                minYear != null ||
                maxYear != null ||
                minKm != null ||
                maxKm != null ||
                fuelType != null ||
                transmission != null ||
                bodyType != null ||
                city != null ||
                color != null
    }
}

enum class VehicleSort(val label: String) {
    NEWEST("Yeni İlanlar"),
    OLDEST("Eski İlanlar"),
    PRICE_ASC("En Düşük Fiyat"),
    PRICE_DESC("En Yüksek Fiyat"),
    KM_ASC("En Düşük KM")
}
