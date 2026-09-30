package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.local.LocalStore
import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.utils.Resource

actual class ReportRepository actual constructor() {
    actual suspend fun createReport(report: Report): Resource<Unit> {
        return submitReport(report)
    }

    actual suspend fun submitReport(report: Report): Resource<Unit> {
        LocalStore.reports.add(0, report)
        return Resource.Success(Unit)
    }

    actual suspend fun getReports(): Resource<List<Report>> {
        return getAllReports()
    }

    actual suspend fun getAllReports(): Resource<List<Report>> {
        return Resource.Success(LocalStore.reports.toList())
    }

    actual suspend fun resolveReport(reportId: String): Resource<Unit> {
        val idx = LocalStore.reports.indexOfFirst { it.id == reportId }
        if (idx != -1) {
            LocalStore.reports[idx] = LocalStore.reports[idx].copy(isResolved = true)
        }
        return Resource.Success(Unit)
    }
}
