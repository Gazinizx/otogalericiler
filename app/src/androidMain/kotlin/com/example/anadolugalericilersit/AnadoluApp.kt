package com.example.anadolugalericilersit

import android.app.Application
import com.google.firebase.FirebaseApp

class AnadoluApp : Application() {

    companion object {
        lateinit var instance: AnadoluApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Prevent startup crash if google-services.json is missing or network is offline
        }
    }
}
