package com.gharmon255.dinostep.garmin

data class GarminPublishResult(
    val success: Boolean,
    val statusMessage: String,
    val jsonPayload: String,
    val payloadSummary: String,
)
