package com.gharmon255.dinostep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gharmon255.dinostep.ui.common.PrivacyPolicyLink
import com.gharmon255.dinostep.ui.theme.DinoStepTheme

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DinoStepTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Health Connect permission",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = "Stepasaurus reads your step count from Health Connect to hatch and grow dinosaurs. " +
                            "Tap Sync again on Home anytime, or let the app sync about once per hour in the background. " +
                            "We do not read your location or track you for advertising.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    PrivacyPolicyLink()
                }
            }
        }
    }
}
