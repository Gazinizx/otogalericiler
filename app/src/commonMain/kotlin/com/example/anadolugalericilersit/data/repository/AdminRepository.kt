package com.example.anadolugalericilersit.data.repository

import com.example.anadolugalericilersit.data.model.AdminStats
import com.example.anadolugalericilersit.utils.Resource

expect class AdminRepository() {
    suspend fun getAdminStats(): Resource<AdminStats>
}
