package com.example.anadolugalericilersit.data.seeder

import com.example.anadolugalericilersit.utils.Resource

object DataSeeder {

    fun seedInitialDataIfNeeded(): Resource<String> {
        return Resource.Success("Sistem güncel.")
    }
}
