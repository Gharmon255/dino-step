package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.promo.PromoCodes

@Composable
fun PromoCodeCard(
    viewModel: GameViewModel,
    isSignedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isSignedIn) return

    var codeInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(isSignedIn) {
        viewModel.refreshEpic20PromoStatus()
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Promo code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            when {
                viewModel.hasPendingEpicRewardEgg -> {
                    Text(
                        text = "Epic egg queued! Claim your next adult to hatch it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                viewModel.epic20PromoRedeemed -> {
                    Text(
                        text = "Code EPIC20 already used on this account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> {
                    Text(
                        text = "Enter a one-time code while signed in. Your next reward egg uses the promo rarity.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.lowercase().filter { ch -> ch.isLetterOrDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Promo code") },
                        singleLine = true,
                        enabled = !viewModel.isPromoLoading,
                    )
                    Button(
                        onClick = { viewModel.redeemPromoCode(codeInput) },
                        enabled = codeInput.isNotBlank() && !viewModel.isPromoLoading,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (viewModel.isPromoLoading) "Redeeming…" else "Redeem code")
                    }
                }
            }

            viewModel.promoStatusMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
