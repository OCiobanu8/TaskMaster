package com.example.taskmaster.core.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object GoogleSignInCoordinator {
    const val REQUEST_CODE: Int = 9024
    private var continuation: ((Result<GoogleSignInAccount>) -> Unit)? = null

    suspend fun launch(activity: Activity, signInIntent: Intent): Result<GoogleSignInAccount> {
        return suspendCancellableCoroutine { cont ->
            continuation = { result ->
                if (cont.isActive) {
                    cont.resume(result)
                }
            }

            activity.startActivityForResult(signInIntent, REQUEST_CODE)
            cont.invokeOnCancellation {
                continuation = null
            }
        }
    }

    fun handleActivityResult(requestCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CODE) {
            return false
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val callback = continuation
        continuation = null
        callback?.invoke(
            if (task.isSuccessful && task.result != null) {
                Result.success(task.result)
            } else {
                Result.failure(task.exception ?: IllegalStateException("Google Sign-In failed"))
            }
        )
        return true
    }
}
