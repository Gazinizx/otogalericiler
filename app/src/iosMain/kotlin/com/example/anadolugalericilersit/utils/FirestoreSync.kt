package com.example.anadolugalericilersit.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.Foundation.NSURL
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object FirestoreSync {
    private const val PROJECT_ID = "galeri-6fa72"
    private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/$PROJECT_ID/databases/(default)/documents"

    fun patchDocument(collection: String, documentId: String, fieldsJson: String) {
        try {
            val urlString = "$BASE_URL/$collection/$documentId"
            val url = NSURL.URLWithString(urlString) ?: return
            val request = NSMutableURLRequest.requestWithURL(url)
            request.httpMethod = "PATCH"
            request.setValue("application/json", forHTTPHeaderField = "Content-Type")
            
            val body = "{ \"fields\": $fieldsJson }"
            val bytes = body.encodeToByteArray()
            val data = memScoped {
                NSData.create(bytes = bytes.refTo(0).getPointer(this), length = bytes.size.toULong())
            }
            request.httpBody = data

            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { _, _, _ -> }
            task.resume()
        } catch (_: Exception) {}
    }

    fun deleteDocument(collection: String, documentId: String) {
        try {
            val urlString = "$BASE_URL/$collection/$documentId"
            val url = NSURL.URLWithString(urlString) ?: return
            val request = NSMutableURLRequest.requestWithURL(url)
            request.httpMethod = "DELETE"

            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { _, _, _ -> }
            task.resume()
        } catch (_: Exception) {}
    }
}
