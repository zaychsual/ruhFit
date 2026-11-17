package com.ruhraga.tracker.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruhraga.tracker.ui.theme.RuhRagaTextStyles
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    fun calculateBearingDegrees(
        latitude: Double = -6.2088,
        longitude: Double = 106.8456
    ): Float {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val dLon = Math.toRadians(KAABA_LNG - longitude)

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360.0) % 360.0).toFloat()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaCompassSheet(
    onDismiss: () -> Unit,
    latitude: Double = -6.2088,
    longitude: Double = 106.8456,
    cityLabel: String = "Jakarta, Indonesia"
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val qiblaBearing = remember(latitude, longitude) {
        QiblaCalculator.calculateBearingDegrees(latitude, longitude)
    }

    var azimuth by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            hasSensor = false
            onDispose { }
        } else {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val degrees = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                    azimuth = azimuth * 0.8f + degrees * 0.2f
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val dialRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = tween(durationMillis = 150),
        label = "dialRotation"
    )
    val qiblaArrowAngle = qiblaBearing - azimuth
    val angleDiff = abs(((qiblaArrowAngle + 540f) % 360f) - 180f)
    val isAligned = angleDiff <= 6f

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kompas Arah Kiblat",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = cityLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${qiblaBearing.toInt()}° NW",
                    style = RuhRagaTextStyles.TabularBodyMd.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // Compass Dial
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val surfaceLow = MaterialTheme.colorScheme.surfaceContainerLow
            val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(surfaceLow)
                    .border(
                        width = 2.dp,
                        color = if (isAligned) primaryColor else trackColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    rotate(degrees = dialRotation, pivot = center) {
                        // Cardinal tick marks
                        for (deg in 0 until 360 step 15) {
                            val isCardinal = deg % 90 == 0
                            val tickLen = if (isCardinal) 14.dp.toPx() else 7.dp.toPx()
                            val rad = Math.toRadians(deg.toDouble())
                            val start = Offset(
                                center.x + (radius - tickLen) * sin(rad).toFloat(),
                                center.y - (radius - tickLen) * cos(rad).toFloat()
                            )
                            val end = Offset(
                                center.x + radius * sin(rad).toFloat(),
                                center.y - radius * cos(rad).toFloat()
                            )
                            drawLine(
                                color = if (deg == 0) secondaryColor else textMuted.copy(alpha = 0.45f),
                                start = start,
                                end = end,
                                strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Qibla pointer needle
                    rotate(degrees = qiblaArrowAngle, pivot = center) {
                        drawLine(
                            color = primaryColor,
                            start = center,
                            end = Offset(center.x, center.y - radius + 22.dp.toPx()),
                            strokeWidth = 5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 10.dp.toPx(),
                            center = Offset(center.x, center.y - radius + 24.dp.toPx())
                        )
                    }

                    drawCircle(
                        color = Color(0xFFDAE2FD),
                        radius = 6.dp.toPx(),
                        center = center
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mosque,
                        contentDescription = null,
                        tint = if (isAligned) primaryColor else textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isAligned) primaryColor.copy(alpha = 0.14f)
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAligned) Icons.Rounded.CheckCircle else Icons.Rounded.Explore,
                    contentDescription = null,
                    tint = if (isAligned) primaryColor else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        !hasSensor -> "Sensor kompas tidak tersedia. Arah Kiblat: ${qiblaBearing.toInt()}° Barat Laut (NW)."
                        isAligned -> "Pas! Perangkat menghadap arah Kiblat (Ka'bah)."
                        else -> "Putar perangkat hingga jarum hijau mengarah ke atas (${qiblaBearing.toInt()}° NW)."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isAligned) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isAligned) primaryColor else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
