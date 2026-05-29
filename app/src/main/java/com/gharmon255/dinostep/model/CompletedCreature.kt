package com.gharmon255.dinostep.model

data class CompletedCreature(
    val creature: CreatureDefinition,
    val stepsCompleted: Int,
    val completedAt: Long,
)
