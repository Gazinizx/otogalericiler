package com.example.anadolugalericilersit.utils

object Constants {

    val CITIES = listOf(
        "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin",
        "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa",
        "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan",
        "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta",
        "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir",
        "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla",
        "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt",
        "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak",
        "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman",
        "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce"
    )

    val BRANDS_WITH_MODELS = mapOf(
        "Audi" to listOf("A1", "A3", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q5", "Q7", "Q8", "e-tron"),
        "BMW" to listOf("1 Serisi", "2 Serisi", "3 Serisi", "4 Serisi", "5 Serisi", "7 Serisi", "X1", "X3", "X5", "X6", "i4", "iX"),
        "Citroen" to listOf("C3", "C3 Aircross", "C4", "C4X", "C5 Aircross", "Berlingo"),
        "Fiat" to listOf("Egea", "Egea Cross", "Panda", "500", "500X", "Fiorino", "Doblo"),
        "Ford" to listOf("Fiesta", "Focus", "Mondeo", "Puma", "Kuga", "Tourneo Courier", "Transit"),
        "Honda" to listOf("Civic", "City", "Accord", "HR-V", "CR-V", "Jazz"),
        "Hyundai" to listOf("i10", "i20", "i30", "Elantra", "Bayon", "Tucson", "Santa Fe", "Ioniq 5"),
        "Kia" to listOf("Picanto", "Rio", "Ceed", "Stonic", "XCeed", "Sportage", "Sorento", "EV6"),
        "Mercedes-Benz" to listOf("A-Serisi", "B-Serisi", "C-Serisi", "E-Serisi", "S-Serisi", "CLA", "GLA", "GLC", "GLE", "EQE"),
        "Nissan" to listOf("Micra", "Juke", "Qashqai", "X-Trail", "Navara"),
        "Opel" to listOf("Corsa", "Astra", "Insignia", "Mokka", "Crossland", "Grandland", "Combo"),
        "Peugeot" to listOf("208", "308", "408", "508", "2008", "3008", "5008", "Rifter"),
        "Renault" to listOf("Clio", "Megane", "Taliant", "Captur", "Austral", "Kadjar", "Koleos", "Kangoo"),
        "Seat" to listOf("Ibiza", "Leon", "Arona", "Ateca", "Tarraco"),
        "Skoda" to listOf("Fabia", "Scala", "Octavia", "Superb", "Kamiq", "Karoq", "Kodiaq"),
        "Toyota" to listOf("Yaris", "Yaris Cross", "Corolla", "Corolla Cross", "C-HR", "RAV4", "Hilux"),
        "Volkswagen" to listOf("Polo", "Golf", "Passat", "Passat Variant", "Arteon", "T-Cross", "Taigo", "T-Roc", "Tiguan", "Touareg", "Caddy"),
        "Volvo" to listOf("S60", "S90", "V60", "XC40", "XC60", "XC90")
    )

    val FUEL_TYPES = listOf("Benzin", "Dizel", "LPG & Benzin", "Hibrit", "Elektrik")

    val TRANSMISSIONS = listOf("Manuel", "Otomatik", "Yarı Otomatik")

    val BODY_TYPES = listOf("Sedan", "Hatchback", "SUV / Crossover", "Station Wagon", "Coupe", "Cabrio", "MPV", "Pick-up", "Minibüs / Panelvan")

    val COLORS = listOf("Beyaz", "Siyah", "Gri", "Gümüş Gri", "Füme", "Kırmızı", "Mavi", "Lacivert", "Yeşil", "Sarı", "Turuncu", "Kahverengi")

    val EQUIPMENT_OPTIONS = listOf(
        "Sunroof",
        "Panoramik Cam Tavan",
        "Deri Döşeme",
        "Isıtmalı Ön Koltuklar",
        "Isıtmalı Arka Koltuklar",
        "Soğutmalı Ön Koltuklar",
        "Elektrikli Hafızalı Koltuklar",
        "Adaptif Hız Sabitleyici (ACC)",
        "Şerit Takip Asistanı",
        "Kör Nokta Uyarı Sistemi",
        "Geri Görüş Kamerası",
        "360 Derece Kamera",
        "Ön ve Arka Park Sensörü",
        "Otonom Park Asistanı",
        "Head-Up Display",
        "Apple CarPlay",
        "Android Auto",
        "Kablosuz Şarj",
        "Hayalet Gösterge Paneli",
        "Matrix LED Far",
        "Xenon / Bi-Xenon Far",
        "Adaptif Far Sistemi",
        "Navigasyon",
        "Çift Bölgeli Dijital Klima",
        "Üç Bölge Otomatik Klima",
        "Anahtarsız Giriş ve Çalıştırma",
        "Elektrikli Bagaj Kapağı",
        "Ambiyans Aydınlatması",
        "Çarpışma Önleme Sistemi",
        "Yokuş Kalkış Desteği",
        "Start & Stop"
    )

    val BODY_PARTS = listOf(
        "Motor Kaputu",
        "Tavan",
        "Bagaj Kapağı",
        "Sol Ön Çamurluk",
        "Sağ Ön Çamurluk",
        "Sol Ön Kapı",
        "Sağ Ön Kapı",
        "Sol Arka Kapı",
        "Sağ Arka Kapı",
        "Sol Arka Çamurluk",
        "Sağ Arka Çamurluk",
        "Ön Tampon",
        "Arka Tampon"
    )

    val BODY_PART_CONDITIONS = listOf(
        "Orijinal",
        "Lokal Boyalı",
        "Boyalı",
        "Değişen",
        "Vernik / Plastik"
    )
}

