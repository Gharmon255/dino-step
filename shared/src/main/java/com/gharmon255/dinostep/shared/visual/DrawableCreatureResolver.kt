package com.gharmon255.dinostep.shared.visual

import android.content.res.Resources
import android.util.Log

/**
 * Resolves creature stage and egg drawables from stable species ids (Sprint 2).
 *
 * Phone and Wear pass their module [packageName] so `R.drawable` ids resolve locally.
 */
object DrawableCreatureResolver {
    private const val TAG = "DrawableCreatureResolver"

    fun resolveDrawableId(
        resources: Resources,
        packageName: String,
        drawableName: String,
        logContext: DrawableResolveContext? = null,
    ): Int {
        val id = resources.getIdentifier(drawableName, "drawable", packageName)
        if (id == 0) {
            logMissingDrawable(drawableName, logContext)
        }
        return id
    }

    fun resolveDrawableId(
        resources: Resources,
        packageName: String,
        candidates: List<String>,
        logContext: DrawableResolveContext? = null,
    ): Int {
        for (name in candidates) {
            val id = resources.getIdentifier(name, "drawable", packageName)
            if (id != 0) {
                return id
            }
        }
        if (candidates.isNotEmpty()) {
            logMissingDrawable(candidates.first(), logContext)
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
            logContext = null,
        )
    }

    /**
     * @param speciesId Stable catalog creature id (legacy ids normalized internally).
     * @param stageName `EGG`, `BABY`, `JUVENILE`, or `ADULT` (Wear/phone growth enums).
     */
    fun stageDrawableId(
        resources: Resources,
        packageName: String,
        speciesId: String,
        stageName: String,
    ): Int {
        val drawableName = CreatureAssetNames.stageDrawableName(speciesId, stageName)
            ?: return 0
        return resolveDrawableId(
            resources = resources,
            packageName = packageName,
            drawableName = drawableName,
            logContext = DrawableResolveContext(
                speciesId = speciesId,
                stageName = stageName,
                expectedDrawableName = drawableName,
            ),
        )
    }

    private fun logMissingDrawable(drawableName: String, context: DrawableResolveContext?) {
        if (context == null) {
            return
        }
        if (!CreatureAssetNames.isAssetBacked(context.speciesId)) {
            return
        }
        if (!Log.isLoggable(TAG, Log.DEBUG)) {
            return
        }
        Log.d(
            TAG,
            "Missing asset-backed drawable: $drawableName " +
                "(species=${context.speciesId}, stage=${context.stageName})",
        )
    }

    data class DrawableResolveContext(
        val speciesId: String,
        val stageName: String,
        val expectedDrawableName: String,
    )
}

/** @deprecated Use [DrawableCreatureResolver] */
@Deprecated("Renamed to DrawableCreatureResolver", ReplaceWith("DrawableCreatureResolver"))
typealias DrawableResourceResolver = DrawableCreatureResolver
