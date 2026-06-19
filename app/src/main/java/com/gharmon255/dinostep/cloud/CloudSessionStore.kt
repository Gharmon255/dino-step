package com.gharmon255.dinostep.cloud

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject

class CloudSessionStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun loadSession(): CloudSession? {
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

    fun saveSession(session: CloudSession) {
        prefs.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_PROVIDER, session.provider)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
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

    companion object {
        private const val PREFS_NAME = "cloud_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_PROVIDER = "provider"
    }
}
