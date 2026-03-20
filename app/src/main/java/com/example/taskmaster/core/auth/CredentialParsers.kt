package com.example.taskmaster.core.auth

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

internal fun parseGoogleIdToken(
    credential: Credential,
    tokenParser: (CustomCredential) -> String = ::parseTokenFromGoogleCustomCredential
): String {
    if (credential !is CustomCredential) {
        throw AuthSignInException.InvalidCredentialType()
    }
    if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        throw AuthSignInException.InvalidCredentialType()
    }

    val idToken = tokenParser(credential)
    if (idToken.isBlank()) {
        throw AuthSignInException.InvalidToken()
    }
    return idToken
}

internal fun parseTokenFromGoogleCustomCredential(credential: CustomCredential): String {
    val parsed = runCatching { GoogleIdTokenCredential.createFrom(credential.data) }.getOrElse {
        throw AuthSignInException.InvalidToken()
    }
    return parsed.idToken
}
