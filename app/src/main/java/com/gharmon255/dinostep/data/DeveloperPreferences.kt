package com.gharmon255.dinostep.data

import android.content.Context
import com.gharmon255.dinostep.game.NextEggTestSpecies

class DeveloperPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getNextEggTestSpecies(): NextEggTestSpecies {
        val raw = prefs.getString(KEY_NEXT_EGG_TEST_SPECIES, null)
        return NextEggTestSpecies.fromStorageValue(raw)
    }

    fun setNextEggTestSpecies(selection: NextEggTestSpecies) {
        prefs.edit()
            .putString(KEY_NEXT_EGG_TEST_SPECIES, selection.storageValue)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "dino_step_developer"
        private const val KEY_NEXT_EGG_TEST_SPECIES = "next_egg_test_species"
    }
}
