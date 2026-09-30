package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun submitReport(report: Report): Resource<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            val newReport = report.copy(id = id, createdAt = System.currentTimeMillis())
            firestore.collection("reports").document(id).set(newReport).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayet bildirilemedi")
        }
    }

    suspend fun getAllReports(): Resource<List<Report>> {
        return try {
            val snapshot = firestore.collection("reports").get().await()
            val reports = snapshot.toObjects(Report::class.java).sortedByDescending { it.createdAt }
            Resource.Success(reports)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayetler alınamadı")
        }
    }

    suspend fun resolveReport(reportId: String): Resource<Unit> {
        return try {
            firestore.collection("reports").document(reportId).update("isResolved", true).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Şikayet güncellenemedi")
        }
    }
}
