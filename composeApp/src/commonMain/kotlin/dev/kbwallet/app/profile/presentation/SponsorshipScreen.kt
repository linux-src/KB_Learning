package dev.kbwallet.app.profile.presentation

import dev.kbwallet.app.theme.KBTheme
import androidx.compose.material3.MaterialTheme
import dev.kbwallet.app.theme.component.BackHeader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.kbwallet.app.core.i18n.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorshipScreen(
    onBack: () -> Unit
) {
    val strings = appStrings()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackHeader(title = strings.sponsorshipTitle, onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = KBTheme.colors.lossRed,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )
            
            Text(
                text = strings.sponsorshipDesc,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            SponsorOption(
                title = strings.sponsorshipCoffee,
                subtitle = strings.sponsorshipCoffeeSub,
                price = "$3",
                onClick = { /* TODO: Implement billing/link */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            SponsorOption(
                title = strings.sponsorshipBeer,
                subtitle = strings.sponsorshipBeerSub,
                price = "$5",
                onClick = { /* TODO: Implement billing/link */ }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = strings.sponsorshipFooter,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SponsorOption(
    title: String,
    subtitle: String,
    price: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = price,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
