package com.gharmon255.dinostep.wear.model

enum class WearGrowthStage {
    EGG,
    BABY,
    JUVENILE,
    ADULT,
    ;

    companion object {
        fun fromRaw(value: String?): WearGrowthStage {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: EGG
        }
    }
}
