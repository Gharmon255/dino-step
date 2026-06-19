package com.gharmon255.dinostep.cloud

import com.gharmon255.dinostep.BuildConfig

data class SupabaseConfig(
    val url: String,
    val anonKey: String,
    val googleWebClientId: String,
) {
    val isConfigured: Boolean
        get() = url.isNotBlank() && anonKey.isNotBlank()

    companion object {
        fun fromBuildConfig(): SupabaseConfig {
            return SupabaseConfig(
                url = BuildConfig.SUPABASE_URL,
                anonKey = BuildConfig.SUPABASE_ANON_KEY,
                googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
            )
        }
    }
}
