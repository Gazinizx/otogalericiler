package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

actual class ReportRepository actual constructor() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    actual suspend fun createReport(report: Report): Resource<Unit> {
        return submitReport(report)
    }

    actual suspend fun submitReport(report: Report): Resource<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newReport = report.copy(id = id, createdAt = System.currentTimeMillis())
            firestore.collection("reports").document(id).set(newReport).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayet bildirilemedi")
        }
    }

    actual suspend fun getReports(): Resource<List<Report>> {
        return getAllReports()
    }

    actual suspend fun getAllReports(): Resource<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports").get().await()
            val reports = snapshot.toObjects(Report::class.java).sortedByDescending { it.createdAt }
            Resource.Success(reports)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayetler alınamadı")
        }
    }

    actual suspend fun resolveReport(reportId: String): Resource<Unit> {
        return try {
            firestore.collection("reports").document(reportId).update("isResolved", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayet güncellenemedi")
        }
    }
}
