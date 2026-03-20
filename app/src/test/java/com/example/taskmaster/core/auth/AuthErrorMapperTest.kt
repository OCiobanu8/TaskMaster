package com.example.taskmaster.core.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun mapAuthErrorToUserMessage_returnsSpecificMessageForKnownAuthErrors() {
        assertThat(mapAuthErrorToUserMessage(AuthSignInException.Cancelled()))
            .isEqualTo("Sign-in cancelled.")
        assertThat(mapAuthErrorToUserMessage(AuthSignInException.NoCredential()))
            .isEqualTo("No Google account credential was found on this device.")
        assertThat(mapAuthErrorToUserMessage(AuthSignInException.ProviderUnavailable()))
            .isEqualTo("Google credential provider is unavailable on this device.")
    }

    @Test
    fun mapAuthErrorToUserMessage_fallsBackToThrowableMessage() {
        val message = mapAuthErrorToUserMessage(IllegalStateException("boom"))
        assertThat(message).isEqualTo("boom")
    }
}
