package com.example.taskmaster.core.debug

import com.google.firebase.firestore.FirebaseFirestoreException

fun formatDebugError(error: Throwable?): String? {
    if (error == null) return null

    val prefix = when (error) {
        is FirebaseFirestoreException -> "Firestore ${error.code.name}"
        else -> error::class.simpleName ?: "Error"
    }

    val message = error.message ?: "No message"
    val cause = error.cause?.message

    return if (cause.isNullOrBlank()) {
        "$prefix: $message"
    } else {
        "$prefix: $message | cause: $cause"
    }
}
