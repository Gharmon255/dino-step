package com.gharmon255.dinostep.model

/**
 * Placeholder or future drawable mapping for one growth stage.
 *
 * [speciesEmoji] is identical for Baby/Juvenile/Adult for a given creature id.
 * [stageScale] and [stageDetailLabel] communicate growth without changing species.
 *
 * TODO: Load [assetKey] from res/drawable when PNG/WebP assets are added.
 */
data class StageVisual(
    val assetKey: String,
    /** Same species emoji across post-egg stages; egg stage uses [displayEmoji]. */
    val speciesEmoji: String,
    /** Emoji or symbol shown in the UI for this stage. */
    val displayEmoji: String,
    val speciesShortLabel: String,
    val stageDetailLabel: String,
    val stageScale: Float,
) {
    /** @deprecated Use [displayEmoji] */
    val placeholderEmoji: String
        get() = displayEmoji
}
