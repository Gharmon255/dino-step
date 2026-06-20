package com.gharmon255.dinostep.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object GoogleSignInHelper {
    suspend fun silentIdToken(context: Context, webClientId: String): String? {
        if (webClientId.isBlank()) {
            return null
        }
        return suspendCancellableCoroutine { continuation ->
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context.applicationContext, gso)
                .silentSignIn()
                .addOnSuccessListener { account ->
                    continuation.resume(account.idToken)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }
}
