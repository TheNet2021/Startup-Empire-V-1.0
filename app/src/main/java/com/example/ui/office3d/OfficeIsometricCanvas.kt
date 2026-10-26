package com.example.ui.office3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.*
import kotlin.math.roundToInt

enum class TimeOfDayLighting {
    AUTO_CLOCK,
    FORCE_DAY,
    FORCE_SUNSET,
    FORCE_NIGHT
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun OfficeIsometricCanvas(
    state: GameState,
    onSelectFurniture: (PlacedFurniture) -> Unit,
    onSelectEmployee: (Employee) -> Unit,
    onOpenServerDetails: () -> Unit,
    onMoveFurniture: (furnitureId: String, newX: Int, newY: Int) -> Unit = { _, _, _ -> },
    onRotateFurniture: (furnitureId: String) -> Unit = {},
    onSellFurniture: (furnitureId: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // -------------------------------------------------------------
    // CAMERA CONTROLS: PERMANENTLY FIXED TARGET & POSITION
    // ONLY pinch-to-zoom and zoom buttons are allowed.
    // Panning, dragging, orbiting, rotation, and shifting are strictly disabled.
    // -------------------------------------------------------------
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    val minZoom = 0.80f
    val maxZoom = 1.50f

    // Selected furniture for interactive move / rotate / sell
    var selectedFurnitureId by remember { mutableStateOf<String?>(null) }
    var isMoveMode by remember { mutableStateOf(false) }

    // Lighting mode (Auto synced with GameClock, or user forced Day/Sunset/Night)
    var lightingMode by remember { mutableStateOf(TimeOfDayLighting.AUTO_CLOCK) }

    // Load and cache all pre-rendered 3D isometric office sprites
    val sprites = rememberIsometricSpriteAtlas()

    // Smooth continuous animation cycle for LED blinks, screen refreshes, typing motion
    val infiniteTransition = rememberInfiniteTransition(label = "office_gameplay_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animTime"
    )

    // Determine current effective time of day
    val isNightEffective = when (lightingMode) {
        TimeOfDayLighting.FORCE_NIGHT -> true
        TimeOfDayLighting.FORCE_DAY -> false
        TimeOfDayLighting.FORCE_SUNSET -> false
        TimeOfDayLighting.AUTO_CLOCK -> state.clock.hour < 6 || state.clock.hour >= 20
    }
    val isSunsetEffective = lightingMode == TimeOfDayLighting.FORCE_SUNSET ||
        (lightingMode == TimeOfDayLighting.AUTO_CLOCK && (state.clock.hour in 18..19 || state.clock.hour in 5..6))

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            // Gesture listener: STRICTLY intercepts ONLY zoom.
            // Pan and rotation offsets are completely discarded to guarantee camera never shifts.
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    zoomLevel = (zoomLevel * zoom).coerceIn(minZoom, maxZoom)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.furniture, zoomLevel, isMoveMode, selectedFurnitureId, state.officeTier) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()

                        val gridW = state.officeTier.gridWidth
                        val gridH = state.officeTier.gridHeight
                        val gridSpan = (gridW + gridH).toFloat()

                        // Base scale tailored so office interior fills portrait screen beautifully
                        val baseScaleFactor = when (state.officeTier) {
                            OfficeTier.GARAGE -> 1.16f
                            OfficeTier.COWORKING -> 1.08f
                            OfficeTier.TECH_LOFT -> 0.98f
                            OfficeTier.CAMPUS_HQ -> 0.90f
                            OfficeTier.CYBER_TOWER -> 0.82f
                        }
                        val rawBaseTileW = ((canvasW * 0.90f) / (gridSpan / 2f)).coerceIn(130f, 260f)
                        val effectiveScale = baseScaleFactor * zoomLevel
                        val scaledTileW = (rawBaseTileW * effectiveScale).roundToInt()
                        val tileW = (if (scaledTileW % 4 != 0) scaledTileW + (4 - scaledTileW % 4) else scaledTileW).toFloat()
                        val tileH = tileW / 2f

                        val wallHeight = (220f * effectiveScale).roundToInt().toFloat()
                        val floorDepth = (gridW + gridH) * (tileH / 2f)

                        // Camera is permanently centered: zero pan offset, fixed geometric center
                        val originX = (canvasW / 2f).roundToInt().toFloat()
                        val defaultOriginY = (canvasH * 0.46f) - ((floorDepth - wallHeight) / 2f)
                        val originY = defaultOriginY.roundToInt().toFloat()

                        // Tap coordinates to isometric grid
                        val dx = tapOffset.x - originX
                        val dy = tapOffset.y - originY

                        val gxFloat = (dx / (tileW / 2f) + dy / (tileH / 2f)) / 2f
                        val gyFloat = (dy / (tileH / 2f) - dx / (tileW / 2f)) / 2f

                        val tappedGx = kotlin.math.floor(gxFloat).toInt()
                        val tappedGy = kotlin.math.floor(gyFloat).toInt()

                        if (isMoveMode && selectedFurnitureId != null) {
                            if (tappedGx in 0 until gridW && tappedGy in 0 until gridH) {
                                onMoveFurniture(selectedFurnitureId!!, tappedGx, tappedGy)
                                isMoveMode = false
                                return@detectTapGestures
                            }
                        }

                        // Check if tapped near an existing furniture or employee
                        var hitFurniture: PlacedFurniture? = null
                        for (item in state.furniture) {
                            val sx = (item.gridX - item.gridY) * (tileW / 2f) + originX
                            val sy = (item.gridX + item.gridY) * (tileH / 2f) + originY

                            val distSq = (tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - (sy - 30f * effectiveScale)) * (tapOffset.y - (sy - 30f * effectiveScale))
                            if (distSq < 80f * 80f * effectiveScale * effectiveScale) {
                                hitFurniture = item
                                break
                            }
                        }

                        if (hitFurniture != null) {
                            selectedFurnitureId = hitFurniture.id
                            val emp = state.employees.find { it.assignedDeskId == hitFurniture.id }
                            if (emp != null) {
                                onSelectEmployee(emp)
                            } else if (hitFurniture.type.category == FurnitureCategory.INFRASTRUCTURE) {
                                onOpenServerDetails()
                            } else {
                                onSelectFurniture(hitFurniture)
                            }
                        } else {
                            selectedFurnitureId = null
                            isMoveMode = false
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val gridW = state.officeTier.gridWidth
            val gridH = state.officeTier.gridHeight
            val gridSpan = (gridW + gridH).toFloat()

            // Dynamic base tile width tailored to mobile portrait screens
            val baseScaleFactor = when (state.officeTier) {
                OfficeTier.GARAGE -> 1.16f
                OfficeTier.COWORKING -> 1.08f
                OfficeTier.TECH_LOFT -> 0.98f
                OfficeTier.CAMPUS_HQ -> 0.90f
                OfficeTier.CYBER_TOWER -> 0.82f
            }
            val rawBaseTileW = ((canvasW * 0.90f) / (gridSpan / 2f)).coerceIn(130f, 260f)
            val effectiveScale = baseScaleFactor * zoomLevel
            val scaledTileW = (rawBaseTileW * effectiveScale).roundToInt()
            val tileW = (if (scaledTileW % 4 != 0) scaledTileW + (4 - scaledTileW % 4) else scaledTileW).toFloat()
            val tileH = tileW / 2f

            val wallHeight = (220f * effectiveScale).roundToInt().toFloat()
            val floorDepth = (gridW + gridH) * (tileH / 2f)

            // Camera origin is permanently fixed at viewport center
            val originX = (canvasW / 2f).roundToInt().toFloat()
            val defaultOriginY = (canvasH * 0.46f) - ((floorDepth - wallHeight) / 2f)
            val originY = defaultOriginY.roundToInt().toFloat()

            // -------------------------------------------------------------
            // LAYER 1: LUXURY ARCHITECTURAL STUDIO BACKDROP
            // Deep subtle vignette with soft overhead studio lighting highlighting the office
            // -------------------------------------------------------------
            val studioCenterColor = when {
                isNightEffective -> Color(0xFF131826)
                isSunsetEffective -> Color(0xFF261922)
                else -> Color(0xFF1A2234)
            }
            val studioEdgeColor = Color(0xFF07090F)

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(studioCenterColor, studioEdgeColor),
                    center = Offset(originX, originY + floorDepth * 0.35f),
                    radius = canvasW * 0.95f
                ),
                size = Size(canvasW, canvasH)
            )

