package com.gharmon255.dinostep.game

import com.gharmon255.dinostep.model.CreatureCatalog

enum class NextEggTestSpecies(
    val displayName: String,
    val storageValue: String?,
    val forcedCreatureId: String?,
) {
    RANDOM("Random / Normal", null, null),
    TINY_RAPTOR("Tiny Raptor", "tiny_raptor", "tiny_raptor"),
    TRICERATOPS("Triceratops", "triceratops", "triceratops"),
    T_REX("T-Rex", "trex", "trex"),
    STEGOSAURUS("Stegosaurus", "stegosaurus", "stegosaurus"),
    BRACHIOSAURUS("Brachiosaurus", "brachiosaurus", "brachiosaurus"),
    ANKYLOSAURUS("Ankylosaurus", "ankylosaurus", "ankylosaurus"),
    PARASAUROLOPHUS("Parasaurolophus", "parasaurolophus", "parasaurolophus"),
    SPINOSAURUS("Spinosaurus", "spinosaurus", "spinosaurus"),
    PTERANODON("Pteranodon", "pteranodon", "pteranodon"),
    DILOPHOSAURUS("Dilophosaurus", "dilophosaurus", "dilophosaurus"),
    CARNOTAURUS("Carnotaurus", "carnotaurus", "carnotaurus"),
    MOSASAURUS("Mosasaurus", "mosasaurus", "mosasaurus"),
    PACHYCEPHALOSAURUS("Pachycephalosaurus", "pachycephalosaurus", "pachycephalosaurus"),
    ALLOSAURUS("Allosaurus", "allosaurus", "allosaurus"),
    IGUANODON("Iguanodon", "iguanodon", "iguanodon"),
    ;

    companion object {
        val selectableOptions: List<NextEggTestSpecies> = entries.toList()

        fun fromStorageValue(raw: String?): NextEggTestSpecies {
            if (raw.isNullOrBlank()) {
                return RANDOM
            }
            entries.firstOrNull { it.storageValue == raw }?.let { return it }
            entries.firstOrNull { it.displayName.equals(raw, ignoreCase = true) }?.let { return it }
            return fromLegacyStorageAlias(raw) ?: RANDOM
        }

        /** Old saves may have stored display labels or pre-roster ids instead of stable species ids. */
        private fun fromLegacyStorageAlias(raw: String): NextEggTestSpecies? {
            val normalized = raw.trim().lowercase().replace('-', '_').replace(' ', '_')
            return when (normalized) {
                "t_rex", "trex", "tyrannosaurus", "tyrannosaurus_rex" -> T_REX
                "pterodactyl", "pteranodon" -> PTERANODON
                "tiny_raptor" -> TINY_RAPTOR
                "triceratops" -> TRICERATOPS
                "stegosaurus" -> STEGOSAURUS
                "brachiosaurus" -> BRACHIOSAURUS
                "ankylosaurus" -> ANKYLOSAURUS
                "parasaurolophus" -> PARASAUROLOPHUS
                "spinosaurus" -> SPINOSAURUS
                "dilophosaurus" -> DILOPHOSAURUS
                "carnotaurus" -> CARNOTAURUS
                "mosasaurus" -> MOSASAURUS
                "pachycephalosaurus" -> PACHYCEPHALOSAURUS
                "allosaurus" -> ALLOSAURUS
                "iguanodon" -> IGUANODON
                else -> null
            }
        }
    }

    /** Non-null only when a specific test species is selected (not Random / Normal). */
    fun testSpeciesOverrideId(): String? {
        val id = forcedCreatureId ?: return null
        return if (CreatureCatalog.byId(id) != null) {
            id
        } else {
            null
        }
    }
}
