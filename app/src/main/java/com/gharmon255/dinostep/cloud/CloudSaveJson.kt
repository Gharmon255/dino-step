package com.gharmon255.dinostep.cloud

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeFormatter

object CloudSaveJson {
    private val isoFormatter = DateTimeFormatter.ISO_INSTANT

    fun encode(save: CloudGameSave): JSONObject {
        return JSONObject().apply {
            put("schemaVersion", save.schemaVersion)
            put("revision", save.revision)
            put("updatedAt", save.updatedAt)
            put("activeCreature", encodeActive(save.activeCreature))
            put("completedCreatures", JSONArray().apply {
                save.completedCreatures.forEach { put(encodeCompleted(it)) }
            })
            put("playerStats", encodeStats(save.playerStats))
            save.lastRewardedEggRarity?.let { put("lastRewardedEggRarity", it) }
            save.lastRewardRollPercent?.let { put("lastRewardRollPercent", it) }
            save.pendingRewardEggRarity?.let { put("pendingRewardEggRarity", it) }
        }
    }

    fun decode(json: JSONObject): CloudGameSave {
        val completed = buildList {
            val array = json.getJSONArray("completedCreatures")
            for (i in 0 until array.length()) {
                add(decodeCompleted(array.getJSONObject(i)))
            }
        }
        return CloudGameSave(
            schemaVersion = json.getInt("schemaVersion"),
            revision = json.getLong("revision"),
            updatedAt = json.getString("updatedAt"),
            activeCreature = decodeActive(json.getJSONObject("activeCreature")),
            completedCreatures = completed,
            playerStats = decodeStats(json.getJSONObject("playerStats")),
            lastRewardedEggRarity = json.optString("lastRewardedEggRarity").takeIf { it.isNotBlank() },
            lastRewardRollPercent = if (json.has("lastRewardRollPercent") && !json.isNull("lastRewardRollPercent")) {
                json.getDouble("lastRewardRollPercent")
            } else {
                null
            },
            pendingRewardEggRarity = json.optString("pendingRewardEggRarity").takeIf { it.isNotBlank() },
        )
    }

    fun encodeRow(row: CloudSaveRow): JSONObject {
        return JSONObject().apply {
            put("user_id", row.userId)
            put("schema_version", row.schemaVersion)
            put("revision", row.revision)
            put("save_json", encode(row.save))
            put("updated_at", row.updatedAt)
        }
    }

    fun decodeRow(json: JSONObject): CloudSaveRow {
        val saveJson = json.getJSONObject("save_json")
        return CloudSaveRow(
            userId = json.getString("user_id"),
            schemaVersion = json.getInt("schema_version"),
            revision = json.getLong("revision"),
            save = decode(saveJson),
            updatedAt = json.getString("updated_at"),
        )
    }

    fun nowIso(): String = isoFormatter.format(Instant.now())

    private fun encodeActive(creature: CloudActiveCreature): JSONObject {
        return JSONObject().apply {
            put("speciesId", creature.speciesId)
            put("eggRarity", creature.eggRarity)
            put("steps", creature.steps)
            put("isRevealed", creature.isRevealed)
            creature.nickname?.let { put("nickname", it) }
            put("startedAt", creature.startedAt)
            put("hatchStep", creature.hatchStep)
            put("juvenileStep", creature.juvenileStep)
            put("totalStepsRequired", creature.totalStepsRequired)
            put("economyVersion", creature.economyVersion)
        }
    }

    private fun decodeActive(json: JSONObject): CloudActiveCreature {
        return CloudActiveCreature(
            speciesId = json.getString("speciesId"),
            eggRarity = json.getString("eggRarity"),
            steps = json.getInt("steps"),
            isRevealed = json.getBoolean("isRevealed"),
            nickname = json.optString("nickname").takeIf { it.isNotBlank() },
            startedAt = json.getString("startedAt"),
            hatchStep = json.getInt("hatchStep"),
            juvenileStep = json.getInt("juvenileStep"),
            totalStepsRequired = json.getInt("totalStepsRequired"),
            economyVersion = json.getInt("economyVersion"),
        )
    }

    private fun encodeCompleted(creature: CloudCompletedCreature): JSONObject {
        return JSONObject().apply {
            put("id", creature.id)
            put("speciesId", creature.speciesId)
            put("stepsCompleted", creature.stepsCompleted)
            put("completedAt", creature.completedAt)
            creature.nickname?.let { put("nickname", it) }
            put("eggRarityAtHatch", creature.eggRarityAtHatch)
            put("exSteps", creature.exSteps)
            put("exLevel", creature.exLevel)
        }
    }

    private fun decodeCompleted(json: JSONObject): CloudCompletedCreature {
        return CloudCompletedCreature(
            id = json.getString("id"),
            speciesId = json.getString("speciesId"),
            stepsCompleted = json.getInt("stepsCompleted"),
            completedAt = json.getString("completedAt"),
            nickname = json.optString("nickname").takeIf { it.isNotBlank() },
            eggRarityAtHatch = json.optString("eggRarityAtHatch", "COMMON"),
            exSteps = json.optInt("exSteps", 0),
            exLevel = json.optInt("exLevel", 1).coerceAtLeast(1),
        )
    }

    private fun encodeStats(stats: CloudPlayerStats): JSONObject {
        return JSONObject().apply {
            put("eggsHatched", stats.eggsHatched)
            put("creaturesCompleted", stats.creaturesCompleted)
            put("lastSyncedStepTotal", stats.lastSyncedStepTotal)
            put("lastSyncDayStartMillis", stats.lastSyncDayStartMillis)
            put("lifetimeStepsApplied", stats.lifetimeStepsApplied)
        }
    }

    private fun decodeStats(json: JSONObject): CloudPlayerStats {
        return CloudPlayerStats(
            eggsHatched = json.getInt("eggsHatched"),
            creaturesCompleted = json.getInt("creaturesCompleted"),
            lastSyncedStepTotal = json.getInt("lastSyncedStepTotal"),
            lastSyncDayStartMillis = json.getLong("lastSyncDayStartMillis"),
            lifetimeStepsApplied = json.getInt("lifetimeStepsApplied"),
        )
    }
}
