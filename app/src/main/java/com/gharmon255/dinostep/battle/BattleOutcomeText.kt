package com.gharmon255.dinostep.battle

object BattleOutcomeText {
    fun headline(battle: BattleRecord, currentUserId: String?): String {
        val winner = battle.winner.lowercase()
        if (winner == "draw") {
            return "Draw!"
        }
        val mySide = when (currentUserId) {
            battle.playerAUserId -> "a"
            battle.playerBUserId -> "b"
            else -> return fallbackWinnerLabel(winner)
        }
        return if (mySide == winner) "You win!" else "You lose!"
    }

    private fun fallbackWinnerLabel(winner: String): String = when (winner) {
        "a" -> "Challenger wins"
        "b" -> "Opponent wins"
        else -> "Draw!"
    }

    fun sideForUser(userId: String?, battle: BattleRecord): String? {
        if (userId == null) return null
        return when (userId) {
            battle.playerAUserId -> "a"
            battle.playerBUserId -> "b"
            else -> null
        }
    }
}
