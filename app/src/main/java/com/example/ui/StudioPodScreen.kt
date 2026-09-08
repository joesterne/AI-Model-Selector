package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import com.example.data.ProgramEntity
import kotlin.math.atan2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.launch

// Retro LCD Monochrome Palette
private val lcdBg = Color(0xFF9EA792)
private val lcdFg = Color(0xFF1B1E16)
private val lcdStatusBar = Color(0xFF8B9481)
private val lcdDivider = Color(0xFF7A8471)

enum class PodScreenState {
    LIST, DETAILS
}

@Composable
fun rememberBatteryLevel(): Float {
    val context = LocalContext.current
    var batteryLevel by remember { mutableFloatStateOf(1f) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    batteryLevel = level.toFloat() / scale.toFloat()
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val currentIntent = context.registerReceiver(receiver, filter)
        if (currentIntent != null) {
            val level = currentIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = currentIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level != -1 && scale != -1) {
                batteryLevel = level.toFloat() / scale.toFloat()
            }
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return batteryLevel
}

@Composable
fun RetroBatteryIcon(level: Float, modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.size(width = 18.dp, height = 9.dp)) {
        val strokeWidth = 1.dp.toPx()
        val nubWidth = 2.dp.toPx()
        val bodyWidth = size.width - nubWidth
        val bodyHeight = size.height

        // Draw battery body outline
        drawRect(
            color = color,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = androidx.compose.ui.geometry.Size(bodyWidth - strokeWidth, bodyHeight - strokeWidth),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )

        // Draw battery nub
        drawRect(
            color = color,
            topLeft = Offset(bodyWidth, bodyHeight * 0.25f),
            size = androidx.compose.ui.geometry.Size(nubWidth, bodyHeight * 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        // Draw battery level fill
        val fillPadding = 2.dp.toPx()
        val maxFillWidth = bodyWidth - (fillPadding * 2)
        val fillWidth = maxFillWidth * level
        if (fillWidth > 0) {
            drawRect(
                color = color,
                topLeft = Offset(fillPadding, fillPadding),
                size = androidx.compose.ui.geometry.Size(fillWidth, bodyHeight - (fillPadding * 2)),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
        }
    }
}

@Composable
fun StudioPodScreen(
    programs: List<ProgramEntity>,
    onAddClick: () -> Unit
) {
    var screenState by remember { mutableStateOf(PodScreenState.LIST) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var selectedProgram by remember { mutableStateOf<ProgramEntity?>(null) }
    val listState = rememberLazyListState()
    val detailScrollState = rememberScrollState()
    val batteryLevel = rememberBatteryLevel()
    val coroutineScope = rememberCoroutineScope()

    // Ensure index is within bounds if list shrinks
    LaunchedEffect(programs) {
        if (programs.isNotEmpty() && selectedIndex >= programs.size) {
            selectedIndex = programs.size - 1
        }
    }

    // Scroll to the selected item smoothly
    LaunchedEffect(selectedIndex) {
        if (programs.isNotEmpty()) {
            listState.animateScrollToItem(maxOf(0, selectedIndex - 2))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        // The iPod Body
        Column(
            modifier = Modifier
                .width(320.dp)
                .height(520.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0F0F0), Color(0xFFD0D0D0))
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(4.dp))
                        .background(lcdBg)
                ) {
                    AnimatedContent(
                        targetState = screenState,
                        transitionSpec = {
                            if (targetState == PodScreenState.DETAILS) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut())
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut())
                            }
                        },
                        label = "screen_transition"
                    ) { state ->
                        when (state) {
                            PodScreenState.LIST -> PodListScreen(
                                programs = programs,
                                selectedIndex = selectedIndex,
                                listState = listState,
                                batteryLevel = batteryLevel
                            )
                            PodScreenState.DETAILS -> PodDetailScreen(
                                program = selectedProgram,
                                batteryLevel = batteryLevel,
                                scrollState = detailScrollState
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Click Wheel area
            ClickWheel(
                onScroll = { direction ->
                    if (screenState == PodScreenState.LIST && programs.isNotEmpty()) {
                        if (direction > 0) {
                            selectedIndex = minOf(selectedIndex + 1, programs.size - 1)
                        } else {
                            selectedIndex = maxOf(selectedIndex - 1, 0)
                        }
                    } else if (screenState == PodScreenState.DETAILS) {
                        coroutineScope.launch {
                            detailScrollState.animateScrollBy(direction * 150f)
                        }
                    }
                },
                onMenuClick = {
                    if (screenState == PodScreenState.DETAILS) {
                        screenState = PodScreenState.LIST
                    }
                },
                onCenterClick = {
                    if (screenState == PodScreenState.LIST && programs.isNotEmpty()) {
                        selectedProgram = programs[selectedIndex]
                        screenState = PodScreenState.DETAILS
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
        }

        // FAB to add new program - kept outside the iPod body for functional convenience
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Program")
        }
    }
}

@Composable
fun PodListScreen(programs: List<ProgramEntity>, selectedIndex: Int, listState: androidx.compose.foundation.lazy.LazyListState, batteryLevel: Float) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.SolidColor(lcdStatusBar))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "StudioPod",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = lcdFg
            )
            RetroBatteryIcon(
                level = batteryLevel,
                color = lcdFg
            )
        }
        
        Divider(color = lcdDivider, thickness = 1.dp)

        if (programs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No programs.\nAdd one below.",
                    textAlign = TextAlign.Center,
                    color = lcdFg,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(programs) { index, program ->
                    val isSelected = (index == selectedIndex)
                    
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) androidx.compose.ui.graphics.SolidColor(lcdFg)
                                    else androidx.compose.ui.graphics.SolidColor(Color.Transparent)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = program.title,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) lcdBg else lcdFg,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isSelected) {
                                Text(
                                    text = ">",
                                    fontSize = 12.sp,
                                    color = lcdFg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!isSelected) {
                            Divider(color = lcdDivider, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodDetailScreen(program: ProgramEntity?, batteryLevel: Float, scrollState: androidx.compose.foundation.ScrollState) {
    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.SolidColor(lcdStatusBar))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Now Playing",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = lcdFg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RetroBatteryIcon(
                level = batteryLevel,
                color = lcdFg
            )
        }
        
        Divider(color = lcdDivider, thickness = 1.dp)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = program?.title ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = lcdFg
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = program?.let { dateFormatter.format(java.util.Date(it.dateCreated)) } ?: "",
                fontSize = 10.sp,
                color = lcdFg.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = program?.description ?: "",
                fontSize = 14.sp,
                color = lcdFg.copy(alpha = 0.8f)
            )
        }
    }
}

