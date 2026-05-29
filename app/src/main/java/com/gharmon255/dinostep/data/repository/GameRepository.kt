package com.gharmon255.dinostep.data.repository

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
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
            .map { it.toDomain() }

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
        return createMysteryEgg(EggRarity.COMMON)
    }

    fun createMysteryEgg(eggRarity: EggRarity): ActiveCreatureState {
        return ActiveCreatureState(
            creature = CreatureCatalog.randomCreatureForEgg(eggRarity),
            steps = 0,
            startedAt = System.currentTimeMillis(),
            isRevealed = false,
        )
    }

    suspend fun clearCollection() = withContext(Dispatchers.IO) {
        completedCreatureDao.deleteAll()
        val statsEntity = playerStatsDao.getById() ?: return@withContext
        playerStatsDao.upsert(
            statsEntity.copy(creaturesCompleted = 0),
        )
    }

    suspend fun resetGameForTesting(): GameSnapshot = withContext(Dispatchers.IO) {
        completedCreatureDao.deleteAll()

        val existingStats = playerStatsDao.getById() ?: PlayerStatsEntity()
        val resetStats = existingStats.copy(
            totalFakeStepsAdded = 0,
            eggsHatched = 0,
            creaturesCompleted = 0,
        )
        playerStatsDao.upsert(resetStats)

        val newEgg = createMysteryCommonEgg()
        activeCreatureDao.upsert(newEgg.toEntity())

        GameSnapshot(
            activeCreature = newEgg,
            collection = emptyList(),
            playerStats = resetStats.toDomain(),
        )
    }
}
