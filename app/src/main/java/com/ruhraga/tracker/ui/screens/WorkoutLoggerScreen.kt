package com.ruhraga.tracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhraga.tracker.ui.state.CardioMode
import com.ruhraga.tracker.ui.state.CustomWorkoutItem
import com.ruhraga.tracker.ui.state.ExerciseCategory
import com.ruhraga.tracker.ui.state.RuhRagaUiState
import com.ruhraga.tracker.ui.theme.RuhRagaTextStyles
import com.ruhraga.tracker.ui.theme.RuhRagaTheme

@Composable
fun WorkoutLoggerScreen(
    state: RuhRagaUiState,
    onSkipRest: () -> Unit,
    onOpenFullscreenTimer: () -> Unit,
    onSelectCardioMode: (CardioMode) -> Unit,
    onLogMetabolicFinisher: () -> Unit,
    onAddPlanItem: (String, Int, Int) -> Unit = { _, _, _ -> },
    onRemovePlanItem: (String) -> Unit = {},
    onFinishPlanSetup: () -> Unit = {},
    onReopenPlanSetup: () -> Unit = {},
    onUpdatePlanSets: (String, Int) -> Unit = { _, _ -> },
    onUpdatePlanReps: (String, Int) -> Unit = { _, _ -> },
    onTogglePlanCompleted: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WorkoutHeader(onOpenFullscreenTimer = onOpenFullscreenTimer)
        }
        item {
            if (!state.isWorkoutPlanConfigured) {
                WorkoutPlanSetupCard(
                    planItems = state.workoutPlan,
                    onAddItem = onAddPlanItem,
                    onRemoveItem = onRemovePlanItem,
                    onFinishSetup = onFinishPlanSetup
                )
            } else {
                DailyWorkoutChecklistSection(
                    items = state.workoutPlan,
                    onReopenSetup = onReopenPlanSetup,
                    onUpdateSets = onUpdatePlanSets,
                    onUpdateReps = onUpdatePlanReps,
                    onToggleComplete = onTogglePlanCompleted
                )
            }
        }
        item {
            RestTimerBannerCard(
                restSeconds = state.restSecondsRemaining,
                onSkipRest = onSkipRest,
                onOpenFullscreen = onOpenFullscreenTimer
            )
        }
        item {
            CardioMetconCard(
                state = state,
                onSelectMode = onSelectCardioMode,
                onLogFinisher = onLogMetabolicFinisher
            )
        }
        item {
            WorkoutHistoryCard(state = state)
        }
    }
}

