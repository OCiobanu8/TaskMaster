package com.example.taskmaster.core.auth

internal sealed class AuthSignInException(message: String) : Exception(message) {
    class MissingConfiguration : AuthSignInException("Set default_web_client_id in strings.xml before using Google Sign-In.")
    class NoActiveActivity : AuthSignInException("No active activity for sign-in.")
    class Cancelled : AuthSignInException("Sign-in cancelled.")
    class NoCredential : AuthSignInException("No Google account credential was found on this device.")
    class ProviderUnavailable : AuthSignInException("Google credential provider is unavailable on this device.")
    class InvalidCredentialType : AuthSignInException("Google sign-in returned an unsupported credential type.")
    class InvalidToken : AuthSignInException("Google sign-in returned an invalid ID token.")
}

fun mapAuthErrorToUserMessage(error: Throwable?): String? {
    return when (error) {
        null -> null
        is AuthSignInException -> error.message
        else -> error.message ?: "Authentication failed. Please try again."
    }
}
