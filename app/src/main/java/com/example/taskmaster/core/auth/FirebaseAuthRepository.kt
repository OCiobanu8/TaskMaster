package com.example.taskmaster.core.auth

import android.app.Activity
import com.example.taskmaster.R
import com.example.taskmaster.core.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val activityProvider: () -> Activity?
) : AuthRepository {

    override fun currentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomain())
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.toDomain())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(): Result<User> {
        val activity = activityProvider()
            ?: return Result.failure(IllegalStateException("No active activity for sign-in"))

        val webClientId = activity.getString(R.string.default_web_client_id)
        if (webClientId.isBlank() || webClientId == "REPLACE_WITH_WEB_CLIENT_ID") {
            return Result.failure(
                IllegalStateException(
                    "Set default_web_client_id in strings.xml before using Google Sign-In."
                )
            )
        }

        return runCatching {
            val account = signInForAccount(activity, webClientId)
            val idToken = account.idToken
                ?: throw IllegalStateException("Google account did not return an ID token.")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.toDomain()
                ?: throw IllegalStateException("Firebase user is null after sign-in")
        }
    }

    override suspend fun signOut() {
        val activity = activityProvider()
        if (activity != null) {
            val client = GoogleSignIn.getClient(activity, buildGoogleSignInOptions(activity))
            client.signOut().await()
        }
        firebaseAuth.signOut()
    }

    private suspend fun signInForAccount(activity: Activity, webClientId: String) =
        try {
            GoogleSignIn.getLastSignedInAccount(activity)
                ?: GoogleSignIn.getClient(activity, buildGoogleSignInOptions(activity, webClientId))
                    .silentSignIn()
                    .await()
        } catch (_: Throwable) {
            val client = GoogleSignIn.getClient(activity, buildGoogleSignInOptions(activity, webClientId))
            GoogleSignInCoordinator.launch(activity, client.signInIntent).getOrThrow()
        }

    private fun buildGoogleSignInOptions(activity: Activity, overrideWebClientId: String? = null): GoogleSignInOptions {
        val webClientId = overrideWebClientId ?: activity.getString(R.string.default_web_client_id)
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
    }
}

private fun com.google.firebase.auth.FirebaseUser.toDomain(): User {
    return User(
        id = uid,
        displayName = displayName,
        email = email
    )
}
