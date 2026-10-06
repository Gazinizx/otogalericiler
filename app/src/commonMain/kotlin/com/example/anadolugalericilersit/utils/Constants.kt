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
        "Alfa Romeo" to listOf("147", "156", "159", "Giulia", "Giulietta", "Stelvio", "Tonale", "MiTo"),
        "Aston Martin" to listOf("DB9", "DB11", "DBS", "Vantage", "DBX"),
        "Audi" to listOf("A1", "A3", "A3 Sedan", "A4", "A5", "A6", "A7", "A8", "Q2", "Q3", "Q4 e-tron", "Q5", "Q7", "Q8", "e-tron", "TT", "R8"),
        "Bentley" to listOf("Continental GT", "Flying Spur", "Bentayga"),
        "BMW" to listOf("1 Serisi", "2 Serisi", "2 Serisi Gran Coupe", "3 Serisi", "4 Serisi", "5 Serisi", "6 Serisi", "7 Serisi", "8 Serisi", "X1", "X2", "X3", "X4", "X5", "X6", "X7", "i3", "i4", "i7", "iX", "iX3", "Z4"),
        "Bugatti" to listOf("Chiron", "Veyron"),
        "Buick" to listOf("Encore", "Regal"),
        "Cadillac" to listOf("Escalade", "CTS", "ATS"),
        "Chery" to listOf("Omoda 5", "Tiggo 7 Pro", "Tiggo 8 Pro", "Arrizo"),
        "Chevrolet" to listOf("Aveo", "Cruze", "Captiva", "Camaro", "Corvette", "Kalos", "Lacetti", "Spark", "Tahoe", "Trax"),
        "Chrysler" to listOf("300C", "Grand Voyager", "Sebring"),
        "Citroen" to listOf("C-Elysée", "C3", "C3 Aircross", "C4", "C4 Cactus", "C4 Grand Picasso", "C4 Picassso", "C4X", "C5", "C5 Aircross", "C5 X", "Ami", "Berlingo", "Jumper", "Jumpy"),
        "Cupra" to listOf("Formentor", "Leon", "Ateca", "Born", "Tavascan"),
        "Dacia" to listOf("Duster", "Sandero", "Sandero Stepway", "Logan", "Lodgy", "Dokker", "Jogger", "Spring"),
        "Daewoo" to listOf("Lanos", "Matiz", "Nexia", "Nubira"),
        "Daihatsu" to listOf("Cuore", "Materia", "Sirion", "Terios"),
        "Dodge" to listOf("Challenger", "Charger", "Durango", "Nitro", "RAM"),
        "DS Automobiles" to listOf("DS 3", "DS 3 Crossback", "DS 4", "DS 7", "DS 7 Crossback", "DS 9"),
        "Ferrari" to listOf("296 GTB", "458", "488", "812", "F8", "Portofino", "Purosangue", "Roma"),
        "Fiat" to listOf("500", "500C", "500L", "500X", "Egea", "Egea Cross", "Egea Hatchback", "Egea Station Wagon", "Linea", "Panda", "Punto", "Tipo", "Fiorino", "Doblo", "Ducato", "Scudo"),
        "Ford" to listOf("B-Max", "C-Max", "Fiesta", "Focus", "Fusion", "Grand C-Max", "Ka", "Mondeo", "Mustang", "Mustang Mach-E", "Puma", "Kuga", "Ranger", "Tourneo Courier", "Tourneo Connect", "Tourneo Custom", "Transit", "Transit Courier", "Transit Custom"),
        "Geely" to listOf("Emgrand", "Geometry C", "E5"),
        "Honda" to listOf("Accord", "City", "Civic", "CR-V", "CR-Z", "HR-V", "Insight", "Jazz", "ZR-V"),
        "Hyundai" to listOf("Accent", "Accent Blue", "Accent Era", "Bayon", "Elantra", "Getz", "i10", "i20", "i20 N", "i30", "Ioniq 5", "Ioniq 6", "Kona", "Santa Fe", "Tucson"),
        "Infiniti" to listOf("FX", "Q30", "Q50", "QX70"),
        "Isuzu" to listOf("D-Max"),
        "Jaguar" to listOf("E-Pace", "F-Pace", "F-Type", "I-Pace", "XE", "XF", "XJ"),
        "Jeep" to listOf("Avenger", "Compass", "Grand Cherokee", "Renegade", "Wrangler"),
        "Kia" to listOf("Ceed", "Ceed SW", "Cerato", "EV6", "EV9", "Picanto", "ProCeed", "Rio", "Sorento", "Soul", "Sportage", "Stonic", "XCeed"),
        "Lada" to listOf("Niva", "Samara", "Vega"),
        "Lamborghini" to listOf("Aventador", "Huracan", "Urus", "Revuelto"),
        "Lancia" to listOf("Delta", "Ypsilon"),
        "Land Rover" to listOf("Defender", "Discovery", "Discovery Sport", "Freelander", "Range Rover", "Range Rover Evoque", "Range Rover Velar", "Range Rover Sport"),
        "Lexus" to listOf("CT", "ES", "LBX", "LS", "NX", "RX", "UX"),
        "Lincoln" to listOf("Aviator", "Navigator"),
        "Maserati" to listOf("Ghibli", "Grecale", "Levante", "Quattroporte"),
        "Mazda" to listOf("2", "3", "6", "CX-3", "CX-30", "CX-5", "MX-5"),
        "McLaren" to listOf("720S", "Artura", "GT"),
        "Mercedes-Benz" to listOf("A-Serisi", "B-Serisi", "C-Serisi", "E-Serisi", "S-Serisi", "CLA", "CLS", "EQA", "EQB", "EQC", "EQE", "EQS", "GLA", "GLB", "GLC", "GLE", "GLS", "G-Serisi", "SLC", "SLK", "Citan", "Vito", "Sprinter"),
        "MG" to listOf("HS", "EHS", "ZS", "ZS EV", "MG4", "Cyberster"),
        "MINI" to listOf("Cooper", "Cooper Clubman", "Cooper Countryman", "One"),
        "Mitsubishi" to listOf("ASX", "Attrage", "Eclipse Cross", "L200", "Lancer", "Outlander", "Pajero", "Space Star"),
        "Nissan" to listOf("Ariya", "Juke", "Micra", "Navara", "Note", "Pulsar", "Qashqai", "X-Trail"),
        "Opel" to listOf("Adam", "Agila", "Astra", "Corsa", "Crossland", "Crossland X", "Grandland", "Grandland X", "Insignia", "Meriva", "Mokka", "Mokka X", "Vectra", "Zafira", "Combo", "Vivaro"),
        "Peugeot" to listOf("107", "206", "207", "208", "301", "307", "308", "407", "408", "508", "2008", "3008", "5008", "508 SW", "RCZ", "Rifter", "Partner", "Expert", "Boxer"),
        "Porsche" to listOf("718 Cayman", "718 Boxster", "911", "Taycan", "Panamera", "Macan", "Cayenne"),
        "RAM" to listOf("1500", "2500"),
        "Renault" to listOf("Austral", "Captur", "Clio", "Fluence", "Kadjar", "Kangoo", "Koleos", "Laguna", "Latitude", "Megane", "Megane E-Tech", "Modus", "Rafale", "Scenic", "Symbol", "Taliant", "Twingo", "Zoe", "Master", "Trafic"),
        "Rolls-Royce" to listOf("Cullinan", "Ghost", "Phantom", "Spectre"),
        "Rover" to listOf("25", "45", "75"),
        "Saab" to listOf("9-3", "9-5"),
        "Seat" to listOf("Alhambra", "Altea", "Arona", "Ateca", "Cordoba", "Ibiza", "Leon", "Tarraco", "Toledo"),
        "Skoda" to listOf("Citigo", "Fabia", "Kamiq", "Karoq", "Kodiaq", "Octavia", "Rapid", "Rapid Spaceback", "Roomster", "Scala", "Superb", "Yeti"),
        "Smart" to listOf("Fortwo", "Forfour", "#1", "#3"),
        "SsangYong / KGM" to listOf("Korando", "Korando e-Motion", "Musso Grand", "Rexton", "Tivoli", "Torres"),
        "Subaru" to listOf("BRZ", "Forester", "Impreza", "Levorg", "Outback", "XV", "Crosstrek"),
        "Suzuki" to listOf("Alto", "Baleno", "Ignis", "Jimny", "S-Cross", "Swace", "Swift", "SX4", "Vitara"),
        "Tesla" to listOf("Model 3", "Model S", "Model X", "Model Y", "Cybertruck"),
        "Tofaş" to listOf("Doğan", "Şahin", "Kartal", "Murat 124", "Murat 131"),
        "Togg" to listOf("T10X", "T10F"),
        "Toyota" to listOf("Auris", "Avensis", "Aygo", "C-HR", "Camry", "Corolla", "Corolla Cross", "GT86", "Hilux", "Land Cruiser", "Prius", "RAV4", "Verso", "Yaris", "Yaris Cross", "Proace City"),
        "Volkswagen" to listOf("Amarok", "Arteon", "Beetle", "Bora", "Caddy", "Crafter", "EOS", "Golf", "ID.3", "ID.4", "ID.5", "Jetta", "Lupo", "Passat", "Passat CC", "Passat Variant", "Polo", "Scirocco", "Sharan", "T-Cross", "T-Roc", "Taigo", "Tiguan", "Tiguan Allspace", "Touareg", "Touran", "Transporter", "Up!"),
        "Volvo" to listOf("C30", "C70", "EX30", "EX90", "S40", "S60", "S80", "S90", "V40", "V40 Cross Country", "V60", "V60 Cross Country", "V90", "XC40", "XC60", "XC90"),
        "Diğer" to listOf("Diğer Model")
    )

    val MODEL_PACKAGES = mapOf(
        "Passat" to listOf("Highline", "Elegance", "Comfortline", "Impression", "Trendline", "R-Line", "Business"),
        "Golf" to listOf("Highline", "R-Line", "Style", "Life", "Comfortline", "Midline Plus", "Impression"),
        "Polo" to listOf("Highline", "Style", "Life", "Comfortline", "Trendline", "Impression"),
        "Tiguan" to listOf("Highline", "R-Line", "Elegance", "Life", "Comfortline", "Trendline"),
        "Caddy" to listOf("Style", "Life", "Comfortline", "Trendline"),
        "3 Serisi" to listOf("M Sport", "Sport Line", "Luxury Line", "Joy", "First Edition", "Modern Line", "Comfort"),
        "5 Serisi" to listOf("M Sport", "Luxury Line", "Executive M Sport", "Exclusive", "Comfort", "Special Edition"),
        "1 Serisi" to listOf("M Sport", "Sport Line", "Urban Line", "Joy", "First Edition"),
        "C-Serisi" to listOf("AMG", "Exclusive", "Avantgarde", "Fascination", "Style"),
        "E-Serisi" to listOf("AMG", "Exclusive", "Avantgarde", "Edition E", "Style"),
        "CLA" to listOf("AMG", "AMG Line", "Urban", "Style"),
        "Egea" to listOf("Lounge", "Urban", "Easy", "Cross Lounge", "Cross Urban", "Cross Street", "Mirror"),
        "Linea" to listOf("Lounge", "Urban", "Pop", "Active Plus", "Emotion"),
        "Doblo" to listOf("Trekking", "Premio", "Safeline", "Elegance", "Urban", "Easy"),
        "Megane" to listOf("Icon", "Touch", "Joy", "GT Line", "Dynamique", "Expression"),
        "Clio" to listOf("Icon", "Touch", "Joy", "RS Line", "Authentique", "Extreme"),
        "Focus" to listOf("Titanium", "ST-Line", "Trend X", "Style", "Ghia", "Comfort"),
        "Fiesta" to listOf("Titanium", "ST-Line", "Trend", "Style", "MyFiesta"),
        "Corolla" to listOf("Passion X-Pack", "Flame X-Pack", "Vision", "Dream", "Touch", "Active"),
        "A3" to listOf("S Line", "Advanced", "Design", "Ambition", "Attraction", "Sport"),
        "A4" to listOf("S Line", "Design", "Design Luxury", "Advanced", "Dynamic"),
        "Civic" to listOf("Executive", "Elegance", "Premium", "Dream", "Eco Executive"),
        "Tucson" to listOf("Prime", "Elite", "Elite Plus", "N Line"),
        "i20" to listOf("Elite", "Style", "Jump", "N Line"),
        "3008" to listOf("GT", "Allure", "Active", "Active Prime"),
        "Astra" to listOf("GS", "Elegance", "Edition", "Design Line", "Enjoy"),
        "T10X" to listOf("V2 RWD Uzun Menzil", "V1 RWD Standart Menzil", "V2 RWD Standart Menzil"),
        "Tiggo 8 Pro" to listOf("Avangarde", "Luxury", "Excellence")
    )

    val FUEL_TYPES = listOf(
        "Benzin",
        "Dizel",
        "LPG & Benzin",
        "Hibrit (Hybrid)",
        "Plug-in Hybrid",
        "Mild Hybrid",
        "Elektrik"
    )

    val TRANSMISSIONS = listOf(
        "Manuel",
        "Otomatik",
        "Yarı Otomatik",
        "DSG / DCT (Çift Kavrama)",
        "CVT (Kademesiz)"
    )

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
