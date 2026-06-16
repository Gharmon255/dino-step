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
            "Stepasaurus reads your step count from Health Connect to hatch and grow dinosaurs. " +
                "Steps sync when you open the app and about once per hour in the background. " +
                "We do not read location or use your steps for ads."
    }

    data object Ready : HealthConnectUiStatus {
        override val message: String =
            "Health Connect is connected. Steps sync automatically about every hour. " +
                "Tap Sync Now on Home anytime for an immediate refresh."
    }

    data class Error(
        override val message: String,
    ) : HealthConnectUiStatus
}
