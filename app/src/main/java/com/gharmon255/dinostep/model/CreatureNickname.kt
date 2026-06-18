package com.gharmon255.dinostep.model

object CreatureNickname {
    const val MAX_LENGTH = 24

    fun normalize(raw: String?): String? {
        val trimmed = raw?.trim()?.take(MAX_LENGTH).orEmpty()
        return trimmed.ifEmpty { null }
    }

    fun activeDisplayName(
        speciesName: String,
        nickname: String?,
        isRevealed: Boolean,
        mysteryDisplayName: String,
    ): String {
        if (!isRevealed) {
            return mysteryDisplayName
        }
        return normalize(nickname) ?: speciesName
    }

    fun speciesSubtitle(
        speciesName: String,
        nickname: String?,
        isRevealed: Boolean,
    ): String? {
        if (!isRevealed || normalize(nickname) == null) {
            return null
        }
        return speciesName
    }
}
