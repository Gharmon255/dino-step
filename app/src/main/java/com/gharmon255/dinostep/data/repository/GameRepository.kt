package com.gharmon255.dinostep.data.repository

import com.gharmon255.dinostep.data.GameSnapshot
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.local.entity.PlayerStatsEntity
import com.gharmon255.dinostep.data.toDomain
import com.gharmon255.dinostep.data.toEntity
import com.gharmon255.dinostep.data.withLegacyV1SnapshotIfMissing
import com.gharmon255.dinostep.game.ActiveCreatureState
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureNickname
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.EggRewardRoller
import com.gharmon255.dinostep.model.EggSpeciesRoller
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

        val activeEntity = activeCreatureDao.getById()
        val migratedEntity = activeEntity?.withLegacyV1SnapshotIfMissing()
        if (migratedEntity != null && migratedEntity != activeEntity) {
            activeCreatureDao.upsert(migratedEntity)
        }

        val activeCreature = migratedEntity?.toDomain()
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

    suspend fun updateCompletedCreatureNickname(id: Long, nickname: String?) = withContext(Dispatchers.IO) {
        if (id > 0) {
            completedCreatureDao.updateNickname(id, CreatureNickname.normalize(nickname))
        }
    }

    suspend fun deleteCompletedCreature(id: Long) = withContext(Dispatchers.IO) {
        if (id > 0) {
            completedCreatureDao.deleteById(id)
        }
    }

    suspend fun savePlayerStats(playerStats: PlayerStats) = withContext(Dispatchers.IO) {
        playerStatsDao.upsert(playerStats.toEntity())
    }

    fun getRandomSpeciesForRarity(
        eggRarity: EggRarity,
        excludeSpeciesIds: Set<String> = emptySet(),
        collectedSpeciesIds: Set<String> = emptySet(),
    ): CreatureDefinition {
        return EggSpeciesRoller.rollSpecies(
            eggRarity = eggRarity,
            excludeSpeciesIds = excludeSpeciesIds,
            collectedSpeciesIds = collectedSpeciesIds,
        )
    }

    fun createRandomEggWithRarity(
        eggRarity: EggRarity,
        excludeSpeciesIds: Set<String> = emptySet(),
        collectedSpeciesIds: Set<String> = emptySet(),
    ): ActiveCreatureState {
        return newMysteryEgg(
            creature = getRandomSpeciesForRarity(
                eggRarity = eggRarity,
                excludeSpeciesIds = excludeSpeciesIds,
                collectedSpeciesIds = collectedSpeciesIds,
            ),
            eggRarity = eggRarity,
        )
    }

    fun createRandomEgg(
        excludeSpeciesIds: Set<String> = emptySet(),
        collectedSpeciesIds: Set<String> = emptySet(),
    ): ActiveCreatureState {
        val roll = EggRewardRoller.rollWeighted()
        return createRandomEggWithRarity(
            eggRarity = roll.eggRarity,
            excludeSpeciesIds = excludeSpeciesIds,
            collectedSpeciesIds = collectedSpeciesIds,
        )
    }

    fun createForcedSpeciesEgg(speciesId: String): ActiveCreatureState {
        val creature = CreatureCatalog.byId(speciesId)
            ?: getRandomSpeciesForRarity(EggRarity.COMMON)
        val eggRarity = EggRarity.valueOf(creature.rarity.name)
        return newMysteryEgg(creature = creature, eggRarity = eggRarity)
    }

    fun createMysteryCommonEgg(): ActiveCreatureState {
        return createRandomEggWithRarity(EggRarity.COMMON)
    }

    suspend fun clearCollection() = withContext(Dispatchers.IO) {
        completedCreatureDao.deleteAll()
        val statsEntity = playerStatsDao.getById() ?: return@withContext
        playerStatsDao.upsert(
            statsEntity.copy(creaturesCompleted = 0),
        )
    }

    suspend fun replaceGameSnapshot(snapshot: GameSnapshot) = withContext(Dispatchers.IO) {
        activeCreatureDao.upsert(snapshot.activeCreature.toEntity())
        completedCreatureDao.deleteAll()
        snapshot.collection.forEach { completedCreatureDao.insert(it.toEntity()) }
        playerStatsDao.upsert(snapshot.playerStats.toEntity())
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

    private fun newMysteryEgg(
        creature: CreatureDefinition,
        eggRarity: EggRarity,
    ): ActiveCreatureState {
        return ActiveCreatureState.newEgg(creature = creature, eggRarity = eggRarity)
    }
}
