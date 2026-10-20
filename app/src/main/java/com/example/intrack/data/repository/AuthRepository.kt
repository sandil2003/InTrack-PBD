package com.example.intrack.data.repository

import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signInAnonymously(): Result<FirebaseUser>
    fun signOut()
}

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirestoreService.db
) : AuthRepository {
    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Authentication returned empty user session")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Account creation failed")

            // 1. Update display name in Firebase Auth profile (defensive)
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (_: Exception) {
                // If profile update encounters network lag, user auth session is still valid
            }

            // 2. Initialize user document in Cloud Firestore (defensive)
            val userProfile = hashMapOf(
                "displayName" to name.trim(),
                "email" to email.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                db.collection("users").document(user.uid).set(userProfile).await()
            } catch (_: Exception) {
                // If offline or rule initialization, auth user session remains valid
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("User is null after sign in")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        auth.signOut()
    }
}