@Composable
private fun WorkoutHeader(onOpenFullscreenTimer: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Daily Calisthenics Mastery",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Upper Body & Core Focus",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onOpenFullscreenTimer)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = "Open Fullscreen Timer",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "24:18",
                style = RuhRagaTextStyles.TabularBodyMd,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExerciseSelectorRow(
    selected: ExerciseCategory,
    onSelect: (ExerciseCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExerciseCategory.entries.forEach { category ->
            val isSelected = category == selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) RuhRagaTheme.extendedColors.ragaVibrantOrange
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .clickable { onSelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = "${category.label} (${category.targetReps})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RepCounterHubCard(
    state: RuhRagaUiState,
    onAdjustReps: (Int) -> Unit,
    onLogSetAndStartRest: () -> Unit
) {
    val target = state.selectedExercise.targetReps
    val progress = (state.currentExerciseReps.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SET ${state.currentSetNumber} OF ${state.totalSetsTarget}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ruh & Raga Synced",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("${state.currentExerciseReps}")
                }
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(" / $target")
                }
            },
            style = RuhRagaTextStyles.DisplayHero
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total Reps Completed ($percent%)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(-1, 5, 10, 25).forEach { delta ->
                val isHighlighted = delta == 10
                val label = if (delta > 0) "+$delta" else "$delta"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isHighlighted) RuhRagaTheme.extendedColors.ragaVibrantOrange
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .clickable { onAdjustReps(delta) }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                        color = if (isHighlighted) MaterialTheme.colorScheme.onSecondary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Button(
            onClick = onLogSetAndStartRest,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = RuhRagaTheme.extendedColors.ragaVibrantOrange,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.DoneAll,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Set & Start Rest",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun RestTimerBannerCard(
    restSeconds: Int,
    onSkipRest: () -> Unit,
    onOpenFullscreen: () -> Unit
) {
    val minutes = restSeconds / 60
    val seconds = restSeconds % 60
    val formatted = "%02d:%02d".format(minutes, seconds)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onOpenFullscreen)
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.HourglassBottom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatted,
                        style = RuhRagaTextStyles.MetricNumMd,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REST",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Recommended recovery window • Tap for timer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onSkipRest)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TechniqueCueCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "TECHNIQUE CUE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Full Scapular Protraction",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Push the ground away at peak lockout. Inhale intention, exhale drive.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompletedSetsCard(state: RuhRagaUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Completed Sets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "TARGET: ${state.totalSetsTarget} SETS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        state.completedSets.forEach { setItem ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${setItem.setNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${setItem.reps} reps • ${setItem.restFormatted}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(RuhRagaTheme.extendedColors.ragaVibrantOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${state.currentSetNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Logging now (Target: 25 reps)...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CardioMetconCard(
    state: RuhRagaUiState,
    onSelectMode: (CardioMode) -> Unit,
    onLogFinisher: () -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.DirectionsRun,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cardio & Metcon\nAdd-on",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CardioMode.entries.forEach { mode ->
                    val selected = mode == state.cardioMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) RuhRagaTheme.extendedColors.ragaVibrantOrange
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) MaterialTheme.colorScheme.onSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetconStatBox(
                label = "DISTANCE",
                value = "${state.cardioDistanceKm}",
                unit = "km",
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            MetconStatBox(
                label = "DURATION",
                value = "${state.cardioDurationMin}",
                unit = "min",
                valueColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            MetconStatBox(
                label = "BURNED",
                value = "${state.cardioCalories}",
                unit = "kcal",
                valueColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable(onClick = onLogFinisher)
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AddCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Metabolic Finisher",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Kalori dihitung dengan rumus MET × ${state.userProfile.weightKg.toInt()} kg × durasi (Run: 9.8 MET, Jump Rope: 11.8 MET)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WorkoutPlanSetupCard(
    planItems: List<CustomWorkoutItem>,
    onAddItem: (String, Int, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onFinishSetup: () -> Unit
) {
    var exerciseName by remember { mutableStateOf("") }
    var sets by remember { mutableIntStateOf(3) }
    var reps by remember { mutableIntStateOf(15) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                1.dp,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Atur Daftar Latihanmu",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Masukkan latihan apa saja yang ingin kamu jalani setiap hari beserta target set & repetisi.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Push-ups", "Pull-ups", "Dips", "Squats", "Sit-ups").forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { exerciseName = preset }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ $preset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Nama Latihan") },
            placeholder = { Text("Contoh: Push-ups") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperBox(
                label = "Set",
                value = sets,
                onMinus = { if (sets > 1) sets-- },
                onPlus = { if (sets < 20) sets++ },
                modifier = Modifier.weight(1f)
            )
            StepperBox(
                label = "Reps",
                value = reps,
                onMinus = { if (reps > 1) reps-- },
                onPlus = { if (reps < 200) reps++ },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (exerciseName.isNotBlank()) {
                        onAddItem(exerciseName, sets, reps)
                        exerciseName = ""
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Tambah", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (planItems.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                planItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${item.sets} set × ${item.repsPerSet} reps (${item.totalReps} total)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onRemoveItem(item.id) }
                        )
                    }
                }
            }

            Button(
                onClick = onFinishSetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RuhRagaTheme.extendedColors.ragaVibrantOrange,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = "Simpan Daftar Latihan (${planItems.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun DailyWorkoutChecklistSection(
    items: List<CustomWorkoutItem>,
    onReopenSetup: () -> Unit,
    onUpdateSets: (String, Int) -> Unit,
    onUpdateReps: (String, Int) -> Unit,
    onToggleComplete: (String) -> Unit
) {
    val completedCount = items.count { it.isCompletedToday }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Checklist Latihan Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$completedCount / ${items.size} latihan selesai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable(onClick = onReopenSetup)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ubah Daftar",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        items.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (item.isCompletedToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .then(
                        if (item.isCompletedToday) Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        ) else Modifier
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${item.sets} set × ${item.repsPerSet} reps = ${item.totalReps} reps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (item.isCompletedToday) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else RuhRagaTheme.extendedColors.ragaVibrantOrange
                            )
                            .clickable(enabled = !item.isCompletedToday) { onToggleComplete(item.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (item.isCompletedToday) "Selesai ✓" else "Tandai",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (item.isCompletedToday) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }

                if (!item.isCompletedToday) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StepperBox(
                            label = "Set",
                            value = item.sets,
                            onMinus = { onUpdateSets(item.id, -1) },
                            onPlus = { onUpdateSets(item.id, 1) },
                            modifier = Modifier.weight(1f)
                        )
                        StepperBox(
                            label = "Repetisi",
                            value = item.repsPerSet,
                            onMinus = { onUpdateReps(item.id, -1) },
                            onPlus = { onUpdateReps(item.id, 1) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutAlarmConfigCard(
    alarmEnabled: Boolean,
    volume: Float,
    isPreviewPlaying: Boolean,
    onToggleAlarm: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onTogglePreview: () -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Alarm Selesai Istirahat",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Dering: Electric Eel (electric_eel.mp3)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = alarmEnabled,
                onCheckedChange = { onToggleAlarm() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                    checkedTrackColor = RuhRagaTheme.extendedColors.ragaVibrantOrange,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Volume Alarm",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                enabled = alarmEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = RuhRagaTheme.extendedColors.ragaVibrantOrange,
                    activeTrackColor = RuhRagaTheme.extendedColors.ragaVibrantOrange,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isPreviewPlaying) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .clickable(enabled = alarmEnabled, onClick = onTogglePreview)
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = if (isPreviewPlaying) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = if (isPreviewPlaying) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPreviewPlaying) "Stop Dering" else "Tes Dering",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPreviewPlaying) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperBox(
    label: String,
    value: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Remove,
            contentDescription = "Kurangi $label",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onMinus)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$value",
                style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Tambah $label",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onPlus)
        )
    }
}

@Composable
private fun WorkoutHistoryCard(state: RuhRagaUiState) {
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
            Column {
                Text(
                    text = "Riwayat Workout",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tersimpan otomatis di database SQLite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Total: ${state.allTimeWorkoutReps} reps",
                style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        if (state.workoutHistory.isEmpty()) {
            Text(
                text = "Belum ada riwayat workout tersimpan. Mulai tambah reps atau log set untuk menyimpan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.workoutHistory.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${entry.date} • ${entry.subtitle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = entry.metricLabel,
                        style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                        color = if (entry.isCardio) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetconStatBox(
    label: String,
    value: String,
    unit: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = RuhRagaTextStyles.MetricNumMd,
                color = valueColor
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
