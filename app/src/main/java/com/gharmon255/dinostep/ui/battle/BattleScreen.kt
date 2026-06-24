package com.gharmon255.dinostep.ui.battle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.battle.BattleFeatures
import com.gharmon255.dinostep.battle.BattlePowerCalculator
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.model.CompletedCreature

@Composable
fun BattleScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var inviteCodeInput by rememberSaveable { mutableStateOf("") }
    val cloudState by viewModel.cloudAccountUiState.collectAsState()

    LaunchedEffect(Unit) {
        if (BattleFeatures.enabled) {
            viewModel.resumeBattlePollingIfNeeded()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BattleArenaBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (!BattleFeatures.enabled) {
                BattleComingSoonBanner()
                FighterSection(viewModel = viewModel, battlesEnabled = false)
            } else when {
                !cloudState.isConfigured -> BattleSignInPrompt()
                cloudState.syncStatus.name == "SignedOut" -> BattleSignInPrompt()
                else -> {
                    SignedInBattleContent(
                        viewModel = viewModel,
                        cloudState = cloudState,
                        inviteCodeInput = inviteCodeInput,
                        onInviteCodeChange = { inviteCodeInput = it },
                    )
                }
            }
        }
    }
}

@Composable
private fun SignedInBattleContent(
    viewModel: GameViewModel,
    cloudState: com.gharmon255.dinostep.cloud.CloudAccountUiState,
    inviteCodeInput: String,
    onInviteCodeChange: (String) -> Unit,
) {
    viewModel.latestBattle?.let { battle ->
        BattleRevealCard(
            battle = battle,
            headline = viewModel.battleOutcomeHeadline(battle),
            currentUserId = cloudState.signedInUserId,
        )
    }

    if (viewModel.isBattleLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    viewModel.battleStatusMessage?.let { message ->
        BattleStatusBanner(message = message)
    }

    viewModel.battleInviteCode?.let { code ->
        BattleCodeBanner(code = code)
    }

    ActionSection(viewModel = viewModel)
    JoinSection(
        inviteCodeInput = inviteCodeInput,
        onInviteCodeChange = onInviteCodeChange,
        viewModel = viewModel,
    )

    viewModel.activeBattleChallengeId?.let { challengeId ->
        BattleActionButton(
            title = "Lock in fighter",
            emoji = "🛡️",
            style = BattleActionStyle.Accent,
            enabled = viewModel.selectedBattleFighter != null && !viewModel.isBattleLoading,
            onClick = { viewModel.submitBattlePick(challengeId) },
        )
    }

    FighterSection(viewModel = viewModel)

    if (viewModel.battleHistory.isNotEmpty()) {
        HistorySection(viewModel = viewModel)
    }
}

@Composable
private fun FighterSection(
    viewModel: GameViewModel,
    battlesEnabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BattleSectionHeader(
            title = "Choose your champion",
            subtitle = if (battlesEnabled) {
                "Adults only · picks stay hidden in friend battles"
            } else {
                "Preview your roster · PvP battles coming soon on Android"
            },
        )

        if (viewModel.collection.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(text = "🐢", style = MaterialTheme.typography.displaySmall)
                    Text(
                        text = "Hatch and claim an adult dinosaur to unlock battles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            sortedFighters(viewModel).forEach { fighter ->
                BattleFighterCard(
                    fighter = fighter,
                    collection = viewModel.collection,
                    selected = battlesEnabled && viewModel.selectedBattleFighter?.id == fighter.id,
                    selectable = battlesEnabled,
                    onSelect = { viewModel.selectBattleFighter(fighter) },
                )
            }
        }
    }
}

@Composable
private fun ActionSection(viewModel: GameViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BattleSectionHeader(title = "Battle modes")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BattleActionButton(
                title = "Quick match",
                emoji = "⚡",
                style = BattleActionStyle.Primary,
                enabled = viewModel.selectedBattleFighter != null && !viewModel.isBattleLoading,
                onClick = { viewModel.findQuickMatch() },
                modifier = Modifier.weight(1f),
            )
            BattleActionButton(
                title = "Challenge",
                emoji = "🏁",
                style = BattleActionStyle.Secondary,
                enabled = !viewModel.isBattleLoading,
                onClick = { viewModel.createFriendChallenge() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun JoinSection(
    inviteCodeInput: String,
    onInviteCodeChange: (String) -> Unit,
    viewModel: GameViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BattleSectionHeader(
            title = "Join a friend",
            subtitle = "Enter the host's 5-letter code",
        )
        BattleJoinCodeField(
            value = inviteCodeInput,
            onValueChange = { raw ->
                onInviteCodeChange(raw.uppercase().filter { it.isLetterOrDigit() }.take(5))
            },
        )
        BattleActionButton(
            title = "Accept & blind pick",
            emoji = "🙈",
            style = BattleActionStyle.Accent,
            enabled = inviteCodeInput.length == 5 &&
                viewModel.selectedBattleFighter != null &&
                !viewModel.isBattleLoading,
            onClick = { viewModel.acceptFriendChallenge(inviteCodeInput) },
        )
    }
}

@Composable
private fun HistorySection(viewModel: GameViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BattleSectionHeader(title = "Recent battles")
        viewModel.battleHistory.take(5).forEach { battle ->
            BattleHistoryRowStyled(
                battle = battle,
                headline = viewModel.battleOutcomeHeadline(battle),
            )
        }
    }
}

private fun sortedFighters(viewModel: GameViewModel): List<CompletedCreature> {
    return viewModel.collection.sortedByDescending { fighter ->
        BattlePowerCalculator.compute(fighter, viewModel.collection).combatPower
    }
}
