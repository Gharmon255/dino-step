package com.gharmon255.dinostep.wear.model

enum class WearGrowthStage {
    EGG,
    BABY,
    JUVENILE,
    ADULT,
    ;

    companion object {
        /**
         * Unknown or missing stage strings default to [EGG] so UI stays on egg/emoji fallbacks
         * instead of crashing (legacy or partial payloads).
         */
        fun fromRaw(value: String?): WearGrowthStage {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: EGG
        }
    }
}
