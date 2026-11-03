package com.example.intrack.data.repository

import com.example.intrack.data.model.UserProfile
import com.example.intrack.data.remote.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun getCurrentUserId(): String = getCurrentUser()?.uid ?: ""
    fun getCurrentUserDisplayName(): String {
        val user = getCurrentUser() ?: return ""
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: ""
    }
    fun getCurrentUserEmail(): String = getCurrentUser()?.email ?: ""
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    suspend fun updateUserPreferences(
        userId: String,
        baseCurrency: String,
        billingCycle: String,
        weeklyStartDay: String
    ): Result<Unit>
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser>
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
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

    override fun getCurrentUserId(): String = getCurrentUser()?.uid ?: ""

    override fun getCurrentUserDisplayName(): String {
        val user = getCurrentUser() ?: return ""
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: ""
    }

    override fun getCurrentUserEmail(): String = getCurrentUser()?.email ?: ""

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val fallbackUser = getCurrentUser()
                if (fallbackUser != null && fallbackUser.uid == userId) {
                    trySend(
                        UserProfile(
                            uid = userId,
                            displayName = getCurrentUserDisplayName(),
                            email = getCurrentUserEmail(),
                            photoUrl = fallbackUser.photoUrl?.toString() ?: ""
                        )
                    )
                }
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val rawName = snapshot.getString("displayName")?.takeIf { it.isNotBlank() }
                    ?: getCurrentUserDisplayName()
                val rawEmail = snapshot.getString("email")?.takeIf { it.isNotBlank() }
                    ?: getCurrentUserEmail()
                val profile = UserProfile(
                    uid = userId,
                    displayName = rawName,
                    email = rawEmail,
                    photoUrl = snapshot.getString("photoUrl") ?: (getCurrentUser()?.photoUrl?.toString() ?: ""),
                    baseCurrency = snapshot.getString("baseCurrency") ?: "LKR (Rs) • Sri Lankan Rupee",
                    billingCycle = snapshot.getString("billingCycle") ?: "25th of Every Month",
                    weeklyStartDay = snapshot.getString("weeklyStartDay") ?: "Monday",
                    createdAtMillis = snapshot.getLong("createdAtMillis") ?: System.currentTimeMillis(),
                    updatedAtMillis = snapshot.getLong("updatedAtMillis") ?: System.currentTimeMillis()
                )
                trySend(profile)
            } else {
                val currentUser = getCurrentUser()
                val initialProfile = UserProfile(
                    uid = userId,
                    displayName = getCurrentUserDisplayName(),
                    email = getCurrentUserEmail(),
                    photoUrl = currentUser?.photoUrl?.toString() ?: ""
                )
                trySend(initialProfile)
                try {
                    docRef.set(initialProfile, SetOptions.merge())
                } catch (_: Exception) {
                }
            }
        }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun updateUserPreferences(
        userId: String,
        baseCurrency: String,
        billingCycle: String,
        weeklyStartDay: String
    ): Result<Unit> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User not authenticated"))
        return try {
            val updates = mapOf(
                "baseCurrency" to baseCurrency,
                "billingCycle" to billingCycle,
                "weeklyStartDay" to weeklyStartDay,
                "updatedAtMillis" to System.currentTimeMillis()
            )
            db.collection("users").document(userId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Authentication returned empty user session")

            // Ensure Firestore user document is synchronized on sign-in
            try {
                withTimeoutOrNull(4000L) {
                    val docRef = db.collection("users").document(user.uid)
                    val snapshot = docRef.get().await()
                    if (!snapshot.exists()) {
                        val derivedName = user.displayName?.takeIf { it.isNotBlank() }
                            ?: email.trim().substringBefore("@").replaceFirstChar { it.uppercase() }
                        val newProfile = UserProfile(
                            uid = user.uid,
                            displayName = derivedName,
                            email = user.email ?: email.trim()
                        )
                        docRef.set(newProfile, SetOptions.merge()).await()
                    }
                }
            } catch (_: Exception) {
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw Exception("Account creation failed")

            // 1. Update display name in Firebase Auth profile
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            } catch (_: Exception) {
            }

            // 2. Initialize user profile document in Cloud Firestore
            val now = System.currentTimeMillis()
            val userProfile = UserProfile(
                uid = user.uid,
                displayName = name.trim(),
                email = email.trim(),
                createdAtMillis = now,
                updatedAtMillis = now
            )
            try {
                withTimeoutOrNull(4000L) {
                    db.collection("users").document(user.uid).set(userProfile).await()
                }
            } catch (_: Exception) {
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
            val derivedName = user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: ""
            val userProfileUpdates = hashMapOf<String, Any>(
                "uid" to user.uid,
                "displayName" to derivedName,
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAtMillis" to System.currentTimeMillis()
            )
            try {
                withTimeoutOrNull(4000L) {
                    db.collection("users").document(user.uid).set(userProfileUpdates, SetOptions.merge()).await()
                }
            } catch (_: Exception) {
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

    override fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {
        }
    }
}
