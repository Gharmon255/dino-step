package com.gharmon255.dinostep.ui.battle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gharmon255.dinostep.battle.BattleOutcomeText
import com.gharmon255.dinostep.battle.BattlePowerCalculator
import com.gharmon255.dinostep.battle.BattleRecord
import com.gharmon255.dinostep.battle.BattleTurn
import com.gharmon255.dinostep.model.CompletedCreature
import com.gharmon255.dinostep.model.CreatureCatalog
import com.gharmon255.dinostep.model.CreatureDefinition
import com.gharmon255.dinostep.model.GrowthStage
import com.gharmon255.dinostep.ui.components.CatalogCreatureStageVisual
import com.gharmon255.dinostep.ui.theme.rarityColors

private val ArenaTop = Color(0xFF142847)
private val ArenaMid = Color(0xFF1F4738)
private val ArenaBottom = Color(0xFF0F1A24)

@Composable
fun BattleArenaBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(ArenaTop, ArenaMid, ArenaBottom),
                ),
            ),
    )
}

@Composable
fun BattleSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun BattleSignInPrompt(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "🔐", fontSize = 40.sp)
            Text(
                text = "Sign in to enter the arena",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Open Stats and sign in with Google to battle other players. Gameplay works offline without an account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun BattleStatusBanner(message: String, modifier: Modifier = Modifier) {
    val waiting = message.contains("waiting", ignoreCase = true)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = if (waiting) "⏳" else "⚡", fontSize = 18.sp)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

@Composable
fun BattleCodeBanner(code: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "SHARE THIS CODE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD54F).copy(alpha = 0.9f),
            letterSpacing = 1.2.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            code.uppercase().forEach { character ->
                Box(
                    modifier = Modifier
                        .size(width = 46.dp, height = 54.dp)
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF3373F2), Color(0xFF1F479B)),
                            ),
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = character.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                    )
                }
            }
        }
        Text(
            text = "New code each Challenge · opponent taps Accept",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BattleFighterCard(
    fighter: CompletedCreature,
    collection: List<CompletedCreature>,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val power = BattlePowerCalculator.compute(fighter, collection)
    val rarityColor = rarityColors(fighter.creature.rarity)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) Color(0xFFFFD54F) else rarityColor.border.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onSelect)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(rarityColor.container),
            contentAlignment = Alignment.Center,
        ) {
            CatalogCreatureStageVisual(
                creature = fighter.creature,
                stage = GrowthStage.ADULT,
                frameSize = 58.dp,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = fighter.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = fighter.creature.rarity.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor.accent,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(rarityColor.container)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            BattleStatBar(
                label = "CP",
                value = power.combatPower,
                maxValue = maxOf(power.combatPower, 300),
                tint = rarityColor.accent,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BattleMiniStat(label = "EX", value = power.exLevel.toString())
                BattleMiniStat(label = "Pack", value = "×${power.packCount}")
                if (power.packCount > 1) {
                    Text(
                        text = BattlePowerCalculator.packAbilityLabel(power.speciesId),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFF9800),
                    )
                }
            }
        }

        Text(
            text = if (selected) "✓" else "○",
            fontSize = 22.sp,
            color = if (selected) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
    }
}

@Composable
private fun BattleStatBar(
    label: String,
    value: Int,
    maxValue: Int,
    tint: Color,
) {
    val fraction = if (maxValue <= 0) 0f else (value.toFloat() / maxValue).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = tint,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.12f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(tint.copy(alpha = 0.85f), tint))),
            )
        }
    }
}

@Composable
private fun BattleMiniStat(label: String, value: String) {
    Text(
        text = "$label $value",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

enum class BattleActionStyle {
    Primary,
    Secondary,
    Accent,
}

@Composable
fun BattleActionButton(
    title: String,
    emoji: String,
    style: BattleActionStyle,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = when (style) {
        BattleActionStyle.Primary -> Brush.verticalGradient(
            listOf(Color(0xFF2E8B57), Color(0xFF1A6138)),
        )
        BattleActionStyle.Secondary -> Brush.verticalGradient(
            listOf(Color(0xFF47546B), Color(0xFF2E364D)),
        )
        BattleActionStyle.Accent -> Brush.verticalGradient(
            listOf(Color(0xFFD98C1F), Color(0xFFA86114)),
        )
    }
    val disabledGradient = Brush.verticalGradient(
        listOf(Color.Gray.copy(alpha = 0.35f), Color.DarkGray.copy(alpha = 0.35f)),
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) gradient else disabledGradient)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$emoji  $title",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun BattleJoinCodeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Opponent's battle code",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.75f),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
            placeholder = {
                Text(
                    text = "ABCDE",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.35f),
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.35f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedContainerColor = Color.Black.copy(alpha = 0.22f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.22f),
            ),
            shape = RoundedCornerShape(14.dp),
        )
    }
}

