package com.gharmon255.dinostep.promo

import com.gharmon255.dinostep.cloud.CloudAuthRepository
import com.gharmon255.dinostep.cloud.SupabaseConfig
import com.gharmon255.dinostep.cloud.SupabaseHttpClient
import com.gharmon255.dinostep.model.EggRarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PromoRedeemResult(
    val pendingRewardEggRarity: EggRarity,
    val message: String,
)

data class PromoStatusResult(
    val redeemed: Boolean,
    val rewardEggRarity: EggRarity?,
)

class PromoRepository(
    private val config: SupabaseConfig,
    private val httpClient: SupabaseHttpClient,
    private val authRepository: CloudAuthRepository,
) {
    suspend fun redeemCode(code: String): PromoRedeemResult = withContext(Dispatchers.IO) {
        val session = authRepository.currentSession()
            ?: throw IllegalStateException("Sign in required to redeem promo codes")
        val response = httpClient.invokePromoFunction(
            session = session,
            body = JSONObject()
                .put("action", "redeem")
                .put("code", code.trim()),
        )
        val rarityRaw = response.optString("pendingRewardEggRarity").ifBlank {
            response.optString("rewardEggRarity")
        }
        val rarity = EggRarity.fromRaw(rarityRaw)
        PromoRedeemResult(
            pendingRewardEggRarity = rarity,
            message = response.optString("message", "Your next reward egg will be ${rarity.name.lowercase()}!"),
        )
    }

    suspend fun status(code: String): PromoStatusResult = withContext(Dispatchers.IO) {
        val session = authRepository.currentSession()
            ?: throw IllegalStateException("Sign in required")
        val response = httpClient.invokePromoFunction(
            session = session,
            body = JSONObject()
                .put("action", "status")
                .put("code", code.trim()),
        )
        PromoStatusResult(
            redeemed = response.optBoolean("redeemed", false),
            rewardEggRarity = response.optString("rewardEggRarity").takeIf { it.isNotBlank() }
                ?.let { EggRarity.fromRaw(it) },
        )
    }
}