            // Soft atmospheric overhead warm spotlight onto the office
            val spotColor = when {
                isNightEffective -> Color(0x2238BDF8)
                isSunsetEffective -> Color(0x33F97316)
                else -> Color(0x28FDE047)
            }
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(spotColor, Color.Transparent),
                    center = Offset(originX, originY + floorDepth * 0.40f),
                    radius = (gridW + gridH) * (tileW * 0.55f)
                ),
                topLeft = Offset(originX - (gridW + gridH) * tileW * 0.55f, originY - wallHeight * 0.2f),
                size = Size((gridW + gridH) * tileW * 1.10f, (gridW + gridH) * tileH * 1.40f)
            )

            // -------------------------------------------------------------
            // LAYER 2: PLAYABLE OFFICE WALLS, WINDOWS & ARCHITECTURE
            // Includes drywall/brick/marble, panoramic windows looking at downtown, brand plaque, clock
            // -------------------------------------------------------------
            IsometricEnvironmentRenderer.drawWallsAndArchitecture(
                scope = this,
                originX = originX,
                originY = originY,
                tileW = tileW,
                tileH = tileH,
                gridW = gridW,
                gridH = gridH,
                tier = state.officeTier,
                hour = state.clock.hour,
                companyName = state.companyName,
                textMeasurer = textMeasurer,
                scale = effectiveScale
            )

            // -------------------------------------------------------------
            // LAYER 3: 3D OFFICE FOUNDATION PLINTH & GROUND SHADOW
            // -------------------------------------------------------------
            IsometricEnvironmentRenderer.drawFloorFoundationPlinth(
                scope = this,
                originX = originX,
                originY = originY,
                tileW = tileW,
                tileH = tileH,
                gridW = gridW,
                gridH = gridH,
                tier = state.officeTier,
                scale = effectiveScale
            )

            // -------------------------------------------------------------
            // LAYER 4: TEXTURED HIGH-DETAIL INTERIOR FLOOR TILES
            // Concrete screed, herringbone parquet, American walnut, carpet tiles, or Calacatta marble
            // -------------------------------------------------------------
            for (gx in 0 until gridW) {
                for (gy in 0 until gridH) {
                    val sx = (gx - gy) * (tileW / 2f) + originX
                    val sy = (gx + gy) * (tileH / 2f) + originY

                    val isSelectedTile = selectedFurnitureId != null && state.furniture.any { it.id == selectedFurnitureId && it.gridX == gx && it.gridY == gy }
                    val activeSelectedFurn = state.furniture.find { it.id == selectedFurnitureId }
                    val isLegalMoveTile = isMoveMode && activeSelectedFurn != null && (
                        OfficeLayoutManager.validatePlacement(
                            activeSelectedFurn.type,
                            gx,
                            gy,
                            activeSelectedFurn.id,
                            state.furniture,
                            state.officeTier
                        ) is OfficeLayoutManager.PlacementValidation.Valid
                    )

                    IsometricEnvironmentRenderer.drawFloorTile(
                        scope = this,
                        centerX = sx,
                        centerY = sy,
                        tileW = tileW,
                        tileH = tileH,
                        tier = state.officeTier,
                        gx = gx,
                        gy = gy,
                        isSelected = isSelectedTile,
                        isMoveTarget = isLegalMoveTile
                    )
                }
            }

            // -------------------------------------------------------------
            // LAYER 5: DEPTH-SORTED INTERIOR FURNITURE & ANIMATED EMPLOYEES
            // Desks, dual monitors, Aeron chairs, server racks, plants, water cooler, arcade, whiteboard
            // -------------------------------------------------------------
            val sortedFurniture = state.furniture.sortedWith(
                compareBy({ it.gridX + it.gridY }, { it.gridX - it.gridY }, { it.id })
            )

            for (furn in sortedFurniture) {
                val sx = (furn.gridX - furn.gridY) * (tileW / 2f) + originX
                val sy = (furn.gridX + furn.gridY) * (tileH / 2f) + originY

                val assignedEmp = state.employees.find { it.assignedDeskId == furn.id }
                val isSelected = furn.id == selectedFurnitureId

                // Soft ambient contact shadow beneath furniture
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x66000000), Color(0x00000000)),
                        center = Offset(sx, sy),
                        radius = 58f * effectiveScale
                    ),
                    topLeft = Offset(sx - 60f * effectiveScale, sy - 20f * effectiveScale),
                    size = Size(120f * effectiveScale, 48f * effectiveScale)
                )

                // Selection highlight ring
                if (isSelected) {
                    drawOval(
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                        topLeft = Offset(sx - 64f * effectiveScale, sy - 24f * effectiveScale),
                        size = Size(128f * effectiveScale, 52f * effectiveScale),
                        style = Stroke(width = 4.5f * effectiveScale)
                    )
                }

                // Render Pre-Rendered 3D Model Sprite (with employee or equipment)
                IsometricFurnitureRenderer.drawFurniture(
                    scope = this,
                    furn = furn,
                    centerX = sx,
                    centerY = sy,
                    tileW = tileW,
                    tileH = tileH,
                    scale = effectiveScale,
                    animTime = animTime,
                    serverLoad = state.serverLoad,
                    hasOutage = state.server.isOutageActive,
                    assignedEmp = assignedEmp,
                    sprites = sprites
                )
            }
        }

        // -------------------------------------------------------------
        // TOP CONTROLS & HUD OVERLAYS
        // -------------------------------------------------------------

        // Top Right: Zoom In, Zoom Out, Reset Zoom, and Time-of-Day Lighting Toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Day/Night Lighting Cycle Button
            FilledTonalIconButton(
                onClick = {
                    SoundManager.playClick()
                    lightingMode = when (lightingMode) {
                        TimeOfDayLighting.AUTO_CLOCK -> TimeOfDayLighting.FORCE_DAY
                        TimeOfDayLighting.FORCE_DAY -> TimeOfDayLighting.FORCE_SUNSET
                        TimeOfDayLighting.FORCE_SUNSET -> TimeOfDayLighting.FORCE_NIGHT
                        TimeOfDayLighting.FORCE_NIGHT -> TimeOfDayLighting.AUTO_CLOCK
                    }
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = when (lightingMode) {
                        TimeOfDayLighting.FORCE_NIGHT -> Color(0xFF1E1B4B)
                        TimeOfDayLighting.FORCE_SUNSET -> Color(0xFF7C2D12)
                        TimeOfDayLighting.FORCE_DAY -> Color(0xFF0284C7)
                        TimeOfDayLighting.AUTO_CLOCK -> Color(0xFF1E293B)
                    }.copy(alpha = 0.95f)
                )
            ) {
                Icon(
                    imageVector = when (lightingMode) {
                        TimeOfDayLighting.FORCE_NIGHT -> Icons.Default.NightsStay
                        TimeOfDayLighting.FORCE_SUNSET -> Icons.Default.WbTwilight
                        TimeOfDayLighting.FORCE_DAY -> Icons.Default.WbSunny
                        TimeOfDayLighting.AUTO_CLOCK -> Icons.Default.Schedule
                    },
                    contentDescription = "Toggle Time of Day Lighting",
                    tint = when (lightingMode) {
                        TimeOfDayLighting.FORCE_NIGHT -> Color(0xFF38BDF8)
                        TimeOfDayLighting.FORCE_SUNSET -> Color(0xFFF97316)
                        TimeOfDayLighting.FORCE_DAY -> Color(0xFFFDE047)
                        TimeOfDayLighting.AUTO_CLOCK -> Color.White
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            // Zoom In button
            FilledTonalIconButton(
                onClick = {
                    SoundManager.playClick()
                    zoomLevel = (zoomLevel + 0.10f).coerceAtMost(maxZoom)
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Zoom Out button
            FilledTonalIconButton(
                onClick = {
                    SoundManager.playClick()
                    zoomLevel = (zoomLevel - 0.10f).coerceAtLeast(minZoom)
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
            }

            // Reset Zoom button (centers default 1.0f zoom distance)
            FilledTonalIconButton(
                onClick = {
                    SoundManager.playClick()
                    zoomLevel = 1.0f
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset Zoom", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
            }
        }

        // Top Left: Office Architecture Tier & Status Badge
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.94f),
            border = BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.60f)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        text = state.officeTier.title,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${state.officeTier.gridWidth}×${state.officeTier.gridHeight} Studio",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${state.employees.size}/${state.totalWorkstations} Staff",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "•",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                    Text(
                        text = when (lightingMode) {
                            TimeOfDayLighting.AUTO_CLOCK -> if (isNightEffective) "Night (${state.clock.hour}:00)" else "Day (${state.clock.hour}:00)"
                            TimeOfDayLighting.FORCE_DAY -> "Daylight"
                            TimeOfDayLighting.FORCE_SUNSET -> "Sunset"
                            TimeOfDayLighting.FORCE_NIGHT -> "Cyber Night"
                        },
                        color = if (isNightEffective) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Selected Furniture Interactive Action Toolbar (Rotate, Move, Sell)
        AnimatedVisibility(
            visible = selectedFurnitureId != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 74.dp)
        ) {
            val selectedFurn = state.furniture.find { it.id == selectedFurnitureId }
            if (selectedFurn != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.96f),
                    border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedFurn.type.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            val assignedEmp = state.employees.find { it.assignedDeskId == selectedFurn.id }
                            Text(
                                text = if (assignedEmp != null) "Assigned: ${assignedEmp.name}" else "Unassigned",
                                color = if (assignedEmp != null) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Rotate button
                        FilledTonalIconButton(
                            onClick = { onRotateFurniture(selectedFurn.id) },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFF1E293B)
                            ),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color(0xFF00E5FF))
                        }

                        // Move button
                        Button(
                            onClick = { isMoveMode = !isMoveMode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMoveMode) Color(0xFF10B981) else Color(0xFF334155)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.OpenWith, contentDescription = "Move", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isMoveMode) "Tap Tile" else "Move", fontSize = 12.sp)
                        }

                        // Sell button
                        IconButton(
                            onClick = {
                                onSellFurniture(selectedFurn.id)
                                selectedFurnitureId = null
                                isMoveMode = false
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Sell", tint = Color(0xFFEF4444))
                        }

                        // Close button
                        IconButton(
                            onClick = {
                                selectedFurnitureId = null
                                isMoveMode = false
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Deselect", tint = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
