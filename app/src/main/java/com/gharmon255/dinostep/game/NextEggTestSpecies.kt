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
    GALLIMIMUS("Gallimimus", "gallimimus", "gallimimus"),
    BARYONYX("Baryonyx", "baryonyx", "baryonyx"),
    VELOCIRAPTOR_ALPHA("Velociraptor Alpha", "velociraptor_alpha", "velociraptor_alpha"),
    THERIZINOSAURUS("Therizinosaurus", "therizinosaurus", "therizinosaurus"),
    GIGANOTOSAURUS("Giganotosaurus", "giganotosaurus", "giganotosaurus"),
    QUETZALCOATLUS("Quetzalcoatlus", "quetzalcoatlus", "quetzalcoatlus"),
    INDOMINUS_HYBRID("Indominus Hybrid", "indominus_hybrid", "indominus_hybrid"),
    ANCIENT_SPINOSAURUS("Ancient Spinosaurus", "ancient_spinosaurus", "ancient_spinosaurus"),
    FROST_RAPTOR("Frost Raptor", "frost_raptor", "frost_raptor"),
    VOLCANIC_T_REX("Volcanic T-Rex", "volcanic_t_rex", "volcanic_t_rex"),
    SHADOW_TRICERATOPS("Shadow Triceratops", "shadow_triceratops", "shadow_triceratops"),
    COSMIC_PTERODACTYL("Cosmic Pterodactyl", "cosmic_pterodactyl", "cosmic_pterodactyl"),
    TITANOSAUR("Titanosaur", "titanosaur", "titanosaur"),
    ANCIENT_APEX_REX("Ancient Apex Rex", "ancient_apex_rex", "ancient_apex_rex"),
    COMPSOGNATHUS("Compsognathus", "compsognathus", "compsognathus"),
    PLESIOSAURUS("Plesiosaurus", "plesiosaurus", "plesiosaurus"),
    DIPLODOCUS("Diplodocus", "diplodocus", "diplodocus"),
    CRYSTAL_CERATOSAURUS("Crystal Ceratosaurus", "crystal_ceratosaurus", "crystal_ceratosaurus"),
    ABYSSAL_MOSASAURUS("Abyssal Mosasaurus", "abyssal_mosasaurus", "abyssal_mosasaurus"),
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
                "gallimimus" -> GALLIMIMUS
                "baryonyx" -> BARYONYX
                "velociraptor_alpha" -> VELOCIRAPTOR_ALPHA
                "therizinosaurus" -> THERIZINOSAURUS
                "giganotosaurus" -> GIGANOTOSAURUS
                "quetzalcoatlus" -> QUETZALCOATLUS
                "indominus_hybrid", "indominus_rex_style_hybrid" -> INDOMINUS_HYBRID
                "ancient_spinosaurus" -> ANCIENT_SPINOSAURUS
                "frost_raptor" -> FROST_RAPTOR
                "volcanic_t_rex" -> VOLCANIC_T_REX
                "shadow_triceratops" -> SHADOW_TRICERATOPS
                "cosmic_pterodactyl" -> COSMIC_PTERODACTYL
                "titanosaur" -> TITANOSAUR
                "ancient_apex_rex" -> ANCIENT_APEX_REX
                "compsognathus" -> COMPSOGNATHUS
                "plesiosaurus" -> PLESIOSAURUS
                "diplodocus" -> DIPLODOCUS
                "crystal_ceratosaurus" -> CRYSTAL_CERATOSAURUS
                "abyssal_mosasaurus" -> ABYSSAL_MOSASAURUS
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
