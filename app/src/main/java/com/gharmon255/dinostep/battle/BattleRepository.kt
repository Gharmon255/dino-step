package com.gharmon255.dinostep.battle

import com.gharmon255.dinostep.cloud.CloudAuthRepository
import com.gharmon255.dinostep.cloud.CloudSession
import com.gharmon255.dinostep.cloud.SupabaseConfig
import com.gharmon255.dinostep.cloud.SupabaseHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class BattleRecord(
    val id: String,
    val mode: String,
    val playerASpeciesId: String,
    val playerBSpeciesId: String,
    val playerAPower: Int,
    val playerBPower: Int,
    val winner: String,
    val turnLog: List<BattleTurn>,
    val createdAt: String,
    val playerAUserId: String,
    val playerBUserId: String,
)

data class BattleTurn(
    val turn: Int,
    val actor: String,
    val action: String,
    val damage: Int,
    val message: String,
    val aHp: Int,
    val bHp: Int,
)

data class BattleChallenge(
    val id: String,
    val status: String,
    val challengerId: String,
    val opponentId: String?,
    val battleId: String?,
)

data class PlayerBattleProfile(
    val inviteCode: String,
    val elo: Int,
)

class BattleRepository(
    private val config: SupabaseConfig,
    private val httpClient: SupabaseHttpClient,
    private val authRepository: CloudAuthRepository,
) {
    suspend fun ensureProfile(): PlayerBattleProfile? = withSession { session ->
        val json = httpClient.invokeBattleFunction(session, JSONObject().put("action", "ensureProfile"))
        val profile = json.getJSONObject("profile")
        PlayerBattleProfile(
            inviteCode = profile.getString("invite_code"),
            elo = profile.optInt("elo", 1000),
        )
    }

    suspend fun createChallenge(): Pair<BattleChallenge, String>? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject().put("action", "createChallenge"),
        )
        val challenge = parseChallenge(json.getJSONObject("challenge"))
        val inviteCode = json.getString("inviteCode")
        challenge to inviteCode
    }

    suspend fun joinChallenge(challengeId: String): BattleChallenge? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject()
                .put("action", "joinChallenge")
                .put("challengeId", challengeId),
        )
        parseChallenge(json.getJSONObject("challenge"))
    }

    suspend fun acceptChallengeByInvite(inviteCode: String): BattleChallenge? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject()
                .put("action", "acceptChallenge")
                .put("inviteCode", inviteCode.trim()),
        )
        parseChallenge(json.getJSONObject("challenge"))
    }

    suspend fun submitPick(
        challengeId: String,
        completedCreatureId: String,
    ): Pair<BattleChallenge, BattleRecord?> {
        return withSession { session ->
            val json = httpClient.invokeBattleFunction(
                session,
                JSONObject()
                    .put("action", "submitPick")
                    .put("challengeId", challengeId)
                    .put("completedCreatureId", completedCreatureId),
            )
            val challenge = parseChallenge(json.getJSONObject("challenge"))
            val battle = json.optJSONObject("battle")?.let { parseBattle(it) }
            challenge to battle
        } ?: error("Sign in required for battles")
    }

    suspend fun findQuickMatch(completedCreatureId: String): BattleRecord? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject()
                .put("action", "findQuickMatch")
                .put("completedCreatureId", completedCreatureId),
        )
        parseBattle(json.getJSONObject("battle"))
    }

    suspend fun listBattles(): List<BattleRecord> {
        return withSession { session ->
            val json = httpClient.invokeBattleFunction(
                session,
                JSONObject().put("action", "listBattles"),
            )
            val array = json.getJSONArray("battles")
            buildList {
                for (i in 0 until array.length()) {
                    add(parseBattle(array.getJSONObject(i)))
                }
            }
        } ?: emptyList()
    }

    suspend fun getChallenge(challengeId: String): BattleChallenge? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject()
                .put("action", "getChallenge")
                .put("challengeId", challengeId),
        )
        parseChallenge(json.getJSONObject("challenge"))
    }

    suspend fun getBattle(battleId: String): BattleRecord? = withSession { session ->
        val json = httpClient.invokeBattleFunction(
            session,
            JSONObject()
                .put("action", "getBattle")
                .put("battleId", battleId),
        )
        parseBattle(json.getJSONObject("battle"))
    }

    private suspend fun <T> withSession(block: suspend (CloudSession) -> T): T? = withContext(Dispatchers.IO) {
        if (!config.isConfigured) {
            return@withContext null
        }
        val session = authRepository.currentSession() ?: return@withContext null
        block(session)
    }

    private fun parseChallenge(json: JSONObject): BattleChallenge {
        return BattleChallenge(
            id = json.getString("id"),
            status = json.getString("status"),
            challengerId = json.getString("challenger_id"),
            opponentId = json.optString("opponent_id").takeIf { it.isNotBlank() },
            battleId = json.optString("battle_id").takeIf { it.isNotBlank() },
        )
    }

    private fun parseBattle(json: JSONObject): BattleRecord {
        val turns = buildList {
            val array = json.getJSONArray("turn_log")
            for (i in 0 until array.length()) {
                val turn = array.getJSONObject(i)
                add(
                    BattleTurn(
                        turn = turn.getInt("turn"),
                        actor = turn.getString("actor"),
                        action = turn.optString("action", "Attack"),
                        damage = turn.optInt("damage"),
                        message = turn.optString("message"),
                        aHp = turn.optInt("aHp"),
                        bHp = turn.optInt("bHp"),
                    ),
                )
            }
        }
        return BattleRecord(
            id = json.getString("id"),
            mode = json.getString("mode"),
            playerASpeciesId = json.getString("player_a_species_id"),
            playerBSpeciesId = json.getString("player_b_species_id"),
            playerAPower = json.getInt("player_a_power"),
            playerBPower = json.getInt("player_b_power"),
            winner = json.getString("winner"),
            turnLog = turns,
            createdAt = json.getString("created_at"),
            playerAUserId = json.getString("player_a_user_id"),
            playerBUserId = json.getString("player_b_user_id"),
        )
    }
}
