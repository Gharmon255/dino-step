package com.gharmon255.dinostep.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreatureNicknameTest {
    @Test
    fun normalize_trimsAndCapsLength() {
        assertEquals("Rex", CreatureNickname.normalize("  Rex  "))
        assertEquals("A".repeat(CreatureNickname.MAX_LENGTH), CreatureNickname.normalize("A".repeat(40)))
        assertNull(CreatureNickname.normalize("   "))
    }

    @Test
    fun activeDisplayName_usesNicknameAfterHatch() {
        assertEquals(
            "Spike",
            CreatureNickname.activeDisplayName(
                speciesName = "Compsognathus",
                nickname = "Spike",
                isRevealed = true,
                mysteryDisplayName = "Mystery Egg",
            ),
        )
    }

    @Test
    fun activeDisplayName_keepsMysteryEggBeforeHatch() {
        assertEquals(
            "Mystery Egg",
            CreatureNickname.activeDisplayName(
                speciesName = "Compsognathus",
                nickname = "Spike",
                isRevealed = false,
                mysteryDisplayName = "Mystery Egg",
            ),
        )
    }
}
