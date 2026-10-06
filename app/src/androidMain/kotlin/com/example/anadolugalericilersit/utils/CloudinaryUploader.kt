package com.example.anadolugalericilersit.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object CloudinaryUploader {

    var cloudName: String = "dgaleri"
    var uploadPreset: String = "anadolu_preset"

    suspend fun uploadImageBytes(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        if (bytes.isEmpty()) return@withContext null
        try {
            val boundary = "----CloudinaryBoundary" + UUID.randomUUID().toString()
            val url = URL("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.doInput = true
            conn.useCaches = false
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val dos = DataOutputStream(conn.outputStream)

            // Part 1: upload_preset
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            dos.writeBytes("$uploadPreset\r\n")

            // Part 2: file
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n")
            dos.writeBytes("Content-Type: image/jpeg\r\n\r\n")
            dos.write(bytes)
            dos.writeBytes("\r\n")

            // End boundary
            dos.writeBytes("--$boundary--\r\n")
            dos.flush()
            dos.close()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream: InputStream = conn.inputStream
                val responseStr = inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                val secureUrl = json.optString("secure_url", "")
                if (secureUrl.isNotBlank()) return@withContext secureUrl
            } else {
                val errorStr = conn.errorStream?.bufferedReader()?.use { it.readText() }
                println("Cloudinary upload error ($responseCode): $errorStr")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
