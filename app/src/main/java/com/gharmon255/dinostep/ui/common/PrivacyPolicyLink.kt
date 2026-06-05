package com.gharmon255.dinostep.ui.common

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.gharmon255.dinostep.R

@Composable
fun PrivacyPolicyLink(
    modifier: Modifier = Modifier,
    label: String = "Privacy Policy",
) {
    val context = LocalContext.current
    val url = stringResource(R.string.privacy_policy_url)

    TextButton(
        onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        },
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
