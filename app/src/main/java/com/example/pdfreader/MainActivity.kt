package com.example.pdfreader

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileCircleTheme {
                ProfileCircleScreen()
            }
        }
    }
}

@Composable
fun ProfileCircleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            onBackground = Color.White
        ),
        content = content
    )
}

enum class AnimationState {
    Initial,
    FirstImageMovingUp,
    ImagesAppearing,
    RotationStarted,
    Complete
}

@Composable
fun ProfileCircleScreen() {
    val context = LocalContext.current

    // Sample profile images - replace with your actual drawable resources
    val profileImages = listOf(
        R.drawable.profile1,
        R.drawable.profile2,
        R.drawable.profile3,
        R.drawable.profile4,
        R.drawable.profile5,
        R.drawable.profile6,
        R.drawable.profile7,
        R.drawable.profile8
    )

    // Convert drawables to ImageBitmap with error handling
    val imageBitmaps = remember {
        profileImages.mapNotNull { resId ->
            try {
                val drawable = context.getDrawable(resId)
                if (drawable != null) {
                    val bmp = Bitmap.createBitmap(
                        drawable.intrinsicWidth.coerceAtLeast(100),
                        drawable.intrinsicHeight.coerceAtLeast(100),
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)
                    bmp.asImageBitmap()
                } else {
                    Log.w("ProfileCircle", "Could not load drawable with id: $resId")
                    null
                }
            } catch (e: Exception) {
                Log.e("ProfileCircle", "Error loading image: $resId", e)
                null
            }
        }
    }

    // Ensure we have at least one image to work with
    if (imageBitmaps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No profile images available",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        return
    }

    var animationState by remember { mutableStateOf(AnimationState.Initial) }
    var showMainText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }
    var showSecondaryText by remember { mutableStateOf(false) }

    // Debug logging for animation states
    LaunchedEffect(animationState) {
        Log.d("ProfileCircle", "Animation state changed to: $animationState")
    }

    // Smoother first image upward movement with spring animation
    val firstImageOffsetY by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.Initial -> 0f
            else -> -110f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "firstImageOffset"
    )

    // Enhanced first image scale animation with better transitions
    val firstImageScale by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.Initial -> 1.2f
            AnimationState.FirstImageMovingUp -> 1.0f
            AnimationState.ImagesAppearing -> 0.8f
            else -> 0.64f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "firstImageScale"
    )

    // Improved images appearing animation with smoother progression
    val imagesAppearProgress by animateFloatAsState(
        targetValue = when (animationState) {
            AnimationState.ImagesAppearing, AnimationState.RotationStarted, AnimationState.Complete -> 1f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "imagesAppear"
    )

    // Continuous rotation animation - optimized for smoother performance
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Apply rotation only when rotation state is active
    val currentRotation = when (animationState) {
        AnimationState.RotationStarted, AnimationState.Complete -> rotation
        else -> 0f
    }

    // Enhanced sequential animation timing with better flow
    LaunchedEffect(Unit) {
        Log.d("ProfileCircle", "Starting animation sequence")
        delay(500) // Reduced initial delay

        // Step 1: Move first image upward
        Log.d("ProfileCircle", "Step 1: Moving first image up")
        animationState = AnimationState.FirstImageMovingUp
        delay(800) // Reduced delay for snappier feel

        // Step 2: Start images appearing
        Log.d("ProfileCircle", "Step 2: Images appearing")
        animationState = AnimationState.ImagesAppearing
        delay(1800) // Adjusted for smoother flow

        // Step 3: Start rotation
        Log.d("ProfileCircle", "Step 3: Starting rotation")
        animationState = AnimationState.RotationStarted
        delay(1000)

        // Step 4: Show text elements with optimized timing
        Log.d("ProfileCircle", "Step 4: Showing text elements")
        showMainText = true
        delay(600)

        showButton = true
        delay(500)

        showSecondaryText = true
        delay(300)

        // Step 5: Complete
        Log.d("ProfileCircle", "Animation sequence complete")
        animationState = AnimationState.Complete
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Profile Circle Animation Container
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressProfileCanvas(
                profileBitmaps = imageBitmaps,
                firstImageOffsetY = firstImageOffsetY,
                firstImageScale = firstImageScale,
                imagesAppearProgress = imagesAppearProgress,
                rotation = currentRotation,
                animationState = animationState
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Main text with improved entrance animation
        AnimatedVisibility(
            visible = showMainText,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialOffsetY = { it / 3 }
            )
        ) {
            Text(
                text = "Get to know the UI/UX wizards\ncrafting pixel-perfect experiences",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Enhanced connect button animation
        AnimatedVisibility(
            visible = showButton,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetY = { it / 2 }
            ) + fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.8f
            )
        ) {
            Button(
                onClick = {
                    Log.d("ProfileCircle", "Connect button clicked")
                    // Handle connect action
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A2A2A)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(52.dp)
            ) {
                Text(
                    text = "Connect",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary text with refined entrance
        AnimatedVisibility(
            visible = showSecondaryText,
            enter = fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "The next wave of creativity is brewing.",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Be part of it.",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CircularProgressProfileCanvas(
    profileBitmaps: List<ImageBitmap>,
    firstImageOffsetY: Float,
    firstImageScale: Float,
    imagesAppearProgress: Float,
    rotation: Float,
    animationState: AnimationState
) {
    val density = LocalDensity.current
    val radius = with(density) { 110.dp.toPx() }
    val center = with(density) { 160.dp.toPx() }
    val imageSize = with(density) { 64.dp.toPx() }
    val count = profileBitmaps.size

    // Debug logging for key values
    LaunchedEffect(imagesAppearProgress, rotation) {
        Log.d("ProfileCircle", "Canvas - imagesAppearProgress: $imagesAppearProgress, rotation: $rotation")
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Ensure we have images to draw
        if (profileBitmaps.isEmpty()) return@Canvas

        // Calculate total images in circle (all images including the first one)
        val totalImagesInCircle = count

        // Draw all images in a circle when rotation starts
        if (animationState == AnimationState.RotationStarted || animationState == AnimationState.Complete) {
            // Draw all images in the circle including the first one
            for (i in 0 until count) {
                // Calculate angle for this image (evenly distributed)
                val baseAngle = (360f / totalImagesInCircle) * i
                val startAngle = -90f // Start from top (-90 degrees)
                val targetAngle = startAngle + baseAngle

                // Add rotation offset
                val finalAngle = targetAngle + rotation

                val angleRad = Math.toRadians(finalAngle.toDouble())
                val x = center + radius * cos(angleRad).toFloat()
                val y = center + radius * sin(angleRad).toFloat()

                drawProfileImage(
                    bitmap = profileBitmaps[i],
                    x = x,
                    y = y,
                    size = imageSize * 0.64f,
                    alpha = 1f
                )
            }
        } else {
            // Pre-rotation state: Draw first image in center/moving up position
            val firstImageY = center + with(density) { firstImageOffsetY.dp.toPx() }
            val firstImageSize = imageSize * firstImageScale

            drawProfileImage(
                bitmap = profileBitmaps[0],
                x = center,
                y = firstImageY,
                size = firstImageSize,
                alpha = 1f
            )

            // Draw other images appearing around the circle (excluding first image)
            val otherImagesCount = count - 1
            if (otherImagesCount > 0) {
                for (i in 1 until count) {
                    val imageIndex = i - 1

                    // Enhanced staggered appearance calculation
                    val staggerDelay = imageIndex * 0.15f
                    val individualProgress = ((imagesAppearProgress - staggerDelay) / (1f - staggerDelay))
                        .coerceIn(0f, 1f)

                    if (individualProgress > 0f) {
                        // Calculate angle for this image (evenly distributed among remaining images)
                        val baseAngle = (360f / otherImagesCount) * imageIndex
                        val startAngle = -90f // Start from top
                        val targetAngle = startAngle + baseAngle

                        val angleRad = Math.toRadians(targetAngle.toDouble())
                        val x = center + radius * cos(angleRad).toFloat()
                        val y = center + radius * sin(angleRad).toFloat()

                        // Enhanced easing with custom curve
                        val easedProgress = easeOutBack(individualProgress)
                        val size = imageSize * 0.64f * easedProgress
                        val alpha = easedProgress

                        // Refined bounce effect
                        val bounceScale = if (easedProgress < 1f) {
                            1f + (0.15f * (1f - easedProgress) * sin(easedProgress * Math.PI * 3).toFloat())
                        } else {
                            1f
                        }

                        drawProfileImage(
                            bitmap = profileBitmaps[i],
                            x = x,
                            y = y,
                            size = size * bounceScale,
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawProfileImage(
    bitmap: ImageBitmap,
    x: Float,
    y: Float,
    size: Float,
    alpha: Float
) {
    if (alpha <= 0f || size <= 0f) return

    drawIntoCanvas { canvas ->
        val left = x - size / 2
        val top = y - size / 2

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            this.alpha = (255 * alpha).toInt()
        }

        val rect = android.graphics.RectF(left, top, left + size, top + size)

        canvas.nativeCanvas.apply {
            save()

            // Enhanced border with improved glow effect
            val borderPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = 2.5f
                color = android.graphics.Color.argb(
                    (200 * alpha).toInt(),
                    135, 206, 235
                )
                setShadowLayer(6f, 0f, 0f, android.graphics.Color.argb(
                    (120 * alpha).toInt(),
                    135, 206, 235
                ))
            }
            drawOval(rect, borderPaint)

            // Create circular clip path for image with improved bounds
            val innerRect = android.graphics.RectF(
                rect.left + 1.25f,
                rect.top + 1.25f,
                rect.right - 1.25f,
                rect.bottom - 1.25f
            )

            val path = android.graphics.Path().apply {
                addOval(innerRect, android.graphics.Path.Direction.CW)
            }

            clipPath(path)
            drawBitmap(bitmap.asAndroidBitmap(), null, innerRect, paint)
            restore()
        }
    }
}

// Enhanced easing functions
private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    return 1f + c3 * (t - 1f).pow(3f) + c1 * (t - 1f).pow(2f)
}