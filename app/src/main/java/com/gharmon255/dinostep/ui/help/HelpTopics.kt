package com.gharmon255.dinostep.ui.help

data class HelpSection(
    val title: String,
    val body: String,
)

object HelpTopics {
    fun sections(includeEggsTab: Boolean): List<HelpSection> = buildList {
        add(
            HelpSection(
                title = "Getting started",
                body = "Walk every day to earn steps. Tap Sync again on Home to pull steps from " +
                    "Health Connect, or let the app sync about once per hour in the background. " +
                    "Steps hatch your egg and grow your active dinosaur through baby, juvenile, and adult stages.",
            ),
        )
        add(
            HelpSection(
                title = "Home",
                body = "Your active egg or dinosaur lives here. Watch cracks appear on the egg as you " +
                    "walk. After hatching, see growth stages, nicknames, and your Dino Dex progress. " +
                    "Duplicate adults can be traded for a new egg of the same rarity.",
            ),
        )
        if (includeEggsTab) {
            add(
                HelpSection(
                    title = "Eggs",
                    body = "See which species can hatch from your current egg rarity and how many steps " +
                        "each milestone needs. Rarer eggs take more walking but can become stronger dinos.",
                ),
            )
        }
        add(
            HelpSection(
                title = "Collection",
                body = "Every adult you claim is saved here. Tap a species to see stats, EX level, and " +
                    "pack size. Owning duplicates of the same species makes that fighter stronger in battles.",
            ),
        )
        add(
            HelpSection(
                title = "Battle",
                body = "Sign in from Stats to battle (optional). Pick an adult fighter, then either " +
                    "Quick match for an instant fight or Challenge a friend:\n\n" +
                    "• Host taps Challenge and shares the 5-letter code\n" +
                    "• Friend enters the code and taps Accept & blind pick\n" +
                    "• Both lock in a fighter — picks stay hidden until the reveal\n\n" +
                    "Friend battles need two different accounts (different emails).",
            ),
        )
        add(
            HelpSection(
                title = "Stats & backup",
                body = "View today's steps, lifetime steps, and dex progress. Optionally sign in to " +
                    "back up your save to the cloud or export a local copy. Grant health permissions " +
                    "here if sync is blocked. Questions or lost saves? Email stepasaurushelp@gmail.com.",
            ),
        )
        add(
            HelpSection(
                title = "Daily step goal",
                body = "Walk at least 5,000 steps on a day or your active dinosaur resets to a fresh egg " +
                    "with 500 steps already applied. Keep moving to protect your progress!",
            ),
        )
    }
}

object BattleIntroContent {
    const val TITLE = "How battles work"

    const val BODY = "Battle with a friend using blind picks — neither player sees the other's fighter " +
        "until the fight ends.\n\n" +
        "1. Sign in from Stats (both players).\n" +
        "2. Host taps Challenge and shares the 5-letter code.\n" +
        "3. Friend enters the code and taps Accept & blind pick.\n" +
        "4. Both lock in an adult from their collection.\n" +
        "5. Stronger fighter wins — rarity, EX, and pack bonuses all matter.\n\n" +
        "Use two different accounts on two phones (different emails)."
}
