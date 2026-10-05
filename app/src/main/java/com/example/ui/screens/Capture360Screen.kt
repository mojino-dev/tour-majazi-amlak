package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.util.PersianUtils
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Capture360Screen(
    onCaptureFinished: (drawableRes: Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Total frames required for full 360 sphere (12 shots around)
    val totalFrames = 12
    var capturedFrames by remember { mutableIntStateOf(0) }
    var isStitching by remember { mutableStateOf(false) }
    var stitchProgress by remember { mutableFloatStateOf(0f) }
    var isCompleted by remember { mutableStateOf(false) }

    // Target angle alignment (simulated orientation tracker)
    var deviceRoll by remember { mutableFloatStateOf(0.4f) } // degrees error
    var devicePitch by remember { mutableFloatStateOf(0.6f) } // degrees error
    var isAligned by remember { mutableStateOf(true) }
    var flashEffect by remember { mutableStateOf(false) }

    // Auto-shutter 2° / 300ms rule
    var holdAlignmentMs by remember { mutableIntStateOf(0) }

    LaunchedEffect(capturedFrames, isAligned) {
        if (!isStitching && capturedFrames < totalFrames) {
            // Count 300ms if aligned within 2°
            while (isAligned && holdAlignmentMs < 300) {
                delay(50)
                holdAlignmentMs += 50
            }
            if (isAligned && holdAlignmentMs >= 300) {
                // Trigger auto shutter
                flashEffect = true
                delay(120)
                flashEffect = false
                capturedFrames++
                holdAlignmentMs = 0

                if (capturedFrames >= totalFrames) {
                    isStitching = true
                } else {
                    // Small simulated shift for next target angle
                    deviceRoll = (Math.random() * 2.5 - 1.25).toFloat()
                    devicePitch = (Math.random() * 2.5 - 1.25).toFloat()
                    isAligned = abs(deviceRoll) < 2.0f && abs(devicePitch) < 2.0f
                }
            }
        }
    }

    // Stitching OpenCV Equirectangular Simulation
    LaunchedEffect(isStitching) {
        if (isStitching) {
            stitchProgress = 0f
            while (stitchProgress < 1f) {
                delay(100)
                stitchProgress += 0.05f
            }
            isStitching = false
            isCompleted = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ثبت هوشمند تصویر ۳۶۰ درجه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ماژول بومی CameraX + الگوریتم انطباق خودکار",
                            style = MaterialTheme.typography.bodySmall,
                            color = AccentYellow
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = AccentYellow, modifier = Modifier.size(14.dp))
                            Text(
                                text = "ژیروسکوپ فعال",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .testTag("capture_360_screen")
        ) {
            // Camera viewfinder background simulation
            Image(
                painter = painterResource(id = R.drawable.img_tour_sample),
                contentDescription = "Viewfinder",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            deviceRoll = (deviceRoll + dragAmount.x * 0.05f).coerceIn(-5f, 5f)
                            devicePitch = (devicePitch + dragAmount.y * 0.05f).coerceIn(-5f, 5f)
                            isAligned = abs(deviceRoll) <= 2.0f && abs(devicePitch) <= 2.0f
                            if (!isAligned) holdAlignmentMs = 0
                        }
                    }
            )

            // Flash effect on auto-shutter
            if (flashEffect) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.85f))
                )
            }

            // Normal Viewfinder HUD Overlay
            if (!isStitching && !isCompleted) {
                // Top rule badge (2° / 300ms)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.75f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAligned) BrandSecondary else AccentOrange),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAligned) Icons.Default.CheckCircle else Icons.Default.RotateRight,
                                contentDescription = null,
                                tint = if (isAligned) BrandSecondary else AccentOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAligned) "انطباق هدف تایید شد (ثبت در ۳۰۰ میلی‌ثانیه)" else "گوشی را در راستای دایره هدف قرار دهید (خطا: ${PersianUtils.toPersianDigits(String.format("%.1f", abs(deviceRoll)))}°)",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Center Guided Target Reticle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer guide ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 3.dp,
                                color = if (isAligned) BrandSecondary else AccentYellow.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    )

                    // Target indicator dot moving with gyro error
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (deviceRoll * 20f).roundToInt(),
                                    (devicePitch * 20f).roundToInt()
                                )
                            }
                            .size(24.dp)
                            .background(
                                color = if (isAligned) BrandSecondary else AccentOrange,
                                shape = CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape)
                    )

                    // Crosshair tick marks
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color.White, CircleShape)
                    )
                }

                // Bottom HUD: Frame progress and manual shutter fallback
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Spherical Shot Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "پیشرفت عکسبرداری پانوراما:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = "${PersianUtils.toPersianDigits(capturedFrames)} از ${PersianUtils.toPersianDigits(totalFrames)} فریم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow
                        )
                    }

                    LinearProgressIndicator(
                        progress = { capturedFrames.toFloat() / totalFrames },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentYellow,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )

                    // Shutter button & Tips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    Toast.makeText(context, "گوشی را ثابت روی محور نگه دارید", Toast.LENGTH_SHORT).show()
                                },
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "راهنمای زاویه ۲ درجه",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Shutter Button (auto or manual tap)
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .border(3.dp, Color.White, CircleShape)
                                .padding(6.dp)
                                .clip(CircleShape)
                                .background(if (isAligned) BrandSecondary else AccentYellow)
                                .clickable {
                                    capturedFrames++
                                    if (capturedFrames >= totalFrames) isStitching = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "ثبت",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    // Align instantly for testing
                                    deviceRoll = 0.2f
                                    devicePitch = 0.1f
                                    isAligned = true
                                },
                            color = AccentYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "تراز سریع",
                                color = AccentYellow,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Stitching Screen (OpenCV 2:1 stitching)
            if (isStitching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { stitchProgress },
                            modifier = Modifier.size(76.dp),
                            color = AccentYellow,
                            strokeWidth = 6.dp
                        )

                        Text(
                            text = "اتصال و دوخت تصاویر با OpenCV...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "در حال ایجاد پانورامای ۲:۱ اکورکتانگولار و درج متادیتای GPano...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${PersianUtils.toPersianDigits((stitchProgress * 100).toInt())}٪",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow
                        )
                    }
                }
            }

            // Completed Screen
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandSecondary,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }

                        Text(
                            text = "تصویر ۳۶۰ درجه با موفقیت ثبت شد!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "فایل پانوراما با متادیتای GPano به صورت محلی در دستگاه ذخیره شد و آماده استفاده در تور مجازی می‌باشد.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "پانوراما به پروژه تور اضافه شد", Toast.LENGTH_SHORT).show()
                                onCaptureFinished(R.drawable.img_tour_sample)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "افزودن به تور مجازی",
                                color = Color.Black,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
