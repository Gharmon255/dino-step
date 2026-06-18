package com.gharmon255.dinostep.model

data class CompletedCreature(
    val id: Long = 0,
    val creature: CreatureDefinition,
    val stepsCompleted: Int,
    val completedAt: Long,
    val nickname: String? = null,
) {
    val displayName: String
        get() = CreatureNickname.normalize(nickname) ?: creature.name
}
