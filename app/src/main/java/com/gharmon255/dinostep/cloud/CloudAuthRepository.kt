package com.gharmon255.dinostep.cloud

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.io.IOException

class CloudAuthRepository(
    private val config: SupabaseConfig,
    private val httpClient: SupabaseHttpClient,
    private val sessionStore: CloudSessionStore,
    private val appContext: Context,
) {
    val isConfigured: Boolean
        get() = config.isConfigured

    fun currentSession(): CloudSession? = sessionStore.loadSession()

    suspend fun signInWithGoogleIdToken(idToken: String): CloudSession {
        val session = httpClient.signInWithIdToken("google", idToken)
        sessionStore.saveSession(session)
        return session
    }

    suspend fun restoreSession(): CloudSession? {
        val existing = sessionStore.loadSession() ?: return null
        return try {
            val refreshed = httpClient.refreshSession(existing.refreshToken)
            sessionStore.saveSession(refreshed)
            refreshed
        } catch (error: SupabaseHttpException) {
            if (error.isInvalidRefreshToken) {
                sessionStore.clear()
                null
            } else {
                existing
            }
        } catch (_: IOException) {
            existing
        } catch (_: Exception) {
            existing
        }
    }

    suspend fun trySilentGoogleSignIn(): CloudSession? {
        if (!config.isConfigured || config.googleWebClientId.isBlank()) {
            return null
        }
        val idToken = GoogleSignInHelper.silentIdToken(appContext, config.googleWebClientId) ?: return null
        return runCatching { signInWithGoogleIdToken(idToken) }.getOrNull()
    }

    fun signOut() {
        sessionStore.clear()
    }
}

fun extractGoogleIdToken(data: android.content.Intent?): String {
    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
    val account = task.getResult(ApiException::class.java)
    return account.idToken ?: error("Google sign-in returned no ID token")
}
