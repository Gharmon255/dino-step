package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.EggRarity
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.model.Rarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DuplicateTradeLogicTest {
    private val tinyRaptor = CreatureCatalog.tinyRaptor

    private fun adultTinyRaptor(steps: Int = tinyRaptor.totalStepsRequired): ActiveCreatureState =
        ActiveCreatureState(
            creature = tinyRaptor,
            eggRarity = EggRarity.COMMON,
            steps = steps,
            isRevealed = true,
        )

    private fun completedTinyRaptor(
        id: Long = 1,
        completedAt: Long = 1_000L,
    ): CompletedCreature = CompletedCreature(
        id = id,
        creature = tinyRaptor,
        stepsCompleted = tinyRaptor.totalStepsRequired,
        completedAt = completedAt,
    )

    @Test
    fun offer_noStoredCopy_notEligible() {
        assertNull(DuplicateTradeLogic.offer(adultTinyRaptor(), emptyList()))
    }

    @Test
    fun offer_oneStoredCopy_secondAdultEligible() {
        val offer = DuplicateTradeLogic.offer(adultTinyRaptor(), listOf(completedTinyRaptor()))
        assertEquals("tiny_raptor", offer?.speciesId)
        assertEquals(1, offer?.storedCount)
        assertEquals(EggRarity.UNCOMMON, offer?.rewardEggRarity)
    }

    @Test
    fun offer_twoStoredCopies_thirdAdultEligible() {
        val offer = DuplicateTradeLogic.offer(
            adultTinyRaptor(),
            listOf(
                completedTinyRaptor(id = 1, completedAt = 100L),
                completedTinyRaptor(id = 2, completedAt = 200L),
            ),
        )
        assertEquals(2, offer?.storedCount)
        assertEquals(EggRarity.UNCOMMON, offer?.rewardEggRarity)
    }

    @Test
    fun offer_differentSpecies_notEligible() {
        val active = ActiveCreatureState(
            creature = CreatureCatalog.stegosaurus,
            eggRarity = EggRarity.UNCOMMON,
            steps = CreatureCatalog.stegosaurus.totalStepsRequired,
            isRevealed = true,
        )
        assertNull(DuplicateTradeLogic.offer(active, listOf(completedTinyRaptor())))
    }

    @Test
    fun offer_notAdult_notEligible() {
        val juvenile = adultTinyRaptor(steps = tinyRaptor.juvenileStep)
        assertEquals(GrowthStage.JUVENILE, juvenile.stage)
        assertNull(DuplicateTradeLogic.offer(juvenile, listOf(completedTinyRaptor())))
    }

    @Test
    fun offer_legendarySpecies_notEligible() {
        val apex = CreatureCatalog.ancientApexRex
        val active = ActiveCreatureState(
            creature = apex,
            eggRarity = EggRarity.LEGENDARY,
            steps = apex.totalStepsRequired,
            isRevealed = true,
        )
        val stored = CompletedCreature(
            id = 9,
            creature = apex,
            stepsCompleted = apex.totalStepsRequired,
            completedAt = 500L,
        )
        assertNull(DuplicateTradeLogic.offer(active, listOf(stored)))
    }

    @Test
    fun nextEggRarity_tiers() {
        assertEquals(EggRarity.UNCOMMON, DuplicateTradeLogic.nextEggRarity(Rarity.COMMON))
        assertEquals(EggRarity.RARE, DuplicateTradeLogic.nextEggRarity(Rarity.UNCOMMON))
        assertEquals(EggRarity.EPIC, DuplicateTradeLogic.nextEggRarity(Rarity.RARE))
        assertEquals(EggRarity.LEGENDARY, DuplicateTradeLogic.nextEggRarity(Rarity.EPIC))
        assertNull(DuplicateTradeLogic.nextEggRarity(Rarity.LEGENDARY))
    }

    @Test
    fun removeOneCompleted_removesOldestMatchingSpecies() {
        val stego = CreatureCatalog.stegosaurus
        val collection = listOf(
            completedTinyRaptor(id = 1, completedAt = 300L),
            completedTinyRaptor(id = 2, completedAt = 100L),
            CompletedCreature(
                id = 3,
                creature = stego,
                stepsCompleted = stego.totalStepsRequired,
                completedAt = 200L,
            ),
        )

        val result = DuplicateTradeLogic.removeOneCompleted("tiny_raptor", collection)
        assertTrue(result != null)
        assertEquals(2, result!!.first.size)
        assertEquals(2L, result.second.id)
        assertEquals(1, DuplicateTradeLogic.collectionCount("tiny_raptor", result.first))
    }
}
