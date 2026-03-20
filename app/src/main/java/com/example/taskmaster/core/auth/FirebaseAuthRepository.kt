package com.example.taskmaster.core.auth

import android.app.Activity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import com.example.taskmaster.R
import com.example.taskmaster.core.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
            ?: return Result.failure(AuthSignInException.NoActiveActivity())

        val webClientId = activity.getString(R.string.default_web_client_id)
        if (webClientId.isBlank() || webClientId == "REPLACE_WITH_WEB_CLIENT_ID") {
            return Result.failure(AuthSignInException.MissingConfiguration())
        }

        return runCatching {
            val idToken = fetchGoogleIdToken(activity, webClientId)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.toDomain()
                ?: throw IllegalStateException("Firebase user is null after sign-in")
        }.recoverCatching { error ->
            throw mapCredentialException(error)
        }
    }

    override suspend fun signOut() {
        val activity = activityProvider()
        if (activity != null) {
            runCatching {
                CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
            }
        }
        firebaseAuth.signOut()
    }

    private suspend fun fetchGoogleIdToken(activity: Activity, webClientId: String): String {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val manager = CredentialManager.create(activity)
        val result = manager.getCredential(
            context = activity,
            request = request
        )
        return parseGoogleIdToken(result.credential)
    }

    private fun mapCredentialException(error: Throwable): Throwable {
        return when (error) {
            is AuthSignInException -> error
            is GetCredentialCancellationException -> AuthSignInException.Cancelled()
            is NoCredentialException -> AuthSignInException.NoCredential()
            is GetCredentialProviderConfigurationException -> AuthSignInException.ProviderUnavailable()
            is GetCredentialException -> AuthSignInException.InvalidCredentialType()
            else -> error
        }
    }
}

private fun com.google.firebase.auth.FirebaseUser.toDomain(): User {
    return User(
        id = uid,
        displayName = displayName,
        email = email
    )
}
