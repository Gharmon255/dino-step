package com.gharmon255.dinostep.health

sealed interface HealthConnectUiStatus {
    val message: String

    data object Unavailable : HealthConnectUiStatus {
        override val message: String =
            "Health Connect is not installed or not supported on this device. " +
                "Install it from the Play Store to sync steps from your fitness apps."
    }

    data object PermissionRequired : HealthConnectUiStatus {
        override val message: String =
            "Dino Step reads your step count from Health Connect to hatch and grow dinosaurs. " +
                "Steps apply only when you tap Sync Steps on Home — not automatically in the background. " +
                "We do not read location or use your steps for ads."
    }

    data object Ready : HealthConnectUiStatus {
        override val message: String =
            "Health Connect is connected. Tap Sync Steps on Home to apply today's walking steps " +
                "to your egg or dinosaur."
    }

    data class Error(
        override val message: String,
    ) : HealthConnectUiStatus
}
