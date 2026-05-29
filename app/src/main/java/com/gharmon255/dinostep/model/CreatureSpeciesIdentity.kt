package com.gharmon255.dinostep.model

/**
 * Stable placeholder identity per creature id. Same [emoji] for Baby, Juvenile, and Adult.
 *
 * TODO: Replace emoji with drawable from [CreatureDefinition] asset keys when PNG/WebP exist.
 */
data class CreatureSpeciesProfile(
    val creatureId: String,
    val emoji: String,
    val shortLabel: String,
)

object CreatureSpeciesIdentity {
    private const val DEFAULT_EMOJI = "🦖"

    private val byId: Map<String, CreatureSpeciesProfile> = listOf(
        profile("tiny_raptor", "🦖", "TR"),
        profile("triceratops", "🦕", "TC"),
        profile("ankylosaurus", "🦕", "AN"),
        profile("parasaurolophus", "🦕", "PR"),
        profile("pachycephalosaurus", "🦕", "PC"),
        profile("gallimimus", "🦖", "GM"),
        profile("stegosaurus", "🦕", "ST"),
        profile("pterodactyl", "🦖", "PT"),
        profile("dilophosaurus", "🦖", "DL"),
        profile("iguanodon", "🦕", "IG"),
        profile("carnotaurus", "🦖", "CA"),
        profile("baryonyx", "🦖", "BY"),
        profile("t_rex", "🦖", "RX"),
        profile("spinosaurus", "🦖", "SP"),
        profile("velociraptor_alpha", "🦖", "VA"),
        profile("allosaurus", "🦖", "AL"),
        profile("therizinosaurus", "🦕", "TH"),
        profile("mosasaurus", "🦖", "MO"),
        profile("giganotosaurus", "🦖", "GG"),
        profile("quetzalcoatlus", "🦖", "QZ"),
        profile("indominus_hybrid", "🦖", "IH"),
        profile("ancient_spinosaurus", "🦖", "AS"),
        profile("volcanic_t_rex", "🦖", "VT"),
        profile("frost_raptor", "🦖", "FR"),
        profile("shadow_triceratops", "🦕", "SH"),
        profile("titanosaur", "🦕", "TI"),
        profile("cosmic_pterodactyl", "🦖", "CP"),
        profile("ancient_apex_rex", "🦖", "AR"),
    ).associateBy { it.creatureId }

    fun forId(creatureId: String): CreatureSpeciesProfile {
        return byId[creatureId] ?: CreatureSpeciesProfile(
            creatureId = creatureId,
            emoji = DEFAULT_EMOJI,
            shortLabel = creatureId.take(3).uppercase(),
        )
    }

    private fun profile(creatureId: String, emoji: String, shortLabel: String): CreatureSpeciesProfile {
        return CreatureSpeciesProfile(creatureId = creatureId, emoji = emoji, shortLabel = shortLabel)
    }
}
