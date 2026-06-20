package com.gharmon255.dinostep.cloud

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class CloudSessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = openPrefs(appContext)

    fun loadSession(): CloudSession? {
        val fallbackPrefs = appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        return try {
            readSession(prefs) ?: readSession(fallbackPrefs)
        } catch (_: Exception) {
            readSession(fallbackPrefs)
        }
    }

    fun saveSession(session: CloudSession) {
        try {
            writeSession(prefs, session)
        } catch (_: Exception) {
            // Encrypted prefs can fail after reinstall; fall back to plain storage.
            writeSession(appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE), session)
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
        appContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun saveSessionFromAuthJson(json: JSONObject): CloudSession {
        val user = json.getJSONObject("user")
        val session = CloudSession(
            userId = user.getString("id"),
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            email = user.optString("email").takeIf { it.isNotBlank() },
            provider = user.optJSONObject("app_metadata")?.optString("provider"),
        )
        saveSession(session)
        return session
    }

    private fun readSession(prefs: SharedPreferences): CloudSession? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null) ?: return null
        return CloudSession(
            userId = userId,
            accessToken = accessToken,
            refreshToken = refreshToken,
            email = prefs.getString(KEY_EMAIL, null),
            provider = prefs.getString(KEY_PROVIDER, null),
        )
    }

    private fun writeSession(prefs: SharedPreferences, session: CloudSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_PROVIDER, session.provider)
            .apply()
    }

    private fun openPrefs(context: Context): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val PREFS_NAME = "cloud_session"
        private const val FALLBACK_PREFS_NAME = "cloud_session_fallback"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROVIDER = "provider"
    }
}
