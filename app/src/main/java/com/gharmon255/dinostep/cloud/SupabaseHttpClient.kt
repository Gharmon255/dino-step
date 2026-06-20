package com.gharmon255.dinostep.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SupabaseHttpClient(
    private val config: SupabaseConfig,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
  suspend fun signInWithIdToken(provider: String, idToken: String): CloudSession {
        val body = JSONObject()
            .put("provider", provider)
            .put("id_token", idToken)
            .toString()
        val json = post(
            path = "/auth/v1/token?grant_type=id_token",
            body = body,
            accessToken = null,
        )
        return CloudSession(
            userId = json.getJSONObject("user").getString("id"),
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            email = json.getJSONObject("user").optString("email").takeIf { it.isNotBlank() },
            provider = provider,
        )
    }

    suspend fun refreshSession(refreshToken: String): CloudSession {
        val body = JSONObject().put("refresh_token", refreshToken).toString()
        val json = post(
            path = "/auth/v1/token?grant_type=refresh_token",
            body = body,
            accessToken = null,
        )
        return CloudSession(
            userId = json.getJSONObject("user").getString("id"),
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            email = json.getJSONObject("user").optString("email").takeIf { it.isNotBlank() },
            provider = json.getJSONObject("user").optJSONObject("app_metadata")?.optString("provider"),
        )
    }

    suspend fun fetchGameSave(session: CloudSession): CloudSaveRow? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${config.url.trimEnd('/')}/rest/v1/game_saves?user_id=eq.${session.userId}&select=*")
            .get()
            .header("apikey", config.anonKey)
            .header("Authorization", "Bearer ${session.accessToken}")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("fetchGameSave failed: ${response.code}")
        }
        val body = response.body?.string().orEmpty()
        val array = JSONArray(body)
        if (array.length() == 0) {
            return@withContext null
        }
        CloudSaveJson.decodeRow(array.getJSONObject(0))
    }

    suspend fun upsertGameSave(session: CloudSession, row: CloudSaveRow) = withContext(Dispatchers.IO) {
        val payload = CloudSaveJson.encodeRow(row).toString()
        val request = Request.Builder()
            .url("${config.url.trimEnd('/')}/rest/v1/game_saves")
            .post(payload.toRequestBody(JSON_MEDIA))
            .header("apikey", config.anonKey)
            .header("Authorization", "Bearer ${session.accessToken}")
            .header("Prefer", "resolution=merge-duplicates")
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("upsertGameSave failed: ${response.code} ${response.body?.string()}")
        }
    }

    suspend fun invokeBattleFunction(session: CloudSession, body: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${config.url.trimEnd('/')}/functions/v1/battle")
                .post(body.toString().toRequestBody(JSON_MEDIA))
                .header("apikey", config.anonKey)
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMessage = runCatching {
                    JSONObject(responseBody).optString("error", responseBody)
                }.getOrDefault(responseBody)
                throw IOException(errorMessage.ifBlank { "Battle request failed: ${response.code}" })
            }
            JSONObject(responseBody)
        }

    private suspend fun post(path: String, body: String, accessToken: String?): JSONObject =
        withContext(Dispatchers.IO) {
            val builder = Request.Builder()
                .url("${config.url.trimEnd('/')}$path")
                .post(body.toRequestBody(JSON_MEDIA))
                .header("apikey", config.anonKey)
                .header("Content-Type", "application/json")
            if (accessToken != null) {
                builder.header("Authorization", "Bearer $accessToken")
            }
            val response = client.newCall(builder.build()).execute()
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IOException("Supabase request failed: ${response.code} $responseBody")
            }
            JSONObject(responseBody)
        }

    companion object {
        private val JSON_MEDIA = "application/json".toMediaType()
    }
}
