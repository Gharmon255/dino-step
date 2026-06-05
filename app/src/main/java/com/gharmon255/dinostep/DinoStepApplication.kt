package com.gharmon255.dinostep

import android.app.Application
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.garmin.GarminConnectIQManager
import com.gharmon255.dinostep.garmin.GarminSdkCompanionPublisher
import com.gharmon255.dinostep.wear.WearDataLayerPublisher

class DinoStepApplication : Application() {
    override fun onTerminate() {
        garminConnectIQManager.shutdown()
        super.onTerminate()
    }
    val developerPreferences: DeveloperPreferences by lazy {
        DeveloperPreferences(this)
    }

    val gameRepository: GameRepository by lazy {
        GameRepository(DinoStepDatabase.getInstance(this))
    }

    val healthConnectRepository: HealthConnectRepository by lazy {
        HealthConnectRepository(this)
    }

    val wearDataLayerPublisher: WearDataLayerPublisher by lazy {
        WearDataLayerPublisher(this)
    }

    val garminConnectIQManager: GarminConnectIQManager by lazy {
        GarminConnectIQManager(this)
    }

    val garminCompanionPublisher: GarminCompanionPublisher by lazy {
        GarminSdkCompanionPublisher(garminConnectIQManager)
    }
}
