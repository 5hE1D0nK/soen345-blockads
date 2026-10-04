package app.pwhs.blockads.ui.statistics.destinations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pwhs.blockads.R
import app.pwhs.blockads.data.entities.CountryStat
import app.pwhs.blockads.ui.statistics.DestinationTimeRange

/**
 * Traffic Destinations analytics section featuring an interactive World Map and top countries.
 */
@Composable
fun TrafficDestinationsSection(
    countryStats: List<CountryStat>,
    selectedRange: DestinationTimeRange,
    onRangeSelected: (DestinationTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCountryIso by remember { mutableStateOf<String?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    val totalQueries = remember(countryStats) { countryStats.sumOf { it.count } }
    val displayList = remember(countryStats, isExpanded) {
        if (isExpanded) countryStats else countryStats.take(5)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.stats_traffic_destinations_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = stringResource(R.string.stats_traffic_destinations_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Range Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DestinationTimeRange.entries.forEach { range ->
                    val isSelected = range == selectedRange
                    val labelRes = when (range) {
                        DestinationTimeRange.HOURS_24 -> R.string.stats_destinations_24h
                        DestinationTimeRange.DAYS_7 -> R.string.stats_destinations_7d
                        DestinationTimeRange.DAYS_30 -> R.string.stats_destinations_30d
                        DestinationTimeRange.ALL -> R.string.stats_destinations_all
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onRangeSelected(range) },
                        label = { Text(text = stringResource(labelRes)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive World Map
            WorldMapCanvas(
                countryStats = countryStats,
                selectedCountryIso = selectedCountryIso,
                onCountrySelected = { selectedCountryIso = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Country Breakdown List
            if (countryStats.isEmpty()) {
                Text(
                    text = stringResource(R.string.stats_destinations_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    displayList.forEach { stat ->
                        DestinationCountryItem(
                            stat = stat,
                            totalCount = totalQueries,
                            isSelected = stat.countryCode.equals(selectedCountryIso, ignoreCase = true),
                            onClick = {
                                selectedCountryIso = if (selectedCountryIso == stat.countryCode) null else stat.countryCode
                            }
                        )
                    }

                    if (countryStats.size > 5) {
                        TextButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = stringResource(
                                    if (isExpanded) R.string.stats_show_less else R.string.stats_show_more
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
