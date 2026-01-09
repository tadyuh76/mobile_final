package com.example.mobile_final.data.repository

import android.content.Context
import android.util.Log
import com.example.mobile_final.R
import com.example.mobile_final.data.local.dao.ActivityDao
import com.example.mobile_final.data.local.dao.LocationPointDao
import com.example.mobile_final.data.local.dao.UserSettingsDao
import com.example.mobile_final.domain.repository.AuthRepository
import com.example.mobile_final.domain.repository.UserDataRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val userDataRepository: UserDataRepository,
    private val activityDao: ActivityDao,
    private val locationPointDao: LocationPointDao,
    private val userSettingsDao: UserSettingsDao
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override val isSignedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user?.let { user ->
                // Restore user data from cloud after successful sign-in
                // This is synchronous to ensure data is ready before UI loads
                restoreUserDataFromCloud(user.uid)
                Result.success(user)
            } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores user activity data from Firestore to local database.
     * This is a SYNCHRONOUS operation - sign-in waits for data to be ready.
     * Clears local database first to prevent mixing different users' data.
     */
    private suspend fun restoreUserDataFromCloud(userId: String) {
        try {
            // Clear any existing local data first to prevent mixing users' data
            Log.d(TAG, "Clearing local database before restore")
            activityDao.deleteAllActivities()
            locationPointDao.deleteAllLocationPoints()
            userSettingsDao.deleteAllSettings()

            userDataRepository.restoreUserActivities(userId)
                .onSuccess { cloudActivities ->
                    Log.d(TAG, "Restoring ${cloudActivities.size} activities from cloud")

                    cloudActivities.forEach { (activity, locationPoints) ->
                        try {
                            // Insert activity
                            val activityId = activityDao.insertActivity(activity.toEntity())

                            // Insert location points with correct activity ID
                            val pointsWithActivityId = locationPoints.map { point ->
                                point.copy(activityId = activityId).toEntity()
                            }
                            locationPointDao.insertLocationPoints(pointsWithActivityId)

                            Log.d(TAG, "Restored activity from ${activity.startTime}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to restore activity: ${e.message}")
                        }
                    }

                    Log.d(TAG, "Data restore completed - ${cloudActivities.size} activities")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to restore user data: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error during data restoration: ${e.message}")
        }
    }

    override suspend fun signOut() {
        // Clear ALL local data for privacy before signing out
        // This ensures no data leaks between different user accounts
        Log.d(TAG, "Clearing all local data on sign-out")
        activityDao.deleteAllActivities()
        locationPointDao.deleteAllLocationPoints()
        userSettingsDao.deleteAllSettings()

        // Sign out from Firebase
        firebaseAuth.signOut()

        // Also sign out from Google to clear the session
        // This ensures the account picker shows up on next sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signOut().await()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}
