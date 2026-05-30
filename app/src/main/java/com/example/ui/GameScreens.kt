package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.game.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// MAIN GRAPHICS ENGINE
@Composable
fun FoodItemGraphic(
    foodType: FoodType,
    specialType: SpecialType,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    // Pulse animation if selected
    val infiniteTransition = rememberInfiniteTransition(label = "select_pulse")
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = if (isSelected) 0.9f else 1.0f,
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Twinkle for specials
    val specialAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle_rotate"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scaleFactor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Special indicator background ring glows
        if (specialType != SpecialType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize().rotate(specialAngle)) {
                val sizeVal = size.minDimension
                val strokeW = sizeVal * 0.08f
                
                when (specialType) {
                    SpecialType.SPICY_SAMOSA_BOMB -> {
                        // Spiky red star glow wrapper
                        drawCircle(
                            color = Color(0xFFFF5722),
                            radius = sizeVal * 0.46f,
                            style = Stroke(width = strokeW, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
                        )
                    }
                    SpecialType.JALEBI_SWIRL_ROW -> {
                        // Orange swirl line horizontal tracker
                        drawLine(
                            color = Color(0xFFFF9800),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = strokeW
                        )
                    }
                    SpecialType.PANI_PURI_SPLASH_COL -> {
                        // Mint green vertical splash tracker
                        drawLine(
                            color = Color(0xFF4CAF50),
                            start = Offset(size.width / 2, 0f),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = strokeW
                        )
                    }
                    SpecialType.BIRYANI_FEAST -> {
                        // Multi-colored golden radiant circular aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFF5722), Color.Transparent),
                                center = center,
                                radius = sizeVal * 0.5f
                            )
                        )
                    }
                    else -> {}
                }
            }
        }

        // The actual food item graphic drawing
        Canvas(modifier = Modifier.fillMaxSize().padding(1.dp)) {
            val width = size.width
            val height = size.height
            val cx = width / 2
            val cy = height / 2
            val radius = size.minDimension * 0.38f

            when (foodType) {
                FoodType.SAMOSA -> {
                    // Golden-brown vector triangle
                    val path = Path().apply {
                        moveTo(cx, cy - radius * 1.1f)
                        lineTo(cx - radius * 1.1f, cy + radius * 0.9f)
                        lineTo(cx + radius * 1.1f, cy + radius * 0.9f)
                        close()
                    }
                    drawPath(path = path, color = Color(0xFFE59866))
                    
                    // Golden inner shadow shade line
                    val shadePath = Path().apply {
                        moveTo(cx, cy - radius * 1.0f)
                        lineTo(cx + radius * 1.0f, cy + radius * 0.85f)
                        lineTo(cx, cy + radius * 0.85f)
                        close()
                    }
                    drawPath(path = shadePath, color = Color(0xFFD35400).copy(alpha = 0.35f))

                    // Crispy pleated bottom ridges
                    val ridgeY = cy + radius * 0.9f
                    val step = (radius * 2.2f) / 5f
                    for (i in 0..4) {
                        val rx = (cx - radius * 1.1f) + (i * step) + (step / 2f)
                        drawCircle(
                            color = Color(0xFFC0392B),
                            radius = width * 0.05f,
                            center = Offset(rx, ridgeY)
                        )
                    }

                    // Hot masala black flakes sprinkle
                    drawCircle(Color(0xFF283747), radius = width * 0.03f, center = Offset(cx - 10f, cy + 5f))
                    drawCircle(Color(0xFF283747), radius = width * 0.02f, center = Offset(cx + 8f, cy - 10f))
                    drawCircle(Color(0xFF2E4053), radius = width * 0.025f, center = Offset(cx + 5f, cy + 20f))
                }

                FoodType.IDLI -> {
                    // Soft light green banana leaf coaster background
                    drawCircle(
                        color = Color(0xFF43A047),
                        radius = radius * 1.05f,
                        center = Offset(cx, cy)
                    )
                    
                    // Double deck banana leaf stripe
                    drawLine(
                        color = Color(0xFF1B5E20),
                        start = Offset(cx - radius, cy),
                        end = Offset(cx + radius, cy),
                        strokeWidth = width * 0.04f
                    )

                    // Soft ivory fluffy idli disc
                    drawCircle(
                        color = Color(0xFFFDFefe),
                        radius = radius * 0.85f,
                        center = Offset(cx, cy)
                    )
                    // Inner accent drop shadow
                    drawCircle(
                        color = Color(0xFFEAEDED).copy(alpha = 0.7f),
                        radius = radius * 0.72f,
                        center = Offset(cx - 3f, cy + 3f)
                    )

                    // Delicious green chutney spot (Coconut & Chutney)
                    drawCircle(
                        color = Color(0xFF81C784),
                        radius = radius * 0.22f,
                        center = Offset(cx + radius * 0.3f, cy - radius * 0.2f)
                    )
                    drawCircle(
                        color = Color(0xFFFFB74D), // Red tomato chutney spot
                        radius = radius * 0.16f,
                        center = Offset(cx - radius * 0.25f, cy + radius * 0.3f)
                    )
                }

                FoodType.DOSA -> {
                    // Draw a brown rolled plate backing
                    val platePath = Path().apply {
                        addArc(Rect(Offset(cx - radius * 1.1f, cy - radius * 1.1f), Size(radius * 2.2f, radius * 2.2f)), -30f, 240f)
                    }
                    drawPath(platePath, Color(0xFF7F4F24).copy(alpha = 0.3f), style = Stroke(width = 6f))

                    // Dynamic folded cylinder crepe diagonally
                    val dosaPath = Path().apply {
                        moveTo(cx - radius * 1.0f, cy + radius * 0.6f)
                        lineTo(cx + radius * 0.6f, cy - radius * 1.0f)
                        lineTo(cx + radius * 1.1f, cy - radius * 0.5f)
                        lineTo(cx - radius * 0.5f, cy + radius * 1.1f)
                        close()
                    }
                    drawPath(dosaPath, Color(0xFFE5A93C))

                    // Crispy brown spots overlay (authentic tawa cook)
                    drawLine(
                        color = Color(0xFF873600),
                        start = Offset(cx - radius * 0.5f, cy + radius * 0.3f),
                        end = Offset(cx, cy - radius * 0.2f),
                        strokeWidth = width * 0.08f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF935116),
                        start = Offset(cx + radius * 0.2f, cy - radius * 0.5f),
                        end = Offset(cx + radius * 0.5f, cy - radius * 0.2f),
                        strokeWidth = width * 0.06f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF7E5109),
                        start = Offset(cx - radius * 0.2f, cy + radius * 0.6f),
                        end = Offset(cx + radius * 0.1f, cy + radius * 0.3f),
                        strokeWidth = width * 0.05f,
                        cap = StrokeCap.Round
                    )
                }

                FoodType.VADA -> {
                    // Bottom red savory chutney backdrop shadow
                    drawCircle(
                        color = Color(0xFFD98880).copy(alpha = 0.55f),
                        radius = radius * 1.0f,
                        center = Offset(cx + 4f, cy + 6f)
                    )

                    // Main golden ring vada donut shape
                    drawCircle(
                        color = Color(0xFFB9770E),
                        radius = radius * 0.88f,
                        center = Offset(cx, cy)
                    )

                    // Lighter crispy tawa finish highlight ring
                    drawCircle(
                        color = Color(0xFFF5B041),
                        radius = radius * 0.72f,
                        center = Offset(cx, cy),
                        style = Stroke(width = width * 0.14f)
                    )

                    // Transparent central vada hole
                    drawCircle(
                        color = Color.Transparent,
                        radius = radius * 0.26f,
                        center = Offset(cx, cy),
                        blendMode = BlendMode.Clear
                    )
                    // Draw clean inner stroke so transparent cutout is framed nicely
                    drawCircle(
                        color = Color(0xFF7E5109),
                        radius = radius * 0.26f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 4f)
                    )

                    // Curry leaves cumin specs sprinkles on the Ring
                    drawCircle(Color(0xFF229954), radius = width * 0.04f, center = Offset(cx - radius * 0.45f, cy - radius * 0.35f))
                    drawCircle(Color(0xFF2C3E50), radius = width * 0.035f, center = Offset(cx + radius * 0.48f, cy + radius * 0.1f))
                    drawCircle(Color(0xFF1B4F72), radius = width * 0.03f, center = Offset(cx - radius * 0.1f, cy - radius * 0.5f))
                }

                FoodType.JALEBI -> {
                    // Bright festive neon orange concentric jalebi spirals (Classic street sweet)
                    val spiralPath = Path().apply {
                        // Loop 1
                        addArc(Rect(Offset(cx - radius * 0.8f, cy - radius * 0.8f), Size(radius * 1.6f, radius * 1.6f)), 0f, 360f)
                        // Loop 2 (Intertwining)
                        addArc(Rect(Offset(cx - radius * 0.55f, cy - radius * 0.55f), Size(radius * 1.1f, radius * 1.1f)), 140f, 360f)
                        // Central sweet loop
                        addArc(Rect(Offset(cx - radius * 0.3f, cy - radius * 0.3f), Size(radius * 0.6f, radius * 0.6f)), 260f, 360f)
                        // Twist tail
                        moveTo(cx, cy + radius * 0.2f)
                        lineTo(cx + radius * 0.95f, cy + radius * 0.2f)
                    }
                    
                    // Shadow thick trace
                    drawPath(
                        path = spiralPath,
                        color = Color(0xFFBA4A00),
                        style = Stroke(width = width * 0.18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Vibrant neon core
                    drawPath(
                        path = spiralPath,
                        color = Color(0xFFFF5722),
                        style = Stroke(width = width * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Shiny sweet syrup gloss lines
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = width * 0.04f, center = Offset(cx - radius * 0.4f, cy - radius * 0.4f))
                    drawCircle(Color.White.copy(alpha = 0.8f), radius = width * 0.03f, center = Offset(cx + radius * 0.5f, cy + radius * 0.1f))
                }

                FoodType.GULAB_JAMUN -> {
                    // Miniature brass bowl background
                    val bowlPath = Path().apply {
                        addArc(Rect(Offset(cx - radius * 1.0f, cy - radius * 0.5f), Size(radius * 2f, radius * 1.8f)), 0f, 180f)
                    }
                    drawPath(bowlPath, Color(0xFFBDC3C7))
                    drawLine(Color(0xFF7F8C8D), Offset(cx - radius * 1.1f, cy + radius * 0.4f), Offset(cx + radius * 1.1f, cy + radius * 0.4f), strokeWidth = 5f)

                    // Shiny glaze rich dark brown sphere Jamun
                    drawCircle(
                        color = Color(0xFF4A1502),
                        radius = radius * 0.8f,
                        center = Offset(cx, cy - radius * 0.15f)
                    )
                    // Rich highlight zone
                    drawCircle(
                        color = Color(0xFF800000),
                        radius = radius * 0.6f,
                        center = Offset(cx + 2f, cy - radius * 0.2f)
                    )
                    // Sticky sugar syrup gloss
                    drawOval(
                        color = Color.White.copy(alpha = 0.8f),
                        topLeft = Offset(cx - radius * 0.45f, cy - radius * 0.5f),
                        size = Size(width * 0.24f, height * 0.12f)
                    )
                    // Silver leaf (Vark) sparkle speck
                    val vark = Path().apply {
                        moveTo(cx + radius * 0.3f, cy - radius * 0.1f)
                        lineTo(cx + radius * 0.45f, cy - radius * 0.2f)
                        lineTo(cx + radius * 0.4f, cy - radius * 0.05f)
                        lineTo(cx + radius * 0.25f, cy)
                        close()
                    }
                    drawPath(vark, Color.White)
                }

                FoodType.PANI_PURI -> {
                    // Crunchy golden purf shell
                    drawCircle(
                        color = Color(0xFFD39E45),
                        radius = radius * 0.92f,
                        center = Offset(cx, cy)
                    )
                    
                    // Shadow crispy under-base
                    drawCircle(
                        color = Color(0xFF935116),
                        radius = radius * 0.82f,
                        center = Offset(cx + 2f, cy + 3f),
                        style = Stroke(width = width * 0.08f)
                    )

                    // Crack hole opening at the top
                    drawCircle(
                        color = Color(0xFF5E3F1F),
                        radius = radius * 0.46f,
                        center = Offset(cx, cy - radius * 0.08f)
                    )

                    // Spiced herbal coriander mint green water puddle inside
                    drawCircle(
                        color = Color(0xFF229954),
                        radius = radius * 0.38f,
                        center = Offset(cx - 2f, cy - radius * 0.05f)
                    )

                    // Sizzling yellow chickpea (Boondi) floating
                    drawCircle(Color(0xFFF4D03F), radius = width * 0.06f, center = Offset(cx - 3f, cy - radius * 0.1f))
                    drawCircle(Color(0xFFF39C12), radius = width * 0.05f, center = Offset(cx + 8f, cy + 3f))

                    // Mint splashes spraying outside
                    drawCircle(Color(0xFF4CDF50).copy(alpha = 0.8f), radius = width * 0.04f, center = Offset(cx - radius * 0.9f, cy - radius * 0.5f))
                    drawCircle(Color(0xFF4CDF50).copy(alpha = 0.8f), radius = width * 0.03f, center = Offset(cx + radius * 0.95f, cy + radius * 0.4f))
                }

                FoodType.BIRYANI -> {
                    // Red terracotta handi clay pot base
                    val potPath = Path().apply {
                        moveTo(cx - radius * 0.8f, cy - radius * 0.1f)
                        cubicTo(
                            cx - radius * 0.95f, cy + radius * 0.9f,
                            cx + radius * 0.95f, cy + radius * 0.9f,
                            cx + radius * 0.8f, cy - radius * 0.1f
                        )
                        lineTo(cx + radius * 0.65f, cy - radius * 0.3f)
                        lineTo(cx - radius * 0.65f, cy - radius * 0.3f)
                        close()
                    }
                    drawPath(potPath, Color(0xFFBA4A00)) // Terracotta red

                    // Wide collar brim neck of claypot
                    drawRoundRect(
                        color = Color(0xFF935116),
                        topLeft = Offset(cx - radius * 0.72f, cy - radius * 0.42f),
                        size = Size(radius * 1.44f, height * 0.15f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )

                    // Delicious heap of saffron rice spilling from mouth
                    val ricePath = Path().apply {
                        moveTo(cx - radius * 0.55f, cy - radius * 0.42f)
                        cubicTo(
                            cx - radius * 0.35f, cy - radius * 0.95f,
                            cx + radius * 0.35f, cy - radius * 0.95f,
                            cx + radius * 0.55f, cy - radius * 0.42f
                        )
                        close()
                    }
                    drawPath(ricePath, Color(0xFFF4D03F)) // Rich saffron yellow yellow

                    // Green parsley coriander leaf garnish on top
                    drawCircle(
                        color = Color(0xFF27AE60),
                        radius = width * 0.08f,
                        center = Offset(cx, cy - radius * 0.65f)
                    )

                    // Miniature spicy whole red chilli on rice
                    drawLine(
                        color = Color(0xFFC0392B),
                        start = Offset(cx - 8f, cy - radius * 0.52f),
                        end = Offset(cx + 12f, cy - radius * 0.58f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

// 1. DASHBOARD / MENU SCREEN
@Composable
fun MenuScreen(
    userStats: UserStats?,
    onNavigate: (AppScreen) -> Unit,
    onClaimDaily: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AmbientSkyDark, Color(0xFF351754), AmbientSkyDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Lanterns hanging around header
        HangingFestivalLanterns()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Elegant title header
            Text(
                text = "STREET FOOD",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentMarigold,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "SMASH",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = HoliPink,
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtitle
            Card(
                colors = CardDefaults.cardColors(containerColor = HoliPink.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "🇮🇳 INDIAN FOOD MELA FESTIVAL 🇮🇳",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SoftAmberText,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Interactive visual centerpiece - spinning Jalebi / Samosa bomb
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                FoodItemGraphic(
                    foodType = FoodType.JALEBI,
                    specialType = SpecialType.BIRYANI_FEAST,
                    isSelected = true,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Menu Buttons
            Button(
                onClick = { onNavigate(AppScreen.LEVEL_SELECTOR) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySaffron),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(56.dp)
                    .testTag("play_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ENTER FESTIVAL MELA", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(AppScreen.ACHIEVEMENTS) },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievements", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BADGES", style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                }

                Button(
                    onClick = { onNavigate(AppScreen.STORE) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMarigold),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Filled.ShoppingBag, contentDescription = "Bazaar Store", tint = Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BAZAAR", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Sweets Box Rewards button
            Button(
                onClick = { onNavigate(AppScreen.DAILY_REWARDS) },
                colors = ButtonDefaults.buttonColors(containerColor = HoliPink),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.CardGiftcard, contentDescription = "Daily Rewards", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DAILY MITHE SWEETS", style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp))
            }

            // Stats Footer Bar
            Spacer(modifier = Modifier.height(40.dp))
            userStats?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(IndianNightCard, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = "Coins", tint = AccentMarigold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🪙 ${it.coins} Coins", color = SoftAmberText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Filled.ColorLens, contentDescription = "Active Theme", tint = PrimarySaffron, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(it.activeTheme, color = SoftAmberText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// 2. STAGE/LEVEL SELECTOR SCREEN
@Composable
fun LevelSelectorScreen(
    levels: List<LevelScore>,
    userStats: UserStats?,
    levelManifest: List<LevelDetails>,
    onBack: () -> Unit,
    onLevelSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Food Stalls Selector", fontWeight = FontWeight.Bold, color = AccentMarigold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentMarigold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmbientSkyDark)
            )
        },
        containerColor = AmbientSkyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AmbientSkyDark, Color(0xFF2C1545))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Saffron festival information card
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(HoliPink, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Storefront, contentDescription = "Mela", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Your Mela Progression", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Master all 10 delicious food stalls to reach Level 10!", color = SoftAmberText, fontSize = 12.sp)
                        }
                    }
                }

                // Grid of Level Stalls
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(levelManifest.size) { index ->
                        val detail = levelManifest[index]
                        val scoreRecord = levels.find { it.levelId == detail.id }
                        val isUnlocked = scoreRecord?.isUnlocked == true || detail.id == 1 || (userStats != null && detail.id <= userStats.levelReached)

                        LevelStallCard(
                            detail = detail,
                            isUnlocked = isUnlocked,
                            record = scoreRecord,
                            onClick = { if (isUnlocked) onLevelSelected(detail.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelStallCard(
    detail: LevelDetails,
    isUnlocked: Boolean,
    record: LevelScore?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) IndianNightCard else Color(0xFF1E1026).copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = if (isUnlocked) 2.dp else 1.dp,
            color = if (isUnlocked) PrimarySaffron.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Food Stall Avatar Icon representer
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        if (isUnlocked) PrimarySaffron.copy(alpha = 0.15f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    val favFood = detail.allowedFoods.last()
                    Box(modifier = Modifier.size(44.dp)) {
                        FoodItemGraphic(foodType = favFood, specialType = SpecialType.NONE, isSelected = false)
                    }
                } else {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "Locked Wood Stall",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stall stall info
            Text(
                text = "STALL ${detail.id}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isUnlocked) HoliPink else Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = detail.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Star score display
            if (isUnlocked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val starCount = record?.stars ?: 0
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= starCount) AccentMarigold else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "Goal: ${detail.targetScore}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftAmberText,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "LOCKED",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

// 3. ACHIEVEMENTS SYSTEM SCREEN
@Composable
fun AchievementsScreen(
    achievements: List<GameAchievement>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Festival Badges", fontWeight = FontWeight.Bold, color = AccentMarigold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentMarigold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmbientSkyDark)
            )
        },
        containerColor = AmbientSkyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AmbientSkyDark, Color(0xFF28133E))
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { badge ->
                    BadgeRow(badge = badge)
                }
            }
        }
    }
}

@Composable
fun BadgeRow(badge: GameAchievement) {
    val progressPercent = (badge.progress.toFloat() / badge.target.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) IndianNightCard else Color(0xFF1F102F).copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (badge.isUnlocked) AccentMarigold.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Circular Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        if (badge.isUnlocked) AccentMarigold.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (badge.isUnlocked) Icons.Filled.MilitaryTech else Icons.Filled.LockOpen,
                    contentDescription = "Badge Icon",
                    tint = if (badge.isUnlocked) AccentMarigold else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.title,
                    color = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = badge.description,
                    color = if (badge.isUnlocked) SoftAmberText else Color.White.copy(alpha = 0.35f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Progress line
                LinearProgressIndicator(
                    progress = { progressPercent },
                    color = if (badge.isUnlocked) SecondaryGreen else PrimarySaffron,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${badge.progress} / ${badge.target}",
                        color = if (badge.isUnlocked) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 4. BAZAAR STORE / COSMETICS SCREEN
@Composable
fun StoreScreen(
    userStats: UserStats?,
    onBack: () -> Unit,
    onPurchaseTheme: (String, Int) -> Unit,
    onActivateTheme: (String) -> Unit,
    onTogglePremium: () -> Unit
) {
    val themePackList = listOf(
        ThemePack("Classic", "Golden lamps, traditional lanterns, Saffron ambient sky.", 0),
        ThemePack("Diwali Lights", "Clay diya lamps, electric twinkling violet lights.", 150),
        ThemePack("Monsoon Street", "Raindrops backdrop, steaming chai stalls, teal theme.", 250),
        ThemePack("Holi Festival", "Spattered colored powders, dynamic magenta splashes.", 350)
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Festival Bazaar Store", fontWeight = FontWeight.Bold, color = AccentMarigold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentMarigold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmbientSkyDark)
            )
        },
        containerColor = AmbientSkyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AmbientSkyDark, Color(0xFF33144C))
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header budget coins bar
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = "Sikka Coins", tint = AccentMarigold, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("YOUR SIKKAS", color = SoftAmberText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                            Text(
                                "🪙 ${userStats?.coins ?: 0} Sikka Coins",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                // Premium Ads-Free section
                item {
                    Text("Support Us", color = AccentMarigold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                        border = BorderStroke(1.dp, HoliPink.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Premium Mela Feast", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(
                                        "Ad-free gameplay, automatic free restarts with +5 extra moves on failing, and golden VIP interface label.",
                                        color = SoftAmberText.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Switch(
                                    checked = userStats?.adFree == true,
                                    onCheckedChange = { onTogglePremium() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HoliPink,
                                        checkedTrackColor = HoliPink.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Cosmetics section
                item {
                    Text("Cosmetic Festival Themes", color = AccentMarigold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                items(themePackList) { pack ->
                    val isUnlocked = userStats?.unlockedThemes?.split(",")?.contains(pack.name) == true || pack.price == 0
                    val isActive = userStats?.activeTheme == pack.name

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                        border = BorderStroke(
                            width = if (isActive) 2.dp else 1.dp,
                            color = if (isActive) PrimarySaffron else Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(pack.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (isActive) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = SecondaryGreen),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "ACTIVE",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    pack.description,
                                    color = SoftAmberText,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            if (isUnlocked) {
                                if (!isActive) {
                                    Button(
                                        onClick = { onActivateTheme(pack.name) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimarySaffron),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("APPLY", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onPurchaseTheme(pack.name, pack.price) },
                                    colors = ButtonDefaults.buttonColors(containerColor = HoliPink),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.MonetizationOn, contentDescription = "Cost", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${pack.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ThemePack(
    val name: String,
    val description: String,
    val price: Int
)

// 5. DAILY REWARDS SWEETS SCREEN
@Composable
fun DailyRewardsScreen(
    userStats: UserStats?,
    message: String?,
    onBack: () -> Unit,
    onClaim: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Daily Mithai Sweets Box", fontWeight = FontWeight.Bold, color = AccentMarigold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentMarigold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AmbientSkyDark)
            )
        },
        containerColor = AmbientSkyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AmbientSkyDark, Color(0xFF26123D))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large sweet boxes drawing centerpiece
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, AccentMarigold),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "KAISE HO! NAMASTE!",
                            color = PrimarySaffron,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Halwai's Magic Box",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Rich sweet graphic
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .background(HoliPink.copy(alpha = 0.1f), CircleShape)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FoodItemGraphic(
                                foodType = FoodType.GULAB_JAMUN,
                                specialType = SpecialType.NONE,
                                isSelected = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Open the traditional sweet box daily to claim random Sikka Coins! (50 to 150 coins)",
                            color = SoftAmberText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Claim status msg
                if (message != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SecondaryGreen.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = message,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }

                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentMarigold),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = "Claim Sweets Box", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OPEN SWEET BOX", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }
        }
    }
}

// 6. DECORATIVE ELEMENTS: BLINKING LANTERNS AND LIGHTS
@Composable
fun HangingFestivalLanterns() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left hanging chain
        FestivalLanternVector(alignmentSign = -1)
        // Center decorations
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                BlinkingFestivalLight()
            }
        }
        // Right hanging chain
        FestivalLanternVector(alignmentSign = 1)
    }
}

@Composable
fun FestivalLanternVector(alignmentSign: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "lantern_sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -8f * alignmentSign,
        targetValue = 8f * alignmentSign,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Canvas(
        modifier = Modifier
            .size(36.dp, 80.dp)
            .rotate(swayAngle)
    ) {
        val w = size.width
        val h = size.height
        
        // Wire line
        drawLine(
            color = Color(0xFFFFD700).copy(alpha = 0.5f),
            start = Offset(w/2, 0f),
            end = Offset(w/2, h * 0.4f),
            strokeWidth = 3f
        )
        
        // Wooden lantern frame (Terracotta-style)
        val rectTop = h * 0.4f
        val rectYHeight = h * 0.35f
        drawRoundRect(
            color = Color(0xFFC0392B),
            topLeft = Offset(w*0.1f, rectTop),
            size = Size(w*0.8f, rectYHeight),
            cornerRadius = CornerRadius(5f, 5f)
        )
        
        // Golden inner lamp bulb core
        drawCircle(
            color = Color(0xFFFFCC00),
            radius = w * 0.22f,
            center = Offset(w/2, rectTop + rectYHeight/2)
        )
        
        // Falling decorative thread strings at the bottom (Marigold-style)
        val threadTop = rectTop + rectYHeight
        for (i in 0..2) {
            val tx = (w * 0.2f) + (i * w * 0.3f)
            drawCircle(Color(0xFFFF9933), radius = 5f, center = Offset(tx, threadTop + 5f))
            drawCircle(Color(0xFF138808), radius = 4f, center = Offset(tx, threadTop + 13f))
            drawCircle(Color(0xFFFFCC00), radius = 3f, center = Offset(tx, threadTop + 19f))
        }
    }
}

@Composable
fun BlinkingFestivalLight() {
    val infiniteTransition = rememberInfiniteTransition(label = "light_blink")
    val alphaColor by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000 + kotlin.random.Random.nextInt(500), easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )
    val color = remember { listOf(HoliPink, AccentMarigold, PrimarySaffron, Color(0xFF33FFA6))[kotlin.random.Random.nextInt(4)] }

    Canvas(modifier = Modifier.size(10.dp, 16.dp)) {
        val w = size.width
        val h = size.height
        // Wire connector
        drawLine(Color.DarkGray, Offset(w/2, 0f), Offset(w/2, h * 0.3f), strokeWidth = 3f)
        // Bulb
        drawOval(
            color = color.copy(alpha = alphaColor),
            topLeft = Offset(0f, h * 0.3f),
            size = Size(w, h * 0.7f)
        )
        // Glow corona wrapper
        if (alphaColor > 0.6f) {
            drawCircle(
                color = color.copy(alpha = 0.25f),
                radius = w * 1.5f,
                center = Offset(w/2, h * 0.65f)
            )
        }
    }
}

// 7. ACTIVE MATCH-3 GAME BOARD PLAY SCREEN
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    userStats: UserStats?,
    onNavigate: (AppScreen) -> Unit
) {
    val activeLevel by viewModel.activeLevel.collectAsState()
    val boardState by viewModel.board.collectAsState()
    val scoreState by viewModel.score.collectAsState()
    val movesState by viewModel.movesLeft.collectAsState()
    val timeState by viewModel.timeLeft.collectAsState()
    val comboMultiplier by viewModel.comboMultiplier.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    
    val showWinDialog by viewModel.showWinDialog.collectAsState()
    val showFailDialog by viewModel.showFailDialog.collectAsState()
    val splashEffect by viewModel.splashEffect.collectAsState()
    val adProgressSeconds by viewModel.adProgressSeconds.collectAsState()

    var showTutorial by remember { mutableStateOf(false) }

    activeLevel?.let { level ->
        val progressPercent = (scoreState.toFloat() / level.targetScore.toFloat()).coerceAtMost(1.0f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = when (userStats?.activeTheme) {
                            "Diwali Lights" -> listOf(Color(0xFF1B052B), Color(0xFF3D0960), Color(0xFF1B052B))
                            "Monsoon Street" -> listOf(Color(0xFF0D1E2D), Color(0xFF1E3A52), Color(0xFF0D1E2D))
                            "Holi Festival" -> listOf(Color(0xFF330921), Color(0xFF6F124A), Color(0xFF330921))
                            else -> listOf(AmbientSkyDark, Color(0xFF2C1445), AmbientSkyDark)
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Blinking lights row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(8) { BlinkingFestivalLight(); Spacer(modifier = Modifier.width(6.dp)) }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top controls banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.LEVEL_SELECTOR) },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Exit to Menu", tint = AccentMarigold)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = level.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Storefront, contentDescription = "Stall", tint = PrimarySaffron, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Stall ${level.id} of 10",
                                style = MaterialTheme.typography.labelMedium.copy(color = SoftAmberText)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showTutorial = true },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = "How To Play", tint = AccentMarigold)
                    }
                }

                // Targets statistics HUD banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Level resources left
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (level.limitType == LevelLimitType.MOVES) "MOVES" else "TIME LEFT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = HoliPink,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (level.limitType == LevelLimitType.MOVES) "$movesState" else "${timeState}s",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }

                        // Target scores
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "STALL GOAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SecondaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${level.targetScore}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = AccentMarigold
                                )
                            )
                        }

                        // Current score details
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MY SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = PrimarySaffron,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$scoreState",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                // Progress Bar with Star Markers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Saffron tracking bar
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            color = PrimarySaffron,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape)
                        )
                        
                        // Render star notches along the bar: Star 1 at 50%, Star 2 at 75%, Star 3 at 100%
                        val starsCount = when {
                            scoreState >= level.targetScore * 2.0f -> 3
                            scoreState >= level.targetScore * 1.4f -> 2
                            scoreState >= level.targetScore -> 1
                            else -> 0
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Empty box spacer
                            Spacer(modifier = Modifier.weight(0.48f))
                            // Star 1 outline
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star 1",
                                tint = if (starsCount >= 1) AccentMarigold else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.weight(0.24f))
                            // Star 2 outline
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star 2",
                                tint = if (starsCount >= 2) AccentMarigold else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.weight(0.24f))
                            // Star 3 outline
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = "Star 3",
                                tint = if (starsCount >= 3) AccentMarigold else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Active Match-3 8x8 Board Container Frame
                boardState?.let { grid ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.0f)
                            .background(Color(0xFF1E102F), RoundedCornerShape(16.dp))
                            .border(2.dp, PrimarySaffron.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing Grid Tiles
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (r in 0 until 8) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (c in 0 until 8) {
                                        val tile = grid[r][c]
                                        val isSelected = selectedTile == Pair(r, c)
                                        
                                        // Animate drop falls nicely
                                        val currentYOffsetState = animateFloatAsState(
                                            targetValue = tile.currentYOffset,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            label = "fall"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1.0f)
                                                .offset { IntOffset(0, currentYOffsetState.value.roundToInt()) }
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) Color.White.copy(alpha = 0.15f)
                                                    else if ((r + c) % 2 == 0) Color(0xFF2C1945).copy(alpha = 0.4f)
                                                    else Color(0xFF180A2D).copy(alpha = 0.4f)
                                                )
                                                .clickable(enabled = !isProcessing) {
                                                    viewModel.onTileSelected(r, c)
                                                }
                                                .padding(3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Render graphic if normal or exploding
                                            if (!tile.isMatched || tile.isExploding) {
                                                FoodItemGraphic(
                                                    foodType = tile.foodType,
                                                    specialType = tile.specialType,
                                                    isSelected = isSelected
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Combo Overlays splash effects
                        splashEffect?.let { effect ->
                            Box(
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                                    .border(2.dp, HoliPink, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                                    .scale(1.1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = effect.message,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = AccentMarigold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Text(
                                        text = "+${effect.scoreGained} points!",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    if (comboMultiplier > 1) {
                                        Text(
                                            text = "COMBO ${comboMultiplier}x ⚡",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HoliPink
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Ad loading state counter
                        adProgressSeconds?.let { remain ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = HoliPink)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Sponsoring Mela Vendor Ad...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Rewarding Moves in $remain seconds",
                                        color = SoftAmberText,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer guidelines
                Text(
                    text = "Tap any item, then tap an adjacent checker to swap!",
                    color = SoftAmberText.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            // 1. TUTORIAL HOW TO PLAY OVERLAY
            if (showTutorial) {
                AlertDialog(
                    onDismissRequest = { showTutorial = false },
                    title = { Text("How to Smash Food!", fontWeight = FontWeight.Bold, color = AccentMarigold) },
                    text = {
                        Column {
                            Text("Swap colorful Indian foods to match 3 or more of the same type sequentially to earn points and trigger cascades!", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recipe Special Power-ups:", fontWeight = FontWeight.Bold, color = PrimarySaffron, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🌶️ Spicy Samosa Bomb: clears surrounding 3x3 tiles. Created by making L/T matches.", fontSize = 12.sp)
                            Text("🌀 Jalebi Swirl Row: clears a full horizontal row. Created by matching 4 horizontally.", fontSize = 12.sp)
                            Text("💦 Pani Puri Column Special: clears a full vertical column. Created by matching 4 vertically.", fontSize = 12.sp)
                            Text("🏺 Biryani Feast: Swapped with any food to clear all instances of that food. Created by matching 5 in a row.", fontSize = 12.sp)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showTutorial = false }) {
                            Text("SUBJH GAYA (UNDERSTOOD)", color = PrimarySaffron)
                        }
                    },
                    containerColor = IndianNightCard
                )
            }

            // 2. LEVEL WON CONGRATULATIONS DIALOG CARD
            if (showWinDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                        border = BorderStroke(2.dp, AccentMarigold)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "STALL COMPLETE!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = AccentMarigold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Namaste, wonderful job!",
                                color = SoftAmberText,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Calculate Victory Stars
                            val starCount = when {
                                scoreState >= level.targetScore * 2.0f -> 3
                                scoreState >= level.targetScore * 1.4f -> 2
                                else -> 1
                            }

                            // Stars animations
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                for (i in 1..3) {
                                    val isLit = i <= starCount
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "Victory Star",
                                        tint = if (isLit) AccentMarigold else Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(50.dp)
                                    )
                                }
                            }

                            // Score and money
                            Text(
                                text = "Final Score: $scoreState",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "Mela Vendor Coins Received: 🪙 +${starCount * 35}",
                                color = SecondaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(AppScreen.LEVEL_SELECTOR) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("STALLS")
                                }

                                Button(
                                    onClick = {
                                        val nextId = level.id + 1
                                        if (nextId <= 10) {
                                            viewModel.startLevel(nextId)
                                        } else {
                                            viewModel.navigateTo(AppScreen.LEVEL_SELECTOR)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySaffron),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = if (level.id < 10) "NEXT STALL" else "MELA DONE",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. LEVEL FAILED / OUT OF RESOURCES SHOP DIALOG
            if (showFailDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = IndianNightCard),
                        border = BorderStroke(1.dp, HoliPink)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Out of Resources!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = HoliPink
                                )
                            )
                            Text(
                                text = "You haven't completed the Stall target score yet.",
                                color = SoftAmberText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            // Status stats
                            Text(
                                text = "Target: ${level.targetScore} | Progress: $scoreState",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Refueling Options
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Option A: Buy moves for coins
                                val coinsAvailable = userStats?.coins ?: 0
                                val canAfford = coinsAvailable >= 50
                                Button(
                                    onClick = { if (canAfford) viewModel.purchaseExtraMoves() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentMarigold),
                                    enabled = canAfford,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🪙 Spend 50 Sikka (+5 Moves/30s)",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Option B: Watch Sponsors Ad
                                Button(
                                    onClick = { viewModel.watchAdForExtraMoves() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📺 Watch sponsored Ad (+5 Moves/30s)",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Option C: Restart stall
                                OutlinedButton(
                                    onClick = { viewModel.startLevel(level.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Restart Food Stall")
                                }

                                // Option D: Leave
                                TextButton(
                                    onClick = { viewModel.navigateTo(AppScreen.LEVEL_SELECTOR) }
                                ) {
                                    Text("Exit To Festival Map", color = Color.White.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

