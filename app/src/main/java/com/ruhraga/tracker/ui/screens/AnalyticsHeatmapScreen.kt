package com.ruhraga.tracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhraga.tracker.ui.state.AnalyticsPeriod
import com.ruhraga.tracker.ui.state.DailyActivityResume
import com.ruhraga.tracker.ui.state.DayMatrixStatus
import com.ruhraga.tracker.ui.state.MatrixDayCell
import com.ruhraga.tracker.ui.state.MilestoneItem
import com.ruhraga.tracker.ui.state.RuhRagaUiState
import com.ruhraga.tracker.ui.theme.RuhRagaTextStyles
import com.ruhraga.tracker.ui.theme.RuhRagaTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AnalyticsHeatmapScreen(
    state: RuhRagaUiState,
    onSelectPeriod: (AnalyticsPeriod) -> Unit,
    onSelectMatrixDay: (Int) -> Unit,
    onNavigateMonth: (Long) -> Unit = {},
    onLongClickDay: (Int) -> Unit = {},
    onDismissResume: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    state.selectedDayResume?.let { resume ->
        DayActivityResumeSheet(
            resume = resume,
            onDismiss = onDismissResume
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnalyticsHeader(monthLabel = state.monthlyMonthLabel)
        }
        item {
            PeriodSegmentedControl(
                selectedPeriod = state.selectedPeriod,
                onSelectPeriod = onSelectPeriod
            )
        }
        item {
            TotalConsistencyCard(state = state)
        }
        item {
            VolumeMetricsRow(state = state)
        }
        item {
            QuranTilawahSummaryCard(state = state)
        }
        item {
            DualDisciplineMatrixCard(
                monthLabel = state.monthlyMonthLabel,
                displayedMonth = state.displayedYearMonth,
                days = state.displayedMatrixDays,
                selectedDay = state.selectedMatrixDay,
                onSelectDay = onSelectMatrixDay,
                onLongClickDay = onLongClickDay,
                onNavigateMonth = onNavigateMonth
            )
        }
        item {
            WeeklyDualBalanceCard(state = state)
        }
        item {
            DisciplineMilestonesSection(milestones = state.milestones)
        }
        item {
            ShareMonthlySummaryCard()
        }
    }
}

