package com.example.anadolugalericilersit.utils

object SecurityRules {

    val FIRESTORE_RULES = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            
            // Helper functions
            function isAuthenticated() {
              return request.auth != null;
            }
            
            function isOwner(userId) {
              return isAuthenticated() && request.auth.uid == userId;
            }
            
            function isAdmin() {
              return isAuthenticated() && 
                (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'SUPER_ADMIN']);
            }
            
            // Users collection
            match /users/{userId} {
              allow read: if isAuthenticated();
              allow create, update: if isOwner(userId) || isAdmin();
              allow delete: if isAdmin();
            }
            
            // Dealers collection
            match /dealers/{dealerId} {
              allow read: if true; // Publicly readable
              allow create: if isAuthenticated();
              allow update: if isAuthenticated() && (resource.data.ownerUid == request.auth.uid || isAdmin());
              allow delete: if isAdmin();
            }
            
            // Vehicles collection
            match /vehicles/{vehicleId} {
              allow read: if true; // Publicly readable
              allow create: if isAuthenticated();
              allow update: if isAuthenticated() && (resource.data.dealerId == request.auth.uid || isAdmin());
              allow delete: if isAuthenticated() && (resource.data.dealerId == request.auth.uid || isAdmin());
            }
            
            // Favorites
            match /favorites/{favoriteId} {
              allow read, write: if isAuthenticated();
            }
            
            // Reports
            match /reports/{reportId} {
              allow create: if true;
              allow read, update, delete: if isAdmin();
            }
            
            // Notifications
            match /notifications/{notificationId} {
              allow read, update: if isAuthenticated() && resource.data.userId == request.auth.uid;
              allow create, delete: if isAdmin();
            }
          }
        }
    """.trimIndent()

    val STORAGE_RULES = """
        rules_version = '2';
        service firebase.storage {
          match /b/{bucket}/o {
            
            match /vehicle_images/{imageId} {
              allow read: if true;
              allow write: if request.auth != null 
                && request.resource.size < 10 * 1024 * 1024 // 10MB
                && request.resource.contentType.matches('image/.*');
            }
            
            match /vehicle_videos/{videoId} {
              allow read: if true;
              allow write: if request.auth != null 
                && request.resource.size < 50 * 1024 * 1024 // 50MB
                && request.resource.contentType.matches('video/.*');
            }
            
            match /dealer_logos/{logoId} {
              allow read: if true;
              allow write: if request.auth != null;
            }
          }
        }
    """.trimIndent()
}
