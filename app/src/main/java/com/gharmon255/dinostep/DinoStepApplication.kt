package com.gharmon255.dinostep

import android.app.Application
import com.gharmon255.dinostep.data.local.DinoStepDatabase
import com.gharmon255.dinostep.data.repository.GameRepository

class DinoStepApplication : Application() {
    val gameRepository: GameRepository by lazy {
        GameRepository(DinoStepDatabase.getInstance(this))
    }
}
