package com.gharmon255.dinostep.health

sealed interface HealthConnectUiStatus {
    val message: String

    data object Unavailable : HealthConnectUiStatus {
        override val message: String =
            "Health Connect is not available on this device. Use fake step buttons to test."
    }

    data object PermissionRequired : HealthConnectUiStatus {
        override val message: String =
            "Allow Dino Step to read steps in Health Connect to sync your progress."
    }

    data object Ready : HealthConnectUiStatus {
        override val message: String =
            "Health Connect is connected. Tap Sync Steps on Home to apply today's steps."
    }

    data class Error(
        override val message: String,
    ) : HealthConnectUiStatus
}
