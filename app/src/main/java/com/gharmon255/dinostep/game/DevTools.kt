package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.BuildConfig

/** True for debug builds; release hides destructive developer UI. */
object DevTools {
    val isEnabled: Boolean = BuildConfig.DEBUG
}
