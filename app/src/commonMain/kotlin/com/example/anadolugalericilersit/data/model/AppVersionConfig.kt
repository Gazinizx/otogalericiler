package com.example.anadolugalericilersit.data.model

data class AppVersionConfig(
    val minVersionCode: Int = 1,
    val latestVersionCode: Int = 1,
    val latestVersionName: String = "1.0",
    val updateTitle: String = "Yeni Güncelleme Mevcut!",
    val updateMessage: String = "Anadolu Galericiler Sitesi uygulamasının yeni sürümü yayınlandı. Lütfen devam etmek için güncelleyiniz.",
    val apkUrl: String = "https://github.com",
    val isForceUpdate: Boolean = false
)
