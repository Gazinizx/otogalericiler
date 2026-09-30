package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.Report
import com.example.anadolugalericilersit.utils.Resource

expect class ReportRepository() {
    suspend fun createReport(report: Report): Resource<Unit>
    suspend fun submitReport(report: Report): Resource<Unit>
    suspend fun getReports(): Resource<List<Report>>
    suspend fun getAllReports(): Resource<List<Report>>
    suspend fun resolveReport(reportId: String): Resource<Unit>
}
