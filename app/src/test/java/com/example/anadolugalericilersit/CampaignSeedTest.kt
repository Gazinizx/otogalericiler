package com.example.anadolugalericilersit

import com.example.anadolugalericilersit.data.model.NotificationItem
import com.example.anadolugalericilersit.data.repository.NotificationRepository
import com.example.anadolugalericilersit.utils.ImgBBUploader
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File

class CampaignSeedTest {

    @Test
    fun seedCampaigns() = runBlocking {
        val repo = NotificationRepository()

        val file1 = File("src/main/res/drawable/damga_hediye_firsati.png")
        val file2 = File("src/main/res/drawable/dpf_temizligi_ecu.png")

        var url1 = "https://i.ibb.co/sample1.png"
        var url2 = "https://i.ibb.co/sample2.png"

        try {
            if (file1.exists()) {
                val bytes1 = file1.readBytes()
                val uploaded1 = ImgBBUploader.uploadImageBytes(bytes1)
                if (!uploaded1.isNullOrBlank()) url1 = uploaded1
            }
            if (file2.exists()) {
                val bytes2 = file2.readBytes()
                val uploaded2 = ImgBBUploader.uploadImageBytes(bytes2)
                if (!uploaded2.isNullOrBlank()) url2 = uploaded2
            }
        } catch (_: Exception) {}

        repo.sendNotification(
            NotificationItem(
                userId = "all",
                title = "1",
                message = "6 Damga, Büyük Hediye Fırsatı",
                imageUrl = url1,
                type = "ANNOUNCEMENT"
            )
        )

        repo.sendNotification(
            NotificationItem(
                userId = "all",
                title = "2",
                message = "DPF Temizliği ve Hediye ECU Kontrolü",
                imageUrl = url2,
                type = "ANNOUNCEMENT"
            )
        )
    }
}
