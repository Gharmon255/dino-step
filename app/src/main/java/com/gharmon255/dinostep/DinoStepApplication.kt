package com.gharmon255.dinostep

import android.app.Application
import com.gharmon255.dinostep.data.AppExperiencePreferences
import com.gharmon255.dinostep.data.DeveloperPreferences
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.repository.GameRepository
import com.gharmon255.dinostep.health.HealthConnectRepository
import com.gharmon255.dinostep.health.HealthStepSyncEngine
import com.gharmon255.dinostep.health.StepSyncScheduler
import com.gharmon255.dinostep.garmin.GarminCompanionPublisher
import com.gharmon255.dinostep.garmin.GarminConnectIQManager
import com.gharmon255.dinostep.garmin.GarminSdkCompanionPublisher
import com.gharmon255.dinostep.notifications.InactivityPenaltyNotifier
import com.gharmon255.dinostep.notifications.StageMilestoneNotifier
import com.gharmon255.dinostep.wear.WearDataLayerPublisher

class DinoStepApplication : Application() {
    val stageMilestoneNotifier: StageMilestoneNotifier by lazy {
        StageMilestoneNotifier(this)
    }

    val inactivityPenaltyNotifier: InactivityPenaltyNotifier by lazy {
        InactivityPenaltyNotifier(this)
    }

    val appExperiencePreferences: AppExperiencePreferences by lazy {
        AppExperiencePreferences(this)
    }

    override fun onCreate() {
        super.onCreate()
        StepSyncScheduler.schedule(this)
    }

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

    val healthStepSyncEngine: HealthStepSyncEngine by lazy {
        HealthStepSyncEngine(
            repository = gameRepository,
            healthConnectRepository = healthConnectRepository,
            appExperiencePreferences = appExperiencePreferences,
            inactivityPenaltyNotifier = inactivityPenaltyNotifier,
            wearDataLayerPublisher = wearDataLayerPublisher,
            garminCompanionPublisher = garminCompanionPublisher,
        )
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
