package com.gharmon255.dinostep.wear

import android.app.Application
import com.gharmon255.dinostep.wear.data.WatchStateRepository

class WearApplication : Application() {
    val watchStateRepository: WatchStateRepository by lazy {
        WatchStateRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        watchStateRepository.startListening()
    }
}
