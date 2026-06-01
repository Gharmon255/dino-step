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
    ;

    companion object {
        val selectableOptions: List<NextEggTestSpecies> = entries.toList()

        fun fromStorageValue(raw: String?): NextEggTestSpecies {
            if (raw.isNullOrBlank()) {
                return RANDOM
            }
            return entries.firstOrNull { it.storageValue == raw } ?: RANDOM
        }
    }

    fun resolveForcedCreatureId(): String? {
        val id = forcedCreatureId ?: return null
        return if (CreatureCatalog.byId(id) != null) {
            id
        } else {
            null
        }
    }
}
