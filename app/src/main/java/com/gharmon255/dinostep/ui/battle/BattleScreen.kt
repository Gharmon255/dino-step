package com.gharmon255.dinostep.ui.battle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.battle.BattlePowerCalculator
import com.gharmon255.dinostep.battle.BattleRecord
import com.gharmon255.dinostep.battle.BattleTurn
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog

@Composable
fun BattleScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var inviteCodeInput by rememberSaveable { mutableStateOf("") }
    val cloudState by viewModel.cloudAccountUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resumeBattlePollingIfNeeded()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!cloudState.isConfigured) {
            Text("Cloud backup is not configured on this build.")
            return@Column
        }

        if (cloudState.syncStatus.name == "SignedOut") {
            Text("Sign in from Stats to battle other players.")
            return@Column
        }

        viewModel.battleInviteCode?.let { code ->
            Text(
                text = "Battle code: $code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Share this 5-letter code with your opponent (new code each Challenge).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (viewModel.isBattleLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        viewModel.battleStatusMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = "Choose your fighter",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (viewModel.collection.isEmpty()) {
            Text("Claim an adult dinosaur to unlock battles.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(viewModel.collection, key = { it.id }) { fighter ->
                    FighterPickCard(
                        fighter = fighter,
                        collection = viewModel.collection,
                        selected = viewModel.selectedBattleFighter?.id == fighter.id,
                        onSelect = { viewModel.selectBattleFighter(fighter) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { viewModel.findQuickMatch() },
                enabled = viewModel.selectedBattleFighter != null && !viewModel.isBattleLoading,
                modifier = Modifier.weight(1f),
            ) {
                Text("Quick match")
            }
            Button(
                onClick = { viewModel.createFriendChallenge() },
                enabled = !viewModel.isBattleLoading,
                modifier = Modifier.weight(1f),
            ) {
                Text("Challenge")
            }
        }

        OutlinedTextField(
            value = inviteCodeInput,
            onValueChange = { inviteCodeInput = it.uppercase().filter { ch -> ch.isLetterOrDigit() }.take(5) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Opponent's 5-letter battle code") },
            singleLine = true,
        )
        Button(
            onClick = { viewModel.acceptFriendChallenge(inviteCodeInput) },
            enabled = inviteCodeInput.length == 5 && viewModel.selectedBattleFighter != null && !viewModel.isBattleLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Accept & blind pick")
        }

        viewModel.activeBattleChallengeId?.let { challengeId ->
            Button(
                onClick = { viewModel.submitBattlePick(challengeId) },
                enabled = viewModel.selectedBattleFighter != null && !viewModel.isBattleLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lock in fighter (hidden until reveal)")
            }
        }

        viewModel.latestBattle?.let { battle ->
            Spacer(modifier = Modifier.height(8.dp))
            BattleResultCard(
                battle = battle,
                headline = viewModel.battleOutcomeHeadline(battle),
            )
        }

        if (viewModel.battleHistory.isNotEmpty()) {
            Text(
                text = "Recent battles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            viewModel.battleHistory.take(5).forEach { battle ->
                BattleHistoryRow(
                    battle = battle,
                    headline = viewModel.battleOutcomeHeadline(battle),
                )
            }
        }
    }
}

@Composable
private fun FighterPickCard(
    fighter: CompletedCreature,
    collection: List<CompletedCreature>,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val power = BattlePowerCalculator.compute(fighter, collection)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = fighter.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Power ${power.combatPower} · EX ${power.exLevel} · Pack ×${power.packCount}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BattleResultCard(
    battle: BattleRecord,
    headline: String,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${displayName(battle.playerASpeciesId)} (${battle.playerAPower}) vs ${displayName(battle.playerBSpeciesId)} (${battle.playerBPower})",
                style = MaterialTheme.typography.bodyMedium,
            )
            battle.turnLog.forEach { turn ->
                BattleTurnLine(turn = turn)
            }
        }
    }
}

@Composable
private fun BattleHistoryRow(
    battle: BattleRecord,
    headline: String,
) {
    Text(
        text = "${displayName(battle.playerASpeciesId)} vs ${displayName(battle.playerBSpeciesId)} — $headline",
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun BattleTurnLine(turn: BattleTurn) {
    Text(
        text = turn.message.ifBlank { "Turn ${turn.turn}: ${turn.action} -${turn.damage}" },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun displayName(speciesId: String): String {
    return CreatureCatalog.byId(speciesId)?.name ?: speciesId
}
