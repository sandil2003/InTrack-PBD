package com.example.intrack.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.persistentCacheSettings

object FirestoreService {
    val db: FirebaseFirestore by lazy {
        val instance = FirebaseFirestore.getInstance()
        try {
            val settings = com.google.firebase.firestore.firestoreSettings {
                setLocalCacheSettings(persistentCacheSettings { })
            }
            instance.firestoreSettings = settings
        } catch (_: Exception) {
            // Settings already initialized
        }
        instance
    }
}
