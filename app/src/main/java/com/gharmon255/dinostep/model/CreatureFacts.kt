package com.gharmon255.dinostep.model

object CreatureFacts {
    fun forSpecies(speciesId: String): String {
        return facts[speciesId] ?: "Paleontologists are still learning secrets about this dinosaur."
    }

    fun growthStageNote(creature: CreatureDefinition, stage: GrowthStage): String = when (stage) {
        GrowthStage.EGG ->
            "Every ${creature.name} begins as a mystery egg — walk ${creature.hatchStep.formatSteps()} steps to hatch."
        GrowthStage.BABY ->
            "Freshly hatched! Baby ${creature.name} is small but full of energy."
        GrowthStage.JUVENILE ->
            "At ${creature.juvenileStep.formatSteps()} steps, ${creature.name} hits a growth spurt as a juvenile."
        GrowthStage.ADULT -> forSpecies(creature.id)
    }

    fun stepMilestoneLabel(creature: CreatureDefinition, stage: GrowthStage): String = when (stage) {
        GrowthStage.EGG -> "${creature.hatchStep.formatSteps()} steps to hatch"
        GrowthStage.BABY -> "From ${creature.hatchStep.formatSteps()} steps"
        GrowthStage.JUVENILE -> "From ${creature.juvenileStep.formatSteps()} steps"
        GrowthStage.ADULT -> "${creature.totalStepsRequired.formatSteps()} steps total"
    }

    private fun Int.formatSteps(): String = "%,d".format(this)

    private val facts: Map<String, String> = mapOf(
        "tiny_raptor" to "Small raptors were fast runners and may have hunted in groups.",
        "triceratops" to "Triceratops had three horns and a huge frill to protect its neck.",
        "ankylosaurus" to "Ankylosaurus wore bony armor and had a club on its tail.",
        "parasaurolophus" to "Parasaurolophus had a hollow crest that may have made trumpet-like sounds.",
        "pachycephalosaurus" to "Pachycephalosaurus had a thick dome skull used in head-butting displays.",
        "gallimimus" to "Gallimimus looked like an ostrich and could run very fast on two legs.",
        "compsognathus" to "Compsognathus was one of the smallest dinosaurs—about chicken size.",
        "stegosaurus" to "Stegosaurus had diamond-shaped plates along its back and spiked tail.",
        "brachiosaurus" to "Brachiosaurus held its neck high to reach treetops other dinos could not.",
        "pteranodon" to "Pteranodon was a flying reptile, not a dinosaur—but it shared the skies with them.",
        "dilophosaurus" to "Dilophosaurus had two thin crests on its head and lived in early Jurassic times.",
        "iguanodon" to "Iguanodon had spiky thumbs and could walk on two legs or four.",
        "carnotaurus" to "Carnotaurus had bull-like horns above its eyes and very short arms.",
        "baryonyx" to "Baryonyx had long claws and fish hooks in its jaws—it loved catching fish.",
        "plesiosaurus" to "Plesiosaurus had a long neck and paddles, swimming through ancient seas.",
        "trex" to "T. rex had one of the strongest bites of any land animal ever.",
        "spinosaurus" to "Spinosaurus is famous for the tall sail on its back and love of water.",
        "velociraptor_alpha" to "Velociraptor was feathered and about the size of a turkey.",
        "allosaurus" to "Allosaurus was a top predator of the Jurassic with sharp, curved teeth.",
        "therizinosaurus" to "Therizinosaurus had enormous claws—longer than your arm.",
        "mosasaurus" to "Mosasaurus was a giant sea reptile that ruled the oceans.",
        "diplodocus" to "Diplodocus had one of the longest tails of any dinosaur.",
        "giganotosaurus" to "Giganotosaurus rivaled T. rex in size and lived in South America.",
        "quetzalcoatlus" to "Quetzalcoatlus was as tall as a giraffe when standing on the ground.",
        "indominus_hybrid" to "This hybrid hunter blends traits of several fierce predators.",
        "ancient_spinosaurus" to "Ancient Spinosaurus legends speak of a sail that glowed at dawn.",
        "crystal_ceratosaurus" to "Crystal Ceratosaurus horns are said to shimmer like frozen starlight.",
        "volcanic_t_rex" to "Volcanic T-Rex thrived near fiery peaks where ash enriched the jungle.",
        "frost_raptor" to "Frost Raptors left claw marks in snow that never seemed to melt.",
        "shadow_triceratops" to "Shadow Triceratops herds moved quietly through misty valleys at dusk.",
        "titanosaur" to "Titanosaurs were among the largest animals to ever walk on land.",
        "cosmic_pterodactyl" to "Cosmic Pterodactyls were said to ride warm winds above the clouds.",
        "ancient_apex_rex" to "Ancient Apex Rex ruled its territory for generations.",
        "abyssal_mosasaurus" to "Abyssal Mosasaurus hunted in the deepest, darkest waters.",
    )
}
