package com.example.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {
    val isInitialized: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (e: IllegalStateException) {
            false
        }

    val auth: FirebaseAuth?
        get() = if (isInitialized) FirebaseAuth.getInstance() else null

    val firestore: FirebaseFirestore?
        get() = if (isInitialized) FirebaseFirestore.getInstance() else null
}
