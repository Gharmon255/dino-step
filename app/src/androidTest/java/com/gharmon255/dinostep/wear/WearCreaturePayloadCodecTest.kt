package com.gharmon255.dinostep.wear

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gharmon255.dinostep.shared.wear.WearCreaturePayload
import com.gharmon255.dinostep.shared.wear.WearCreaturePayloadCodec
import com.gharmon255.dinostep.shared.wear.WearStageProgress
import com.gharmon255.dinostep.shared.wear.WearSyncEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearCreaturePayloadCodecTest {
    @Test
    fun roundTrip_preservesStageProgressFields() {
        val original = WearCreaturePayload(
            creatureId = "trex",
            creatureName = "T-Rex",
            displayName = "T-Rex",
            stage = "BABY",
            currentSteps = 12_000,
            nextMilestone = 25_000,
            totalStepsRequired = 50_000,
            progressPercent = 13.3f,
            stepsUntilNextMilestone = 13_000,
            stepsUntilNextStage = 13_000,
            nextStageLabel = "juvenile",
            isRevealed = true,
            displayEmoji = "🦖",
            speciesShortLabel = "TR",
            stageScale = 0.9f,
            eggRarity = "RARE",
            creatureRarity = "RARE",
            accentColorArgb = 0xFFFF0000,
            isAssetBacked = true,
            stageDrawableKey = "dino_trex_baby",
            eventType = WearSyncEventType.CREATURE_UPDATE,
            updatedAtMillis = 1_700_000_000_000L,
        )

        val dataMap = WearCreaturePayloadCodec.toDataMap(original)
        val decoded = WearCreaturePayloadCodec.fromDataMap(dataMap)

        assertNotNull(decoded)
        assertEquals(original.creatureId, decoded!!.creatureId)
        assertEquals(original.stage, decoded.stage)
        assertEquals(original.stepsUntilNextStage, decoded.stepsUntilNextStage)
        assertEquals(original.nextStageLabel, decoded.nextStageLabel)
        assertEquals(original.stageDrawableKey, decoded.stageDrawableKey)
    }

    @Test
    fun legacyPayload_resolvesStageProgress() {
        val dataMap = WearCreaturePayloadCodec.toDataMap(
            WearCreaturePayload(
                creatureName = "Mystery",
                displayName = "Mystery Egg",
                stage = "EGG",
                currentSteps = 100,
                nextMilestone = 1_600,
                totalStepsRequired = 8_000,
                progressPercent = 6.25f,
                stepsUntilNextMilestone = 1_500,
                stepsUntilNextStage = 1_500,
                nextStageLabel = "hatch",
                isRevealed = false,
                displayEmoji = "🥚",
                eventType = WearSyncEventType.CREATURE_UPDATE,
            ),
        )
        dataMap.remove("steps_until_next_stage")
        dataMap.remove("next_stage_label")
        dataMap.putInt("steps_until_next_milestone", 1_500)
        dataMap.putString("next_stage_goal", "hatch")

        val decoded = WearCreaturePayloadCodec.fromDataMap(dataMap)
        assertNotNull(decoded)
        assertEquals(1_500, decoded!!.stepsUntilNextStage)
        assertEquals("hatch", decoded.nextStageLabel)
    }

    @Test
    fun adultStage_mapsToReadyToClaim() {
        val info = WearStageProgress.calculate("ADULT", 50_000, 10_000, 25_000, 50_000)
        assertEquals(WearStageProgress.LABEL_READY_TO_CLAIM, info.nextStageLabel)
    }
}