@Composable
fun BattleRevealCard(
    battle: BattleRecord,
    headline: String,
    currentUserId: String?,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(battle.id) {
        visible = true
    }

    val outcome = remember(headline) { BattleOutcomeStyle.fromHeadline(headline) }
    val mySide = BattleOutcomeText.sideForUser(currentUserId, battle)
    val lastTurn = battle.turnLog.lastOrNull()

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF242E47), Color(0xFF141C28)),
                    ),
                )
                .border(2.dp, outcome.borderColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(outcome.borderColor.copy(alpha = 0.35f), Color.Transparent),
                        ),
                    )
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = headline.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = outcome.textColor,
                )
                Text(
                    text = if (battle.mode == "friend") "Friend battle" else "Quick match",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.55f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                BattleFighterColumn(
                    creature = CreatureCatalog.byId(battle.playerASpeciesId),
                    speciesId = battle.playerASpeciesId,
                    power = battle.playerAPower,
                    hp = lastTurn?.aHp ?: 0,
                    isWinner = battle.winner == "a",
                    isMe = mySide == "a",
                    alignEnd = false,
                    modifier = Modifier.weight(1f),
                )
                BattleVsBadge(modifier = Modifier.padding(top = 28.dp))
                BattleFighterColumn(
                    creature = CreatureCatalog.byId(battle.playerBSpeciesId),
                    speciesId = battle.playerBSpeciesId,
                    power = battle.playerBPower,
                    hp = lastTurn?.bHp ?: 0,
                    isWinner = battle.winner == "b",
                    isMe = mySide == "b",
                    alignEnd = true,
                    modifier = Modifier.weight(1f),
                )
            }

            BattleLogPanel(
                turns = battle.turnLog,
                speciesA = battle.playerASpeciesId,
                speciesB = battle.playerBSpeciesId,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}

@Composable
private fun BattleVsBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF9800)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "VS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
    }
}

@Composable
private fun BattleFighterColumn(
    creature: CreatureDefinition?,
    speciesId: String,
    power: Int,
    hp: Int,
    isWinner: Boolean,
    isMe: Boolean,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val name = creature?.name ?: speciesId
    val rarityColor = creature?.let { rarityColors(it.rarity) }
    val maxHp = maxOf(1, (power * 1.2).toInt())
    val hpTint = if (isWinner) Color(0xFF4CAF50) else Color(0xFFE53935)

    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isMe) {
            Text(
                text = "YOU",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background((rarityColor?.container ?: Color.Gray.copy(alpha = 0.2f)))
                .border(
                    width = if (isWinner) 3.dp else 1.dp,
                    color = if (isWinner) Color(0xFFFFD54F) else (rarityColor?.border ?: Color.Gray),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (creature != null) {
                CatalogCreatureStageVisual(
                    creature = creature,
                    stage = GrowthStage.ADULT,
                    frameSize = 72.dp,
                )
            }
        }

        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        BattleHpBar(
            current = hp,
            maxHp = maxHp,
            tint = hpTint,
            alignEnd = alignEnd,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "CP $power",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = rarityColor?.accent ?: Color.White,
        )
    }
}

@Composable
private fun BattleHpBar(
    current: Int,
    maxHp: Int,
    tint: Color,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val fraction = if (maxHp <= 0) 0f else (current.toFloat() / maxHp).coerceIn(0f, 1f)
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = "HP $current/$maxHp",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.White.copy(alpha = 0.75f),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f)),
        ) {
            Box(
                modifier = Modifier
                    .align(if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart)
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(tint.copy(alpha = 0.7f), tint))),
            )
        }
    }
}

@Composable
private fun BattleLogPanel(
    turns: List<BattleTurn>,
    speciesA: String,
    speciesB: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "📖 Battle log",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            turns.forEach { turn ->
                BattleTurnRow(turn = turn, speciesA = speciesA, speciesB = speciesB)
            }
        }
    }
}

@Composable
private fun BattleTurnRow(
    turn: BattleTurn,
    speciesA: String,
    speciesB: String,
) {
    val actorName = CreatureCatalog.byId(if (turn.actor == "a") speciesA else speciesB)?.name
        ?: turn.actor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = if (turn.turn % 2 == 0) 0.06f else 0.03f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = turn.turn.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD54F),
            modifier = Modifier.width(18.dp),
        )
        Column {
            Text(
                text = turn.message.ifBlank { "$actorName used ${turn.action}!" },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f),
            )
            if (turn.damage > 0) {
                Text(
                    text = "−${turn.damage} HP",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800),
                )
            }
        }
    }
}

@Composable
fun BattleHistoryRowStyled(
    battle: BattleRecord,
    headline: String,
    modifier: Modifier = Modifier,
) {
    val outcome = BattleOutcomeStyle.fromHeadline(headline)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BattleMiniSprite(battle.playerASpeciesId)
        Text(
            text = "vs",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BattleMiniSprite(battle.playerBSpeciesId)
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = headline,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = outcome.textColor,
        )
    }
}

@Composable
private fun BattleMiniSprite(speciesId: String) {
    val creature = CreatureCatalog.byId(speciesId) ?: return
    CatalogCreatureStageVisual(
        creature = creature,
        stage = GrowthStage.ADULT,
        frameSize = 36.dp,
    )
}

private data class BattleOutcomeStyle(
    val borderColor: Color,
    val textColor: Color,
) {
    companion object {
        fun fromHeadline(headline: String): BattleOutcomeStyle {
            return when {
                headline.contains("you win", ignoreCase = true) -> BattleOutcomeStyle(
                    borderColor = Color(0xFF4CAF50),
                    textColor = Color(0xFF81C784),
                )
                headline.contains("you lose", ignoreCase = true) -> BattleOutcomeStyle(
                    borderColor = Color(0xFFE53935),
                    textColor = Color(0xFFFF8A65),
                )
                else -> BattleOutcomeStyle(
                    borderColor = Color(0xFFFFD54F),
                    textColor = Color(0xFFFFF59D),
                )
            }
        }
    }
}
