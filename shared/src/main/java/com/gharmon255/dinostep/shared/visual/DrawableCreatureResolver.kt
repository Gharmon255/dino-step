package com.gharmon255.dinostep.shared.visual

import android.content.res.Resources
import android.util.Log

/**
 * Single resolver for phone and Wear creature stage / egg drawables.
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
     * Resolves baby/juvenile/adult drawable: species PNG if backed and present, else stage placeholder.
     * Never falls back to another species' art.
     */
    fun stageDrawableId(
        resources: Resources,
        packageName: String,
        speciesId: String,
        stageName: String,
    ): Int {
        val suffix = CreatureAssetNames.stageSuffixFromName(stageName) ?: return 0
        val assetSlug = CreatureAssetNames.assetSlugForSpeciesArt(speciesId)
        val logContext = DrawableResolveContext(
            speciesId = speciesId,
            stageName = stageName,
            assetSlug = assetSlug,
        )

        if (assetSlug != null) {
            val speciesDrawable = CreatureAssetNames.dinoStageDrawableNameForSlug(assetSlug, suffix)
            val speciesId_res = resolveDrawableId(
                resources = resources,
                packageName = packageName,
                drawableName = speciesDrawable,
                logContext = logContext.copy(expectedDrawableName = speciesDrawable, isPlaceholder = false),
            )
            if (speciesId_res != 0) {
                return speciesId_res
            }
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(
                    TAG,
                    "Missing species drawable $speciesDrawable for speciesId=$speciesId — using placeholder",
                )
            }
        }

        val placeholderName = CreatureAssetNames.placeholderStageDrawableName(suffix)
        return resolveDrawableId(
            resources = resources,
            packageName = packageName,
            drawableName = placeholderName,
            logContext = logContext.copy(expectedDrawableName = placeholderName, isPlaceholder = true),
        )
    }

    private fun logMissingDrawable(drawableName: String, context: DrawableResolveContext?) {
        if (context == null || !Log.isLoggable(TAG, Log.DEBUG)) {
            return
        }
        if (context.isPlaceholder) {
            Log.d(
                TAG,
                "Missing placeholder drawable: $drawableName (add to res/drawable in app and wear)",
            )
            return
        }
        Log.d(
            TAG,
            "Missing species drawable: $drawableName (speciesId=${context.speciesId}, slug=${context.assetSlug})",
        )
    }

    data class DrawableResolveContext(
        val speciesId: String,
        val stageName: String,
        val assetSlug: String?,
        val expectedDrawableName: String = "",
        val isPlaceholder: Boolean = false,
    )
}

/** @deprecated Use [DrawableCreatureResolver] */
@Deprecated("Renamed to DrawableCreatureResolver", ReplaceWith("DrawableCreatureResolver"))
typealias DrawableResourceResolver = DrawableCreatureResolver
