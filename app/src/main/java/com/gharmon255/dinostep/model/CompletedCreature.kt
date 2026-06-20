package com.gharmon255.dinostep.model

data class CompletedCreature(
    val id: Long = 0,
    val creature: CreatureDefinition,
    val stepsCompleted: Int,
    val completedAt: Long,
    val nickname: String? = null,
    val eggRarityAtHatch: EggRarity = EggRarity.COMMON,
    val exSteps: Int = 0,
    val exLevel: Int = 1,
) {
    val displayName: String
        get() = CreatureNickname.normalize(nickname) ?: creature.name
}
