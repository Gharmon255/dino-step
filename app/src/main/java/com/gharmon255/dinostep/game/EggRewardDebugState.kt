package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.EggRarity

data class EggRewardDebugState(
    val lastRewardedEggRarity: EggRarity? = null,
    val lastRewardRollValue: Int? = null,
)
