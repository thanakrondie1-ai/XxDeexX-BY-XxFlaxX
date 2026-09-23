package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.data.model.CreationItem
import com.example.data.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseService(private val context: Context) {

    private val isFirebaseAvailable: Boolean by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context) != null
            } else {
                true
            }
        } catch (e: Throwable) {
            Log.w("FirebaseService", "FirebaseApp init unavailable: ${e.message}")
            false
        }
    }

    private val auth: FirebaseAuth? by lazy {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                Log.w("FirebaseService", "FirebaseAuth unavailable: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Throwable) {
                Log.w("FirebaseService", "FirebaseFirestore unavailable: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "guest_user",
            email = null,
            displayName = "Guest Explorer",
            photoUrl = null,
            isAnonymous = true
        )
    )
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    // Local in-memory store fallback for offline/emulator environments
    private val localCreations = mutableListOf<CreationItem>()

    init {
        try {
            val currentAuth = auth
            if (currentAuth != null) {
                updateProfile(currentAuth.currentUser)
                currentAuth.addAuthStateListener { firebaseAuth ->
                    updateProfile(firebaseAuth.currentUser)
                }
            }
        } catch (e: Throwable) {
            Log.w("FirebaseService", "Firebase Auth init fallback: ${e.message}")
        }
    }

    private fun updateProfile(user: FirebaseUser?) {
        _currentUserProfile.value = if (user != null) {
            UserProfile(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: if (user.isAnonymous) "Guest Explorer" else "XxFlaxX User",
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } else {
            UserProfile(
                uid = "guest_user",
                email = null,
                displayName = "Guest Explorer",
                photoUrl = null,
                isAnonymous = true
            )
        }
    }

    suspend fun signInAnonymously(): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth
        if (currentAuth == null) {
            val guest = UserProfile(
                uid = "guest_user",
                email = null,
                displayName = "Guest Explorer",
                photoUrl = null,
                isAnonymous = true
            )
            _currentUserProfile.value = guest
            return@withContext Result.success(guest)
        }

        try {
            val result = currentAuth.signInAnonymously().await()
            val user = result.user
            updateProfile(user)
            if (user != null) {
                Result.success(
                    UserProfile(
                        uid = user.uid,
                        email = null,
                        displayName = "Guest Explorer",
                        photoUrl = null,
                        isAnonymous = true
                    )
                )
            } else {
                Result.failure(Exception("Failed to obtain guest session"))
            }
        } catch (e: Throwable) {
            Log.e("FirebaseService", "Anonymous sign in failed, fallback to local guest", e)
            val fallback = UserProfile(
                uid = "guest_user",
                email = null,
                displayName = "Guest Explorer",
                photoUrl = null,
                isAnonymous = true
            )
            _currentUserProfile.value = fallback
            Result.success(fallback)
        }
    }

    suspend fun signInWithGoogleCredential(
        activityContext: Context,
        serverClientId: String = ""
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        val currentAuth = auth
        if (currentAuth == null) {
            return@withContext signInAnonymously()
        }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    if (serverClientId.isNotEmpty()) serverClientId else "default-client-id"
                )
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = currentAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                updateProfile(user)
                if (user != null) {
                    Result.success(
                        UserProfile(
                            uid = user.uid,
                            email = user.email,
                            displayName = user.displayName,
                            photoUrl = user.photoUrl?.toString(),
                            isAnonymous = false
                        )
                    )
                } else {
                    Result.failure(Exception("Failed to authenticate Google user"))
                }
            } else {
                Result.failure(Exception("Unexpected credential type returned"))
            }
        } catch (e: Throwable) {
            Log.w("FirebaseService", "Google Sign In fallback to guest: ${e.message}")
            signInAnonymously()
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            updateProfile(null)
        } catch (e: Throwable) {
            Log.e("FirebaseService", "Sign out error", e)
        }
    }

    suspend fun saveCreation(creation: CreationItem): Result<Unit> = withContext(Dispatchers.IO) {
        // Keep in local cache
        synchronized(localCreations) {
            localCreations.removeAll { it.id == creation.id }
            localCreations.add(0, creation)
        }

        val currentFirestore = firestore
        val user = auth?.currentUser
        val uid = user?.uid ?: "guest_user"

        if (currentFirestore != null) {
            try {
                currentFirestore.collection("users")
                    .document(uid)
                    .collection("creations")
                    .document(creation.id)
                    .set(creation.toFirestoreMap())
                    .await()
            } catch (e: Throwable) {
                Log.w("FirebaseService", "Firestore write exception, saved locally: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    fun observeCreations(): Flow<List<CreationItem>> = callbackFlow {
        val currentFirestore = firestore
        val user = auth?.currentUser
        val uid = user?.uid ?: "guest_user"

        if (currentFirestore == null) {
            synchronized(localCreations) {
                trySend(localCreations.toList())
            }
            awaitClose { }
            return@callbackFlow
        }

        try {
            val listenerRegistration = currentFirestore.collection("users")
                .document(uid)
                .collection("creations")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirebaseService", "Firestore listen error: ${error.message}")
                        synchronized(localCreations) {
                            trySend(localCreations.toList())
                        }
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                CreationItem(
                                    id = doc.getString("id") ?: doc.id,
                                    title = doc.getString("title") ?: "Creation",
                                    prompt = doc.getString("prompt") ?: "",
                                    category = doc.getString("category") ?: "INTELLIGENCE",
                                    modelUsed = doc.getString("modelUsed") ?: "",
                                    resultText = doc.getString("resultText"),
                                    videoOperationName = doc.getString("videoOperationName"),
                                    videoStatus = doc.getString("videoStatus"),
                                    executionDurationMs = doc.getLong("executionDurationMs") ?: 0L,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    aspectRatio = doc.getString("aspectRatio"),
                                    isBookmarked = doc.getBoolean("isBookmarked") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        synchronized(localCreations) {
                            val merged = items.toMutableList()
                            localCreations.forEach { local ->
                                val existingIndex = merged.indexOfFirst { it.id == local.id }
                                if (existingIndex != -1) {
                                    merged[existingIndex] = merged[existingIndex].copy(
                                        imageBase64 = local.imageBase64,
                                        groundingMetadata = local.groundingMetadata
                                    )
                                } else {
                                    merged.add(local)
                                }
                            }
                            merged.sortByDescending { it.timestamp }
                            trySend(merged)
                        }
                    }
                }

            awaitClose {
                listenerRegistration.remove()
            }
        } catch (e: Throwable) {
            Log.w("FirebaseService", "Firestore observe exception: ${e.message}")
            synchronized(localCreations) {
                trySend(localCreations.toList())
            }
            awaitClose { }
        }
    }

    suspend fun deleteCreation(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        synchronized(localCreations) {
            localCreations.removeAll { it.id == id }
        }

        val currentFirestore = firestore
        val user = auth?.currentUser
        val uid = user?.uid ?: "guest_user"

        if (currentFirestore != null) {
            try {
                currentFirestore.collection("users")
                    .document(uid)
                    .collection("creations")
                    .document(id)
                    .delete()
                    .await()
            } catch (e: Throwable) {
                Log.w("FirebaseService", "Firestore delete exception: ${e.message}")
            }
        }
        Result.success(Unit)
    }

    suspend fun toggleBookmark(id: String, isBookmarked: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        synchronized(localCreations) {
            val index = localCreations.indexOfFirst { it.id == id }
            if (index != -1) {
                localCreations[index] = localCreations[index].copy(isBookmarked = isBookmarked)
            }
        }

        val currentFirestore = firestore
        val user = auth?.currentUser
        val uid = user?.uid ?: "guest_user"

        if (currentFirestore != null) {
            try {
                currentFirestore.collection("users")
                    .document(uid)
                    .collection("creations")
                    .document(id)
                    .update("isBookmarked", isBookmarked)
                    .await()
            } catch (e: Throwable) {
                Log.w("FirebaseService", "Firestore bookmark exception: ${e.message}")
            }
        }
        Result.success(Unit)
    }
}
