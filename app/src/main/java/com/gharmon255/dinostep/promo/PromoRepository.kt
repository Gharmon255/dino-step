package com.gharmon255.dinostep.promo

import com.gharmon255.dinostep.cloud.CloudAuthRepository
import com.gharmon255.dinostep.cloud.SupabaseConfig
import com.gharmon255.dinostep.cloud.SupabaseHttpClient
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.PlayerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class PromoRedeemResult(
    val playerStats: PlayerStats,
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
    suspend fun redeemCode(code: String, currentStats: PlayerStats): PromoRedeemResult =
        withContext(Dispatchers.IO) {
            val normalized = PromoCatalog.normalize(code)
            val rarity = PromoCatalog.rewardFor(normalized)
                ?: throw IllegalArgumentException("Unknown or invalid promo code")

            if (PromoRedemptionCodec.hasRedeemed(currentStats.redeemedPromoCodes, normalized)) {
                throw IllegalStateException("This promo code was already used on this device")
            }

            val session = authRepository.currentSession()
            if (session != null && config.isConfigured) {
                runCatching {
                    redeemViaServer(session, code, currentStats, normalized, rarity)
                }.onSuccess { return@withContext it }
                    .onFailure { error ->
                        if (error is IllegalStateException && error.message?.contains("already used") == true) {
                            throw error
                        }
                        if (error is IllegalArgumentException) {
                            throw error
                        }
                        // Fall back to local redemption when cloud is unavailable or sign-in fails mid-flight.
                    }
            }

            redeemLocally(currentStats, normalized, rarity)
        }

    suspend fun syncRedemptionStatus(currentStats: PlayerStats): PlayerStats = withContext(Dispatchers.IO) {
        val session = authRepository.currentSession() ?: return@withContext currentStats
        if (!config.isConfigured) return@withContext currentStats

        var redeemed = PromoRedemptionCodec.parse(currentStats.redeemedPromoCodes)
        PromoCatalog.knownCodes().forEach { code ->
            runCatching {
                status(code).takeIf { it.redeemed }?.let { redeemed = redeemed + code }
            }
        }
        currentStats.copy(redeemedPromoCodes = PromoRedemptionCodec.encode(redeemed))
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

    private suspend fun redeemViaServer(
        session: com.gharmon255.dinostep.cloud.CloudSession,
        rawCode: String,
        currentStats: PlayerStats,
        normalized: String,
        expectedRarity: EggRarity,
    ): PromoRedeemResult {
        val response = httpClient.invokePromoFunction(
            session = session,
            body = JSONObject()
                .put("action", "redeem")
                .put("code", rawCode.trim()),
        )
        val rarityRaw = response.optString("pendingRewardEggRarity").ifBlank {
            response.optString("rewardEggRarity")
        }
        val rarity = EggRarity.fromRaw(rarityRaw)
        val updatedStats = currentStats.copy(
            pendingRewardEggRarity = rarity.name,
            redeemedPromoCodes = PromoRedemptionCodec.markRedeemed(currentStats.redeemedPromoCodes, normalized),
        )
        return PromoRedeemResult(
            playerStats = updatedStats,
            message = response.optString("message", PromoCatalog.successMessage(expectedRarity)),
        )
    }

    private fun redeemLocally(
        currentStats: PlayerStats,
        normalized: String,
        rarity: EggRarity,
    ): PromoRedeemResult {
        val updatedStats = currentStats.copy(
            pendingRewardEggRarity = rarity.name,
            redeemedPromoCodes = PromoRedemptionCodec.markRedeemed(currentStats.redeemedPromoCodes, normalized),
        )
        return PromoRedeemResult(
            playerStats = updatedStats,
            message = PromoCatalog.successMessage(rarity),
        )
    }
}
