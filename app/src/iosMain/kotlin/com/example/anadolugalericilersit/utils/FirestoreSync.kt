package com.example.anadolugalericilersit.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(BetaInteropApi::class)
object FirestoreSync {
    private const val PROJECT_ID = "galeri-6fa72"
    private const val DOCUMENTS_URL =
        "https://firestore.googleapis.com/v1/projects/" + PROJECT_ID + "/databases/(default)/documents"

    suspend fun patchDocument(collection: String, documentId: String, fieldsJson: String) {
        require(collection.matches(Regex("[A-Za-z0-9_-]+"))) { "Geçersiz Firestore koleksiyonu" }
        require(documentId.isNotBlank()) { "Firestore belge kimliği boş olamaz" }

        val fieldNames = Regex("""(?m)^\s*"([^"]+)"\s*:""")
            .findAll(fieldsJson)
            .map { it.groupValues[1] }
            .distinct()
            .toList()
        require(fieldNames.isNotEmpty()) { "Kaydedilecek Firestore alanı bulunamadı" }

        val encodedId = encodePathSegment(documentId)
        val mask = fieldNames.joinToString("&") { "updateMask.fieldPaths=" + encodeQueryValue(it) }
        val url = NSURL.URLWithString(DOCUMENTS_URL + "/" + collection + "/" + encodedId + "?" + mask)
            ?: throw IllegalArgumentException("Firestore URL oluşturulamadı")
        val request = NSMutableURLRequest.requestWithURL(url)
        request.HTTPMethod = "PATCH"
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField = "Content-Type")
        val body = """{"fields":$fieldsJson}"""
        request.HTTPBody = (body as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            ?: throw IllegalStateException("Firestore isteği kodlanamadı")
        execute(request, "Firestore kaydı başarısız")
    }

    suspend fun deleteDocument(collection: String, documentId: String) {
        require(collection.matches(Regex("[A-Za-z0-9_-]+"))) { "Geçersiz Firestore koleksiyonu" }
        require(documentId.isNotBlank()) { "Firestore belge kimliği boş olamaz" }
        val url = NSURL.URLWithString(DOCUMENTS_URL + "/" + collection + "/" + encodePathSegment(documentId))
            ?: throw IllegalArgumentException("Firestore URL oluşturulamadı")
        val request = NSMutableURLRequest.requestWithURL(url)
        request.HTTPMethod = "DELETE"
        execute(request, "Firestore silme işlemi başarısız")
    }

    private fun encodePathSegment(value: String): String =
        value.replace("%", "%25").replace("/", "%2F").replace(" ", "%20")

    private fun encodeQueryValue(value: String): String =
        value.replace("%", "%25").replace(" ", "%20").replace("/", "%2F")

    private suspend fun execute(request: NSMutableURLRequest, fallbackMessage: String) =
        suspendCancellableCoroutine<Unit> { continuation ->
            val task = NSURLSession.sharedSession.dataTaskWithRequest(
                request,
                completionHandler = { _: NSData?, response: NSURLResponse?, error: NSError? ->
                    if (continuation.isCancelled) return@dataTaskWithRequest
                    if (error != null) {
                        continuation.resumeWithException(
                            IllegalStateException(error.localizedDescription ?: fallbackMessage)
                        )
                        return@dataTaskWithRequest
                    }
                    val status = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
                    if (status in 200..299) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("$fallbackMessage (HTTP $status)")
                        )
                    }
                }
            )
            continuation.invokeOnCancellation { task.cancel() }
            task.resume()
        }
}