package com.gharmon255.dinostep.model

import org.junit.Assert.assertEquals
import org.junit.Test
class EggRewardRollerTest {
    @Test
    fun rollWeighted_boundaryValues() {
        assertEquals(EggRarity.COMMON, EggRewardRoller.rollWeighted(0).eggRarity)
        assertEquals(EggRarity.COMMON, EggRewardRoller.rollWeighted(64).eggRarity)
        assertEquals(EggRarity.UNCOMMON, EggRewardRoller.rollWeighted(65).eggRarity)
        assertEquals(EggRarity.UNCOMMON, EggRewardRoller.rollWeighted(86).eggRarity)
        assertEquals(EggRarity.RARE, EggRewardRoller.rollWeighted(87).eggRarity)
        assertEquals(EggRarity.EPIC, EggRewardRoller.rollWeighted(96).eggRarity)
        assertEquals(EggRarity.LEGENDARY, EggRewardRoller.rollWeighted(99).eggRarity)
    }

    @Test
    fun rollWeighted_storesRollValue() {
        val result = EggRewardRoller.rollWeighted(42)
        assertEquals(42, result.rollValue)
        assertEquals("42", result.rollPercentLabel)
    }
}
