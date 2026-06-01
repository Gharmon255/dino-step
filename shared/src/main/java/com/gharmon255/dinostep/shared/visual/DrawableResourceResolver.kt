package com.gharmon255.dinostep.shared.visual

import android.content.res.Resources
import android.util.Log

object DrawableResourceResolver {
    private const val TAG = "DrawableResourceResolver"

    fun resolveDrawableId(
        resources: Resources,
        packageName: String,
        candidates: List<String>,
    ): Int {
        for (name in candidates) {
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) {
                return id
            }
        }
        if (candidates.isNotEmpty()) {
            Log.w(TAG, "Missing drawable for candidates: ${candidates.joinToString()}")
        }
        return 0
    }

    fun eggDrawableId(
        resources: Resources,
        packageName: String,
        eggRarityName: String,
    ): Int {
        return resolveDrawableId(
            resources = resources,
            packageName = packageName,
            candidates = CreatureAssetNames.eggDrawableCandidates(eggRarityName),
        )
    }

    fun stageDrawableId(
        resources: Resources,
        packageName: String,
        creatureId: String,
        stageName: String,
    ): Int {
        val name = CreatureAssetNames.stageDrawableName(creatureId, stageName) ?: return 0
        return resolveDrawableId(
            resources = resources,
            packageName = packageName,
            candidates = listOf(name),
        )
    }
}