fun Modifier.circularScroll(
    onScroll: (direction: Int) -> Unit,
    angleThreshold: Float = 20f
): Modifier = composed {
    var accumulatedAngle by remember { mutableFloatStateOf(0f) }
    var lastAngle by remember { mutableFloatStateOf(-1f) }
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    
    this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { offset ->
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                lastAngle = Math.toDegrees(atan2(offset.y - centerY, offset.x - centerX).toDouble()).toFloat()
                accumulatedAngle = 0f
            },
            onDrag = { change, _ ->
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val currentAngle = Math.toDegrees(atan2(change.position.y - centerY, change.position.x - centerX).toDouble()).toFloat()
                
                if (lastAngle != -1f) {
                    var diff = currentAngle - lastAngle
                    if (diff < -180f) diff += 360f
                    if (diff > 180f) diff -= 360f
                    
                    accumulatedAngle += diff
                    
                    if (accumulatedAngle > angleThreshold) {
                        onScroll(1)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        accumulatedAngle -= angleThreshold
                    } else if (accumulatedAngle < -angleThreshold) {
                        onScroll(-1)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        accumulatedAngle += angleThreshold
                    }
                }
                lastAngle = currentAngle
            },
            onDragEnd = {
                lastAngle = -1f
                accumulatedAngle = 0f
            }
        )
    }
}

@Composable
fun ClickWheel(
    onScroll: (direction: Int) -> Unit,
    onMenuClick: () -> Unit,
    onCenterClick: () -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .size(200.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFFFAFAFA))
            .circularScroll(onScroll = onScroll),
        contentAlignment = Alignment.Center
    ) {
        // Labels
        Text("MENU", modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 16.dp)
            .clickable { 
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onMenuClick() 
            },
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
        Text("I<<", modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 16.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
        Text(">>I", modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 16.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
        Text(">II", modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 16.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            
        // Center Button
        Box(
            modifier = Modifier
                .size(70.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFEBEBEB), Color(0xFFDCDCDC))
                    )
                )
                .clickable { 
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onCenterClick() 
                }
        )
    }
}
