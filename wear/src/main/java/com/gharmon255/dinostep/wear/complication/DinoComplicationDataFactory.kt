package com.gharmon255.dinostep.wear.complication

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.GoalProgressComplicationData
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.NoDataComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import com.gharmon255.dinostep.wear.MainActivity
import com.gharmon255.dinostep.wear.R
import com.gharmon255.dinostep.wear.model.WatchCreatureState
import com.gharmon255.dinostep.wear.model.WearGrowthStage
import java.text.NumberFormat
import java.util.Locale

object DinoComplicationDataFactory {
    private val numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault())

    fun build(
        context: Context,
        type: ComplicationType,
        state: WatchCreatureState,
    ): ComplicationData {
        val tapAction = openAppPendingIntent(context)
        return when (type) {
            ComplicationType.SHORT_TEXT -> shortText(state, tapAction)
            ComplicationType.RANGED_VALUE -> rangedValue(context, state, tapAction)
            ComplicationType.GOAL_PROGRESS -> goalProgress(context, state, tapAction)
            ComplicationType.SMALL_IMAGE -> smallImage(context, state, tapAction)
            ComplicationType.LONG_TEXT -> longText(context, state, tapAction)
            else -> NoDataComplicationData()
        }
    }

    fun preview(context: Context, type: ComplicationType): ComplicationData {
        return build(
            context = context,
            type = type,
            state = WatchCreatureState(
                creatureName = "Mystery Egg",
                displayName = "Mystery Egg",
                stage = WearGrowthStage.EGG,
                currentSteps = 1200,
                nextMilestone = 5000,
                totalStepsRequired = 50_000,
                progressPercent = 24f,
                stepsUntilNextMilestone = 3800,
                stepsUntilNextStage = 3800,
                nextStageLabel = "hatch",
                isRevealed = false,
                displayEmoji = "🥚",
                eggRarity = "COMMON",
                isFromPhone = true,
            ),
        )
    }

    private fun shortText(
        state: WatchCreatureState,
        tapAction: PendingIntent,
    ): ComplicationData {
        val emoji = ComplicationCreatureIcon.centerEmoji(state)
        val description = if (state.isFromPhone) {
            "${state.displayName} ${state.progressPercent.toInt()}% ${state.stageLabel}"
        } else {
            "Dino Step waiting for phone sync"
        }
        return ShortTextComplicationData.Builder(
            text = plain(emoji),
            contentDescription = plain(description),
        )
            .setTitle(plain("${state.progressPercent.toInt()}%"))
            .setTapAction(tapAction)
            .build()
    }

    private fun rangedValue(
        context: Context,
        state: WatchCreatureState,
        tapAction: PendingIntent,
    ): ComplicationData {
        val progress = state.progressPercent.coerceIn(0f, 100f)
        return RangedValueComplicationData.Builder(
            value = progress,
            min = 0f,
            max = 100f,
            contentDescription = plain("${state.displayName} ${progress.toInt()} percent"),
        )
            .setText(plain(ComplicationCreatureIcon.centerEmoji(state)))
            .setTitle(plain("${progress.toInt()}%"))
            .setMonochromaticImage(ringCenterImage(context, state))
            .setTapAction(tapAction)
            .build()
    }

    private fun goalProgress(
        context: Context,
        state: WatchCreatureState,
        tapAction: PendingIntent,
    ): ComplicationData {
        val progress = state.progressPercent.coerceIn(0f, 100f)
        return GoalProgressComplicationData.Builder(
            value = progress,
            targetValue = 100f,
            contentDescription = plain(
                "${state.displayName} ${progress.toInt()} percent ${ComplicationCreatureIcon.centerEmoji(state)}",
            ),
        )
            .setText(plain(ComplicationCreatureIcon.centerEmoji(state)))
            .setMonochromaticImage(ringCenterImage(context, state))
            .setTapAction(tapAction)
            .build()
    }

    private fun ringCenterImage(
        context: Context,
        state: WatchCreatureState,
    ): MonochromaticImage {
        return MonochromaticImage.Builder(ComplicationCreatureIcon.resolveIcon(context, state)).build()
    }

    private fun smallImage(
        context: Context,
        state: WatchCreatureState,
        tapAction: PendingIntent,
    ): ComplicationData {
        val icon = ComplicationCreatureIcon.resolveIcon(context, state)
        return SmallImageComplicationData.Builder(
            smallImage = SmallImage.Builder(icon, SmallImageType.ICON).build(),
            contentDescription = plain(state.displayName),
        )
            .setTapAction(tapAction)
            .build()
    }

    private fun longText(
        context: Context,
        state: WatchCreatureState,
        tapAction: PendingIntent,
    ): ComplicationData {
        val body = if (state.isFromPhone) {
            "${ComplicationCreatureIcon.centerEmoji(state)} ${state.displayName}\n" +
                "${state.stageLabel} · ${state.progressPercent.toInt()}%\n" +
                state.stepsUntilNextStageDisplay(numberFormat)
        } else {
            context.getString(R.string.complication_waiting)
        }
        return LongTextComplicationData.Builder(
            text = plain(body),
            contentDescription = plain(body),
        )
            .setTitle(plain("Dino Step"))
            .setTapAction(tapAction)
            .build()
    }

    private fun plain(value: String): PlainComplicationText {
        return PlainComplicationText.Builder(value).build()
    }

    private fun openAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
