package com.gharmon255.dinostep.shared.wear

fun WearCreaturePayload.toLogString(): String {
    return buildString {
        append("displayName=").append(displayName)
        append(", creatureName=").append(creatureName)
        append(", stage=").append(stage)
        append(", currentSteps=").append(currentSteps)
        append(", nextMilestone=").append(nextMilestone)
        append(", totalStepsRequired=").append(totalStepsRequired)
        append(", progressPercent=").append(progressPercent)
        append(", stepsUntilNextMilestone=").append(stepsUntilNextMilestone)
        append(", isRevealed=").append(isRevealed)
        append(", emoji=").append(displayEmoji)
        append(", eventType=").append(eventType)
        append(", updatedAt=").append(updatedAtMillis)
    }
}
