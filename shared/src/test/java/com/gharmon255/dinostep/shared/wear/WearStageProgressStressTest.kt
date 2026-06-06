package com.gharmon255.dinostep.shared.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WearStageProgressStressTest {
    @Test
    fun rapidStepSimulation_neverNegativeStepsUntilNext() {
        val hatch = 2_000
        val juvenile = 5_000
        val total = 10_000
        repeat(50_000) { iteration ->
            val steps = iteration % (total + 1)
            val stage = when {
                steps < hatch -> "EGG"
                steps < juvenile -> "BABY"
                steps < total -> "JUVENILE"
                else -> "ADULT"
            }
            val info = WearStageProgress.calculate(stage, steps, hatch, juvenile, total)
            assertTrue("stepsUntil negative at $steps/$stage", info.stepsUntilNextStage >= 0)
            if (stage == "ADULT") {
                assertEquals(WearStageProgress.LABEL_READY_TO_CLAIM, info.nextStageLabel)
            }
        }
    }

    @Test
    fun formatDisplayLine_manyIterations_stableOutput() {
        repeat(10_000) { i ->
            val label = if (i % 97 == 0) WearStageProgress.LABEL_READY_TO_CLAIM else "hatch"
            val line = WearStageProgress.formatDisplayLine(i, label, i.toString())
            if (label == WearStageProgress.LABEL_READY_TO_CLAIM) {
                assertEquals(WearStageProgress.LABEL_READY_TO_CLAIM, line)
            } else {
                assertEquals("$i to hatch", line)
            }
        }
    }
}
