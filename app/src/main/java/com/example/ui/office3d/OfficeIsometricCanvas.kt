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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.roundToInt

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
    // Zoom scale: starts framed at 1.15f for an expansive mobile viewport view
    var scale by remember { mutableFloatStateOf(1.15f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Selected furniture for interactive move / rotate / sell
    var selectedFurnitureId by remember { mutableStateOf<String?>(null) }
    var isMoveMode by remember { mutableStateOf(false) }

    // Load and cache all pre-rendered 3D isometric sprites
    val sprites = rememberIsometricSpriteAtlas()

    // Smooth continuous animation cycle for LED blinks, screen refreshes, and breathing
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

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF090D16),
                        Color(0xFF030712)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.70f, 2.5f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.furniture, scale, panOffset, isMoveMode, selectedFurnitureId, state.officeTier) {
                    detectTapGestures { tapOffset ->
                        val canvasW = size.width.toFloat()
                        val canvasH = size.height.toFloat()

                        val gridW = state.officeTier.gridWidth
                        val gridH = state.officeTier.gridHeight
                        val gridSpan = (gridW + gridH).toFloat()

                        val rawBaseTileW = ((canvasW * 0.88f) / (gridSpan / 2f)).coerceIn(150f, 260f)
                        val scaledTileW = (rawBaseTileW * scale).roundToInt()
                        val tileW = (if (scaledTileW % 4 != 0) scaledTileW + (4 - scaledTileW % 4) else scaledTileW).toFloat()
                        val tileH = tileW / 2f

                        val wallHeight = (220f * scale).roundToInt().toFloat()
                        val floorDepth = (gridW + gridH) * (tileH / 2f)

                        val originX = ((canvasW / 2f) + panOffset.x).roundToInt().toFloat()
                        val defaultOriginY = (canvasH * 0.44f) - ((floorDepth - wallHeight) / 2f)
                        val originY = (defaultOriginY + panOffset.y).roundToInt().toFloat()

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

                            val distSq = (tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - (sy - 30f * scale)) * (tapOffset.y - (sy - 30f * scale))
                            if (distSq < 80f * 80f * scale * scale) {
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

            // Dynamic base tile width tailored to mobile portrait screens to fill 88% width
            val rawBaseTileW = ((canvasW * 0.88f) / (gridSpan / 2f)).coerceIn(150f, 260f)
            // Snap tileW to a multiple of 4 so (tileW / 2f) and (tileH / 2f) are exact whole integers
            val scaledTileW = (rawBaseTileW * scale).roundToInt()
            val tileW = (if (scaledTileW % 4 != 0) scaledTileW + (4 - scaledTileW % 4) else scaledTileW).toFloat()
            val tileH = tileW / 2f

            val wallHeight = (220f * scale).roundToInt().toFloat()
            val floorDepth = (gridW + gridH) * (tileH / 2f)

            val originX = ((canvasW / 2f) + panOffset.x).roundToInt().toFloat()
            // Auto-center vertically in portrait viewport, leaving comfortable clearance for top & bottom bars
            val defaultOriginY = (canvasH * 0.44f) - ((floorDepth - wallHeight) / 2f)
            val originY = (defaultOriginY + panOffset.y).roundToInt().toFloat()

            // 1. Draw Architectural Walls, Windows, and Lighting per Tier
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
                scale = scale
            )

            // 2. Draw 3D Foundation Slab / Plinth with Ground Shadow
            IsometricEnvironmentRenderer.drawFloorFoundationPlinth(
                scope = this,
                originX = originX,
                originY = originY,
                tileW = tileW,
                tileH = tileH,
                gridW = gridW,
                gridH = gridH,
                tier = state.officeTier,
                scale = scale
            )

            // 3. Draw High-Fidelity Textured Floor Tiles
            for (gx in 0 until gridW) {
                for (gy in 0 until gridH) {
                    val sx = (gx - gy) * (tileW / 2f) + originX
                    val sy = (gx + gy) * (tileH / 2f) + originY

                    val isOccupied = state.furniture.any { it.gridX == gx && it.gridY == gy }
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

            // 4. Depth-Sorted Rendering of Furniture and Seated Employees
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
                        radius = 58f * scale
                    ),
                    topLeft = Offset(sx - 60f * scale, sy - 20f * scale),
                    size = Size(120f * scale, 48f * scale)
                )

                // Selection highlight ring
                if (isSelected) {
                    drawOval(
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                        topLeft = Offset(sx - 64f * scale, sy - 24f * scale),
                        size = Size(128f * scale, 52f * scale),
                        style = Stroke(width = 4.5f * scale)
                    )
                }

                // Render High-Resolution Pre-Rendered 3D Model Sprite (with employee or equipment)
                IsometricFurnitureRenderer.drawFurniture(
                    scope = this,
                    furn = furn,
                    centerX = sx,
                    centerY = sy,
                    tileW = tileW,
                    tileH = tileH,
                    scale = scale,
                    animTime = animTime,
                    serverLoad = state.serverLoad,
                    hasOutage = state.server.isOutageActive,
                    assignedEmp = assignedEmp,
                    sprites = sprites
                )
            }
        }

        // Camera zoom & pan reset controls at top right of office view
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalIconButton(
                onClick = { scale = (scale + 0.20f).coerceAtMost(2.5f) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            FilledTonalIconButton(
                onClick = { scale = (scale - 0.20f).coerceAtLeast(0.70f) },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            FilledTonalIconButton(
                onClick = {
                    scale = 1.15f
                    panOffset = Offset.Zero
                    selectedFurnitureId = null
                    isMoveMode = false
                },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.92f)
                )
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset Camera", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
            }
        }

        // Office tier & Workstation status badge at top left of office view
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.92f),
            border = BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Text(
                    text = state.officeTier.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.employees.size}/${state.totalWorkstations} Staff • ${state.totalWorkstations}/${state.officeTier.maxDesks} Desks",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        // Selected Furniture Interactive Action Toolbar (Rotate, Move, Sell)
        AnimatedVisibility(
            visible = selectedFurnitureId != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 74.dp) // Elevated above the floating action pill
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
