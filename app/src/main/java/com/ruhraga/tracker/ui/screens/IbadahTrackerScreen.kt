package com.ruhraga.tracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhraga.tracker.ui.components.CircularProgressRing
import com.ruhraga.tracker.ui.components.QiblaCompassSheet
import com.ruhraga.tracker.ui.state.IbadahTab
import com.ruhraga.tracker.ui.state.PrayerItem
import com.ruhraga.tracker.ui.state.RuhRagaUiState
import com.ruhraga.tracker.ui.state.SunnahItem
import com.ruhraga.tracker.ui.theme.RuhRagaTextStyles

@Composable
fun IbadahTrackerScreen(
        state: RuhRagaUiState,
        onSelectTab: (IbadahTab) -> Unit,
        onTogglePrayerCompleted: (String) -> Unit,
        onTogglePrayerJamaah: (String) -> Unit,
        onToggleSunnah: (String) -> Unit,
        onToggleTilawahComplete: () -> Unit,
        onTapTasbih: () -> Unit,
        modifier: Modifier = Modifier
) {
    var showQiblaSheet by remember { mutableStateOf(false) }

    if (showQiblaSheet) {
        QiblaCompassSheet(onDismiss = { showQiblaSheet = false })
    }

    LazyColumn(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { IbadahHeroCard(state = state, onOpenQibla = { showQiblaSheet = true }) }
        item {
            IbadahSegmentedControl(selectedTab = state.selectedIbadahTab, onSelectTab = onSelectTab)
        }

        if (state.selectedIbadahTab == IbadahTab.FARDHU) {
            item {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "FIVE PILLARS OF TIME",
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                    ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                    Modifier.clip(CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.14f
                                                    )
                                            )
                                            .clickable { showQiblaSheet = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                                text = "Kompas Kiblat • 295° NW",
                                style =
                                        MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            items(state.prayers.size) { index ->
                val prayer = state.prayers[index]
                PrayerTrackerCard(
                        prayer = prayer,
                        countdownLabel = state.nextPrayerCountdown,
                        onToggleComplete = { onTogglePrayerCompleted(prayer.id) },
                        onToggleJamaah = { onTogglePrayerJamaah(prayer.id) }
                )
            }
        }

        if (state.selectedIbadahTab == IbadahTab.FARDHU ||
                        state.selectedIbadahTab == IbadahTab.SUNNAH
        ) {
            item {
                SunnahRoutineSection(items = state.sunnahItems, onToggleSunnah = onToggleSunnah)
            }
        }

        if (state.selectedIbadahTab == IbadahTab.FARDHU ||
                        state.selectedIbadahTab == IbadahTab.TILAWAH_DZIKIR
        ) {
            item { DailyTilawahCard(state = state, onToggleComplete = onToggleTilawahComplete) }
            item { EveningAdhkarCard(state = state, onTapTasbih = onTapTasbih) }
        }
    }
}

@Composable
private fun IbadahHeroCard(state: RuhRagaUiState, onOpenQibla: () -> Unit) {
    val completedCount = state.completedPrayersCount
    val percent = ((completedCount.toFloat() / state.prayers.size.coerceAtLeast(1)) * 100).toInt()

    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                    Modifier.clip(CircleShape)
                                            .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.14f
                                                    )
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = "RUH ROUTINE",
                                style =
                                        MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                                color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                            text = "Jum'at Mubarak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                        text = "Ibadah & Spiritual Duty",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = "Discipline of the heart before exertion of the body",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            CircularProgressRing(
                    progress = percent / 100f,
                    ringColor = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    size = 68.dp,
                    strokeWidth = 6.dp
            ) {
                Text(
                        text = "$percent%",
                        style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                        text = "$completedCount of ${state.prayers.size} Obligatory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                            Modifier.clip(CircleShape)
                                    .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                    )
                                    .clickable(onClick = onOpenQibla)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                        text = "Arah Kiblat",
                        style =
                                MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                ),
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun IbadahSegmentedControl(selectedTab: IbadahTab, onSelectTab: (IbadahTab) -> Unit) {
    Row(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IbadahTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            Box(
                    modifier =
                            Modifier.weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                            if (selected)
                                                    MaterialTheme.colorScheme
                                                            .surfaceContainerHighest
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                    .clickable { onSelectTab(tab) }
                                    .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = tab.label,
                        style =
                                MaterialTheme.typography.labelSmall.copy(
                                        fontWeight =
                                                if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                        color =
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PrayerTrackerCard(
        prayer: PrayerItem,
        countdownLabel: String,
        onToggleComplete: () -> Unit,
        onToggleJamaah: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val containerColor by
            animateColorAsState(
                    targetValue =
                            when {
                                prayer.isCompleted ->
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                prayer.isActive -> MaterialTheme.colorScheme.surfaceContainerHigh
                                else -> MaterialTheme.colorScheme.surfaceContainer
                            },
                    label = "prayerCardBg"
            )

    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(containerColor)
                            .then(
                                    when {
                                        prayer.isCompleted ->
                                                Modifier.border(
                                                        width = 1.dp,
                                                        color =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.35f),
                                                        shape = RoundedCornerShape(16.dp)
                                                )
                                        prayer.isActive ->
                                                Modifier.border(
                                                        width = 1.5.dp,
                                                        color =
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.55f),
                                                        shape = RoundedCornerShape(16.dp)
                                                )
                                        else -> Modifier
                                    }
                            )
                            .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
                modifier =
                        Modifier.fillMaxWidth().clickable(
                            enabled = prayer.isAvailable && !prayer.isCompleted
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleComplete()
                        },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                        modifier =
                                Modifier.size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                                when {
                                                    prayer.isCompleted -> MaterialTheme.colorScheme.primary
                                                    !prayer.isAvailable -> MaterialTheme.colorScheme.surfaceContainerLow
                                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                                }
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector =
                                    when {
                                        prayer.isCompleted -> Icons.Rounded.Check
                                        !prayer.isAvailable -> Icons.Rounded.Schedule
                                        else -> Icons.Rounded.RadioButtonUnchecked
                                    },
                            contentDescription = null,
                            tint =
                                    when {
                                        prayer.isCompleted -> MaterialTheme.colorScheme.onPrimary
                                        !prayer.isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                                text = prayer.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (prayer.isAvailable) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text = prayer.arabicName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (prayer.isAvailable && prayer.isActive && !prayer.isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "SAAT INI",
                                    style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                            ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier =
                                            Modifier.clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                            MaterialTheme.colorScheme.primary.copy(
                                                                    alpha = 0.16f
                                                            )
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                            text =
                                    when {
                                        !prayer.isAvailable -> "${prayer.time} • Belum masuk waktu"
                                        prayer.isActive && !prayer.isCompleted ->
                                            "${prayer.time} • ${prayer.rakaat} Rakaat"
                                        else -> "${prayer.time} • ${prayer.rakaat} Rakaat"
                                    },
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                    if (prayer.isAvailable && prayer.isActive && !prayer.isCompleted)
                                            MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Friendly explicit action pill or countdown lock pill
            if (!prayer.isAvailable) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                                Modifier.clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = prayer.countdownUntilStart ?: "Belum waktunya",
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                    ),
                            color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                Box(
                        modifier =
                                Modifier.clip(CircleShape)
                                        .background(
                                                if (prayer.isCompleted)
                                                        MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.18f
                                                        )
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                            text = if (prayer.isCompleted) "Selesai ✓" else "Tandai",
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color =
                                    if (prayer.isCompleted) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Smooth inline Jama'ah / Munfarid selector once prayer is marked complete
        AnimatedVisibility(visible = prayer.isCompleted) {
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                        modifier =
                                Modifier.weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                                if (prayer.isJamaah)
                                                        MaterialTheme.colorScheme.primary.copy(
                                                                alpha = 0.18f
                                                        )
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .clickable {
                                            if (!prayer.isJamaah) {
                                                haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                )
                                                onToggleJamaah()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Groups,
                            contentDescription = null,
                            tint =
                                    if (prayer.isJamaah) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                            text = "Jama'ah (+27x)",
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight =
                                                    if (prayer.isJamaah) FontWeight.Bold
                                                    else FontWeight.Medium
                                    ),
                            color =
                                    if (prayer.isJamaah) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                        modifier =
                                Modifier.weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                                if (!prayer.isJamaah)
                                                        MaterialTheme.colorScheme
                                                                .surfaceContainerHigh
                                                else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .clickable {
                                            if (prayer.isJamaah) {
                                                haptic.performHapticFeedback(
                                                        HapticFeedbackType.TextHandleMove
                                                )
                                                onToggleJamaah()
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint =
                                    if (!prayer.isJamaah) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                            text = "Sendiri (Munfarid)",
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight =
                                                    if (!prayer.isJamaah) FontWeight.Bold
                                                    else FontWeight.Medium
                                    ),
                            color =
                                    if (!prayer.isJamaah) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (prayer.isAvailable && prayer.isActive && !prayer.isCompleted) {
            Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleComplete()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                            )
            ) {
                Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = "Tandai ${prayer.name} Sudah Shalat",
                        style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SunnahRoutineSection(items: List<SunnahItem>, onToggleSunnah: (String) -> Unit) {
    val completed = items.count { it.isCompleted }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        text = "Sunnah Routine",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = "$completed of ${items.size} Complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier =
                                Modifier.clip(CircleShape)
                                        .background(
                                                MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.14f
                                                )
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                    text = "Log Extra",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        SunnahGridCard(
                                item = item,
                                onClick = { onToggleSunnah(item.id) },
                                modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SunnahGridCard(item: SunnahItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable(onClick = onClick)
                            .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = item.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
            when {
                item.progressText != null -> {
                    Text(
                            text = item.progressText,
                            style =
                                    MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = MaterialTheme.colorScheme.secondary
                    )
                }
                item.isCompleted -> {
                    Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                    )
                }
                else -> {
                    Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
        )
        Text(
                text = item.detail,
                style = MaterialTheme.typography.bodySmall,
                color =
                        if (item.isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DailyTilawahCard(state: RuhRagaUiState, onToggleComplete: () -> Unit) {
    val t = state.tilawah
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier =
                                Modifier.size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                                MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.14f
                                                )
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                            text = "DAILY TILAWAH",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = t.surahTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                    text = t.juzLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                            Modifier.clip(CircleShape)
                                    .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Column(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                    text = t.arabicVerse,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                    text = t.ayatRange,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                    text = "Progress: ${t.progressPercent}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = "Page ${t.currentPage} / ${t.totalPages}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth((t.progressPercent / 100f).coerceIn(0f, 1f))
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Button(
                onClick = onToggleComplete,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                containerColor =
                                        if (t.isCompletedToday)
                                                MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                        )
        ) {
            Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text =
                            if (t.isCompletedToday) "Today's Tilawah Completed ✓"
                            else "Mark Today's Tilawah Complete",
                    style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun EveningAdhkarCard(state: RuhRagaUiState, onTapTasbih: () -> Unit) {
    val tasbih = state.tasbih
    Column(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier =
                                Modifier.size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                        alpha = 0.2f
                                                )
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                            text = "EVENING ADHKAR",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = "Tasbih Fatimah",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                    text = "Step ${tasbih.activePhaseIndex + 1} of 3",
                    style =
                            MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                            ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier =
                            Modifier.clip(CircleShape)
                                    .background(
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TasbihPhasePill(
                    label = "Subhanallah",
                    count = tasbih.subhanallah,
                    target = tasbih.targetPerPhase,
                    isActive = tasbih.activePhaseIndex == 0,
                    modifier = Modifier.weight(1f)
            )
            TasbihPhasePill(
                    label = "Alhamdulillah",
                    count = tasbih.alhamdulillah,
                    target = tasbih.targetPerPhase,
                    isActive = tasbih.activePhaseIndex == 1,
                    modifier = Modifier.weight(1f)
            )
            TasbihPhasePill(
                    label = "Allahu Akbar",
                    count = tasbih.allahuAkbar,
                    target = tasbih.targetPerPhase,
                    isActive = tasbih.activePhaseIndex == 2,
                    modifier = Modifier.weight(1f)
            )
        }

        Row(
                modifier =
                        Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .clickable(onClick = onTapTasbih)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                                MaterialTheme.colorScheme.secondaryContainer.copy(
                                                        alpha = 0.25f
                                                )
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                            text = "Tap to Count",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                            text = "Haptic pulse feedback enabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                    text = "${tasbih.activeCount}",
                    style = RuhRagaTextStyles.MetricNumLg,
                    color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun TasbihPhasePill(
        label: String,
        count: Int,
        target: Int,
        isActive: Boolean,
        modifier: Modifier = Modifier
) {
    val isDone = count >= target
    val accent =
            if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Column(
            modifier =
                    modifier.clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .then(
                                    if (isActive)
                                            Modifier.border(
                                                    1.dp,
                                                    accent.copy(alpha = 0.5f),
                                                    RoundedCornerShape(12.dp)
                                            )
                                    else Modifier
                            )
                            .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
                text = "$count/$target",
                style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                color = accent
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth(
                                            (count.toFloat() / target.coerceAtLeast(1)).coerceIn(
                                                    0f,
                                                    1f
                                            )
                                    )
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(accent)
            )
        }
    }
}
