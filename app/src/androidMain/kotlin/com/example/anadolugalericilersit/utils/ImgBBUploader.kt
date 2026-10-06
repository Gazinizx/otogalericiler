package com.example.anadolugalericilersit.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ImgBBUploader {

    private const val API_KEY = "6d207e02198a847aa98d0a2a901485a5"

    suspend fun uploadImageBytes(bytes: ByteArray): String? = withContext(Dispatchers.IO) {
        if (bytes.isEmpty()) return@withContext null
        try {
            val boundary = "----ImgBBBnd" + UUID.randomUUID().toString().take(12)
            val url = URL("https://api.imgbb.com/1/upload?key=$API_KEY")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.doInput = true
            conn.useCaches = false
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val dos = DataOutputStream(conn.outputStream)

            // Form data part: image
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n")
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
                val dataObj = json.optJSONObject("data")
                val imageUrl = dataObj?.optString("url", "") ?: ""
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    return@withContext imageUrl
                }
            } else {
                val errorStr = conn.errorStream?.bufferedReader()?.use { it.readText() }
                println("ImgBB Upload error ($responseCode): $errorStr")
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
