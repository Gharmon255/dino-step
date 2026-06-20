package com.gharmon255.dinostep.cloud

import java.io.IOException

class SupabaseHttpException(
    val statusCode: Int,
    val responseBody: String,
    message: String = "Supabase request failed: $statusCode $responseBody",
) : IOException(message) {
    val isInvalidRefreshToken: Boolean
        get() = statusCode in 400..401 &&
            (
                responseBody.contains("invalid_grant", ignoreCase = true) ||
                    responseBody.contains("refresh_token_not_found", ignoreCase = true) ||
                    responseBody.contains("Invalid Refresh Token", ignoreCase = true)
                )
}
