package com.gharmon255.dinostep.data.repository

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.PlayerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameRepository(
    private val database: DinoStepDatabase,
) {
    private val activeCreatureDao = database.activeCreatureDao()
    private val completedCreatureDao = database.completedCreatureDao()
    private val playerStatsDao = database.playerStatsDao()

    suspend fun loadOrCreateGame(): GameSnapshot = withContext(Dispatchers.IO) {
        val statsEntity = playerStatsDao.getById() ?: PlayerStatsEntity().also {
            playerStatsDao.upsert(it)
        }

        val collection = completedCreatureDao
            .getAllOrderedByCompletedAt()
            .mapNotNull { it.toDomain() }

        val activeCreature = activeCreatureDao.getById()?.toDomain()
            ?: createMysteryCommonEgg().also { saveActiveCreature(it) }

        GameSnapshot(
            activeCreature = activeCreature,
            collection = collection,
            playerStats = statsEntity.toDomain(),
        )
    }

    suspend fun saveActiveCreature(activeCreature: ActiveCreatureState) = withContext(Dispatchers.IO) {
        activeCreatureDao.upsert(activeCreature.toEntity())
    }

    suspend fun saveCompletedCreature(completedCreature: CompletedCreature) = withContext(Dispatchers.IO) {
        completedCreatureDao.insert(completedCreature.toEntity())
    }

    suspend fun savePlayerStats(playerStats: PlayerStats) = withContext(Dispatchers.IO) {
        playerStatsDao.upsert(playerStats.toEntity())
    }

    fun createMysteryCommonEgg(): ActiveCreatureState {
        return ActiveCreatureState(
            creature = CreatureCatalog.randomCommonCreature(),
            steps = 0,
            startedAt = System.currentTimeMillis(),
            isRevealed = false,
        )
    }
}
