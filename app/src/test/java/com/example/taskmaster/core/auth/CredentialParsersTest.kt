package com.example.taskmaster.core.auth

import android.os.Bundle
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CredentialParsersTest {

    @Test
    fun parseGoogleIdToken_returnsTokenForValidGoogleCredential() {
        val credential = CustomCredential(
            type = GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
            data = Bundle()
        )

        val token = parseGoogleIdToken(credential) { "id-token-123" }

        assertThat(token).isEqualTo("id-token-123")
    }

    @Test(expected = AuthSignInException.InvalidCredentialType::class)
    fun parseGoogleIdToken_failsForUnsupportedCredentialType() {
        val unsupported = CustomCredential(
            type = "unsupported_type",
            data = Bundle()
        )

        parseGoogleIdToken(unsupported)
    }

    @Test(expected = AuthSignInException.InvalidToken::class)
    fun parseGoogleIdToken_failsForMissingToken() {
        val broken = CustomCredential(
            type = GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
            data = Bundle()
        )

        parseGoogleIdToken(broken) { "" }
    }
}