@Composable
private fun AnalyticsHeader(monthLabel: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Progress &\nConsistency",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dihitung langsung dari aktivitas yang kamu catat",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun PeriodSegmentedControl(
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AnalyticsPeriod.entries.forEach { period ->
            val selected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHighest
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    .clickable { onSelectPeriod(period) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TotalConsistencyCard(state: RuhRagaUiState) {
    val consistency = when (state.selectedPeriod) {
        AnalyticsPeriod.THIS_WEEK -> state.weeklySuccessPercent
        AnalyticsPeriod.THIS_MONTH, AnalyticsPeriod.ALL_LOG -> state.totalConsistencyPercent
    }
    val ruhPct = when (state.selectedPeriod) {
        AnalyticsPeriod.THIS_WEEK -> state.ruhCompletionPercent
        AnalyticsPeriod.THIS_MONTH, AnalyticsPeriod.ALL_LOG -> state.avgRuhPercent
    }
    val ragaPct = when (state.selectedPeriod) {
        AnalyticsPeriod.THIS_WEEK -> state.ragaCompletionPercent
        AnalyticsPeriod.THIS_MONTH, AnalyticsPeriod.ALL_LOG -> state.avgRagaPercent
    }

    val statusText = when {
        consistency >= 80 -> "Optimal Harmony"
        consistency >= 40 -> "Building Momentum"
        consistency > 0 -> "Started Today"
        else -> "Belum Ada Data"
    }

    val totalSum = (ruhPct + ragaPct).coerceAtLeast(1)
    val ruhWeight = (ruhPct.toFloat() / totalSum) * (consistency / 100f)
    val ragaWeight = (ragaPct.toFloat() / totalSum) * (consistency / 100f)
    val emptyWeight = (1f - ruhWeight - ragaWeight).coerceAtLeast(0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
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
            Text(
                text = "TOTAL CONSISTENCY",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${state.activeLoggedDaysCount} hari aktif",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$consistency%",
                style = RuhRagaTextStyles.DisplayHero,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (ruhWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(ruhWeight)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (ragaWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(ragaWeight)
                        .height(8.dp)
                        .background(RuhRagaTheme.extendedColors.ragaVibrantOrange)
                )
            }
            if (emptyWeight > 0f) {
                Spacer(modifier = Modifier.weight(emptyWeight))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ruh: $ruhPct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(RuhRagaTheme.extendedColors.ragaVibrantOrange)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Raga: $ragaPct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VolumeMetricsRow(state: RuhRagaUiState) {
    val shalatPct = ((state.completedPrayersCount.toFloat() / state.prayers.size.coerceAtLeast(1)) * 100).toInt()
    val repsTarget = state.selectedExercise.targetReps

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mosque,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Shalat Tercatat",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$shalatPct%",
                style = RuhRagaTextStyles.MetricNumMd,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${state.completedPrayersCount} / ${state.prayers.size} Fardh",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Total Reps",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${state.totalSessionReps}",
                style = RuhRagaTextStyles.MetricNumMd,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Target: $repsTarget reps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuranTilawahSummaryCard(state: RuhRagaUiState) {
    val t = state.tilawah
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "QURAN TILAWAH",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (t.isCompletedToday) "1 Sesi Selesai" else "Belum Dicatat",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${t.progressPercent}% Hari Ini",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = t.surahTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DualDisciplineMatrixCard(
    monthLabel: String,
    displayedMonth: YearMonth,
    days: List<MatrixDayCell>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onLongClickDay: (Int) -> Unit,
    onNavigateMonth: (Long) -> Unit
) {
    val today = LocalDate.now()
    val isCurrentMonth = displayedMonth == YearMonth.from(today)
    val canGoNext = displayedMonth.isBefore(YearMonth.now())
    val activeCell = days.firstOrNull { it.day == selectedDay } ?: days.first()
    val dualCount = days.count { it.status == DayMatrixStatus.PERFECT_DUAL }
    val haptic = LocalHapticFeedback.current
    var dragDelta by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .pointerInput(displayedMonth) {
                detectHorizontalDragGestures(
                    onDragStart = { dragDelta = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragDelta += dragAmount },
                    onDragEnd = {
                        if (dragDelta > 70f) {
                            onNavigateMonth(-1)
                        } else if (dragDelta < -70f && canGoNext) {
                            onNavigateMonth(1)
                        }
                        dragDelta = 0f
                    }
                )
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dual Discipline Matrix",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$monthLabel (${days.size} Days)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onNavigateMonth(-1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Bulan sebelumnya",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            if (canGoNext) MaterialTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                        .clickable(enabled = canGoNext) { onNavigateMonth(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Bulan berikutnya",
                        tint = if (canGoNext) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$dualCount",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCurrentMonth && activeCell.day == today.dayOfMonth) "Tgl ${activeCell.day} (Today)" else "Tanggal ${activeCell.day}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (activeCell.status) {
                            DayMatrixStatus.PERFECT_DUAL -> "DUAL ACTIVE"
                            DayMatrixStatus.RUH_ONLY -> "RUH FOCUS"
                            DayMatrixStatus.RAGA_ONLY -> "RAGA FOCUS"
                            DayMatrixStatus.REST -> "NO LOG"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${activeCell.completionPercent}%",
                    style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = activeCell.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            days.chunked(7).forEach { weekRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weekRow.forEach { cell ->
                        val isSelected = cell.day == selectedDay
                        val cellModifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .let { base ->
                                when (cell.status) {
                                    DayMatrixStatus.PERFECT_DUAL -> base.background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                RuhRagaTheme.extendedColors.ragaVibrantOrange.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                                    DayMatrixStatus.RUH_ONLY -> base.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    )
                                    DayMatrixStatus.RAGA_ONLY -> base.background(
                                        RuhRagaTheme.extendedColors.ragaVibrantOrange.copy(alpha = 0.38f)
                                    )
                                    DayMatrixStatus.REST -> base.background(
                                        MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                }
                            }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(10.dp)
                                ) else Modifier
                            )
                            .combinedClickable(
                                onClick = { onSelectDay(cell.day) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onLongClickDay(cell.day)
                                }
                            )

                        Box(
                            modifier = cellModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${cell.day}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected || cell.status == DayMatrixStatus.PERFECT_DUAL) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (cell.status == DayMatrixStatus.PERFECT_DUAL) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    repeat(7 - weekRow.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MatrixLegendItem(color = MaterialTheme.colorScheme.surfaceContainerLow, label = "Rest")
            MatrixLegendItem(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), label = "Ruh Only")
            MatrixLegendItem(color = RuhRagaTheme.extendedColors.ragaVibrantOrange.copy(alpha = 0.5f), label = "Raga Only")
            MatrixLegendItem(color = MaterialTheme.colorScheme.primary, label = "Perfect Dual")
        }

        Text(
            text = "Geser atau gunakan < > untuk ganti bulan • Tekan lama tanggal untuk lihat resume",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayActivityResumeSheet(
    resume: DailyActivityResume,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFormatted = resume.date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID")))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "RESUME AKTIVITAS HARIAN",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResumeScoreChip("Ruh", "${resume.ruhPercent}%", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ResumeScoreChip("Raga", "${resume.ragaPercent}%", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                ResumeScoreChip("Harmony", "${resume.harmonyPercent}%", MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruh (Ibadah)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (resume.completedPrayers.isEmpty() && resume.completedSunnah.isEmpty() && !resume.tilawahDone) {
                    Text(
                        text = "Belum ada catatan ibadah di hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    if (resume.completedPrayers.isNotEmpty()) {
                        val prayersLine = resume.completedPrayers.joinToString(", ") { (name, jamaah) ->
                            if (jamaah) "$name (Jama'ah)" else name
                        }
                        Text(
                            text = "Shalat: $prayersLine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (resume.completedSunnah.isNotEmpty()) {
                        Text(
                            text = "Sunnah: ${resume.completedSunnah.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (resume.tilawahDone) {
                        Text(
                            text = "Tilawah: Selesai ✓",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Raga (Workout)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (resume.workoutItems.isEmpty() && resume.totalReps == 0 && resume.cardioSummary == null) {
                    Text(
                        text = "Belum ada catatan latihan di hari ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    resume.workoutItems.forEach { line ->
                        Text(
                            text = "• $line",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (resume.totalReps > 0) {
                        Text(
                            text = "Total Volume: ${resume.totalReps} reps",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    resume.cardioSummary?.let { cardio ->
                        Text(
                            text = "Cardio: $cardio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResumeScoreChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
private fun MatrixLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeeklyDualBalanceCard(state: RuhRagaUiState) {
    val bars = state.weeklyBars
    val hasAnyData = bars.any { it.ruhPercent > 0f || it.ragaPercent > 0f }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weekly Dual Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasAnyData) "Grafik diperbarui otomatis dari log mingguanmu"
                    else "Mulai catat Ibadah & Workout agar grafik terisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MatrixLegendItem(color = MaterialTheme.colorScheme.primary, label = "Ruh")
                MatrixLegendItem(color = RuhRagaTheme.extendedColors.ragaVibrantOrange, label = "Raga")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { bar ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    if (bar.isPeak) {
                        Text(
                            text = "PEAK",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((82 * bar.ruhPercent).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    if (bar.ruhPercent > 0f) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                        )
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height((82 * bar.ragaPercent).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .background(
                                    if (bar.ragaPercent > 0f) RuhRagaTheme.extendedColors.ragaVibrantOrange
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bar.weekLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (bar.isPeak) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (bar.isPeak) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DisciplineMilestonesSection(milestones: List<MilestoneItem>) {
    val unlockedCount = milestones.count { it.isUnlocked }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Discipline Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Terbuka otomatis sesuai pencapaian harianmu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$unlockedCount / ${milestones.size} Unlocked",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        milestones.forEachIndexed { idx, item ->
            val accent = if (item.isEmerald) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            val icon = when (idx) {
                0 -> Icons.Rounded.WbTwilight
                1 -> Icons.AutoMirrored.Rounded.MenuBook
                2 -> Icons.AutoMirrored.Rounded.DirectionsRun
                else -> Icons.Rounded.LocalFireDepartment
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(accent.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = accent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accent.copy(alpha = 0.14f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (item.isUnlocked) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (item.progressLabel != null) {
                        Text(
                            text = item.progressLabel,
                            style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                item.progressFraction?.let { fraction ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareMonthlySummaryCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.IosShare,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Share Monthly Summary",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Export clean high-res breakdown card",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Export",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
