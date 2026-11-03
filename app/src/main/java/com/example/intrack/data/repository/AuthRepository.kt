package com.example.intrack.data.repository

import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun getCurrentUserId(): String = getCurrentUser()?.uid ?: "guest_user"
    fun getCurrentUserDisplayName(): String =
        getCurrentUser()?.displayName?.takeIf { it.isNotBlank() } ?: "Sandith Sithmaka"
    fun getCurrentUserEmail(): String =
        getCurrentUser()?.email?.takeIf { it.isNotBlank() } ?: "sandith@intrack.lk"
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signInAnonymously(): Result<FirebaseUser>
    fun signOut()
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    constructor() : this(FirebaseAuth.getInstance(), FirestoreService.db)

    override fun getCurrentUser(): FirebaseUser? = try {
        auth.currentUser
    } catch (_: Exception) {
        null
    }

    override fun getCurrentUserId(): String = getCurrentUser()?.uid ?: "guest_user"

    override fun getCurrentUserDisplayName(): String =
        getCurrentUser()?.displayName?.takeIf { it.isNotBlank() } ?: "Sandith Sithmaka"

    override fun getCurrentUserEmail(): String =
        getCurrentUser()?.email?.takeIf { it.isNotBlank() } ?: "sandith@intrack.lk"

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
                kotlinx.coroutines.withTimeoutOrNull(4000L) {
                    db.collection("users").document(user.uid).set(userProfile).await()
                }
            } catch (_: Exception) {
                // If offline or rule initialization, auth user session remains valid
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google authentication returned empty user session")

            // Initialize or merge user document in Cloud Firestore
            val userProfile = hashMapOf(
                "displayName" to (user.displayName ?: "User"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                kotlinx.coroutines.withTimeoutOrNull(4000L) {
                    db.collection("users").document(user.uid).set(userProfile, SetOptions.merge()).await()
                }
            } catch (_: Exception) {
                // Ignore Firestore sync errors so auth session succeeds even offline
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
        try {
            auth.signOut()
        } catch (_: Exception) {
        }
    }
}
