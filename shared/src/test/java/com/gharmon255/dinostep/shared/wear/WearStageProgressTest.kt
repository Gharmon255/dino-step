package com.gharmon255.dinostep.shared.wear

import org.junit.Assert.assertEquals
import org.junit.Test

class WearStageProgressTest {
    private val hatch = 1_600
    private val juvenile = 4_000
    private val total = 8_000

    @Test
    fun eggStage_stepsUntilHatch() {
        val info = WearStageProgress.calculate("EGG", currentSteps = 400, hatch, juvenile, total)
        assertEquals(1_200, info.stepsUntilNextStage)
        assertEquals("hatch", info.nextStageLabel)
    }

    @Test
    fun babyStage_stepsUntilJuvenile() {
        val info = WearStageProgress.calculate("BABY", currentSteps = 2_000, hatch, juvenile, total)
        assertEquals(2_000, info.stepsUntilNextStage)
        assertEquals("juvenile", info.nextStageLabel)
    }

    @Test
    fun juvenileStage_stepsUntilAdult() {
        val info = WearStageProgress.calculate("JUVENILE", currentSteps = 5_000, hatch, juvenile, total)
        assertEquals(3_000, info.stepsUntilNextStage)
        assertEquals("adult", info.nextStageLabel)
    }

    @Test
    fun adultStage_readyToClaim() {
        val info = WearStageProgress.calculate("ADULT", currentSteps = 8_000, hatch, juvenile, total)
        assertEquals(0, info.stepsUntilNextStage)
        assertEquals(WearStageProgress.LABEL_READY_TO_CLAIM, info.nextStageLabel)
    }

    @Test
    fun negativeStepDelta_clampedToZero() {
        val info = WearStageProgress.calculate("EGG", currentSteps = 2_000, hatch, juvenile, total)
        assertEquals(0, info.stepsUntilNextStage)
    }

    @Test
    fun formatDisplayLine_usesFormattedSteps() {
        val line = WearStageProgress.formatDisplayLine(500, "hatch", "500")
        assertEquals("500 to hatch", line)
    }

    @Test
    fun formatDisplayLine_adultUsesClaimLabel() {
        val line = WearStageProgress.formatDisplayLine(
            stepsUntilNextStage = 0,
            nextStageLabel = WearStageProgress.LABEL_READY_TO_CLAIM,
            formattedSteps = "0",
        )
        assertEquals(WearStageProgress.LABEL_READY_TO_CLAIM, line)
    }
}
