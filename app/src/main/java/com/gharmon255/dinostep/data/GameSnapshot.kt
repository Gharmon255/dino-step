package com.gharmon255.dinostep.data

import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.PlayerStats

data class GameSnapshot(
    val activeCreature: ActiveCreatureState,
    val collection: List<CompletedCreature>,
    val playerStats: PlayerStats,
)
