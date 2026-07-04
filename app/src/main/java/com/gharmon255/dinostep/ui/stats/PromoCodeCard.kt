package com.gharmon255.dinostep.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.gharmon255.dinostep.game.GameViewModel
import com.gharmon255.dinostep.promo.PromoCatalog

@Composable
fun PromoCodeCard(
    viewModel: GameViewModel,
    isSignedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    var codeInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            viewModel.refreshPromoRedemptionStatus()
        }
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

            viewModel.pendingPromoRewardRarity?.let { pending ->
                Text(
                    text = "${pending.name.lowercase().replaceFirstChar { it.uppercase() }} egg queued! " +
                        "Claim your next adult to hatch it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Text(
                text = if (isSignedIn) {
                    "Enter a one-time code. Redemption is saved on this device and on your account when signed in."
                } else {
                    "Enter a one-time code. Each code works once on this device — no sign-in required."
                },
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

            val normalizedInput = PromoCatalog.normalize(codeInput)
            if (normalizedInput.isNotBlank() && viewModel.isPromoCodeRedeemed(normalizedInput)) {
                Text(
                    text = "Code ${normalizedInput.uppercase()} was already used on this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = { viewModel.redeemPromoCode(codeInput) },
                enabled = codeInput.isNotBlank() && !viewModel.isPromoLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (viewModel.isPromoLoading) "Redeeming…" else "Redeem code")
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
