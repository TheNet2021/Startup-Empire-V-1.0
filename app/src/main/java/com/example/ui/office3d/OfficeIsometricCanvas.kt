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
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Selected furniture for interactive move / rotate / sell
    var selectedFurnitureId by remember { mutableStateOf<String?>(null) }
    var isMoveMode by remember { mutableStateOf(false) }

    // Smooth animation loop for character typing, LED pulses, screen scrolls, and clocks
    val infiniteTransition = rememberInfiniteTransition(label = "office_gameplay_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
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
                    scale = (scale * zoom).coerceIn(0.65f, 2.2f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.furniture, scale, panOffset, isMoveMode, selectedFurnitureId) {
                    detectTapGestures { tapOffset ->
                        val tileW = 120f * scale
                        val tileH = 60f * scale
                        val originX = (size.width / 2f) + panOffset.x
                        val originY = (size.height / 3.2f) + panOffset.y

                        // Tap coordinates to isometric grid
                        val dx = tapOffset.x - originX
                        val dy = tapOffset.y - originY

                        val gxFloat = (dx / (tileW / 2f) + dy / (tileH / 2f)) / 2f
                        val gyFloat = (dy / (tileH / 2f) - dx / (tileW / 2f)) / 2f

                        val tappedGx = kotlin.math.floor(gxFloat).toInt()
                        val tappedGy = kotlin.math.floor(gyFloat).toInt()

                        val gridW = state.officeTier.gridWidth
                        val gridH = state.officeTier.gridHeight

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

                            val distSq = (tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - (sy - 24f * scale)) * (tapOffset.y - (sy - 24f * scale))
                            if (distSq < 60f * 60f * scale * scale) {
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

            // Enlarged base tile dimensions for crisp viewing on mobile screens
            val tileW = 120f * scale
            val tileH = 60f * scale
            val originX = (canvasW / 2f) + panOffset.x
            val originY = (canvasH / 3.2f) + panOffset.y

            val gridW = state.officeTier.gridWidth
            val gridH = state.officeTier.gridHeight

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

            // 2. Draw High-Fidelity Textured Floor Tiles
            for (gx in 0 until gridW) {
                for (gy in 0 until gridH) {
                    val sx = (gx - gy) * (tileW / 2f) + originX
                    val sy = (gx + gy) * (tileH / 2f) + originY

                    val isOccupied = state.furniture.any { it.gridX == gx && it.gridY == gy }
                    val isSelectedTile = selectedFurnitureId != null && state.furniture.any { it.id == selectedFurnitureId && it.gridX == gx && it.gridY == gy }

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
                        isMoveTarget = isMoveMode && !isOccupied
                    )
                }
            }

            // 3. Depth-Sorted Rendering of Furniture and Seated Employees
            val sortedFurniture = state.furniture.sortedBy { it.gridX + it.gridY }

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
                        radius = 42f * scale
                    ),
                    topLeft = Offset(sx - 44f * scale, sy - 14f * scale),
                    size = Size(88f * scale, 34f * scale)
                )

                // Selection highlight ring
                if (isSelected) {
                    drawOval(
                        color = Color(0xFF00E5FF).copy(alpha = 0.7f),
                        topLeft = Offset(sx - 48f * scale, sy - 18f * scale),
                        size = Size(96f * scale, 38f * scale),
                        style = Stroke(width = 3.5f * scale)
                    )
                }

                // Render 3D Furniture Model
                IsometricFurnitureRenderer.drawFurniture(
                    scope = this,
                    furn = furn,
                    centerX = sx,
                    centerY = sy,
                    scale = scale,
                    animTime = animTime,
                    serverLoad = state.serverLoad,
                    hasOutage = state.server.isOutageActive,
                    assignedEmp = assignedEmp
                )

                // Render Stylized 3D Adult Character seated at this workstation
                if (assignedEmp != null) {
                    IsometricCharacterRenderer.drawCharacter(
                        scope = this,
                        emp = assignedEmp,
                        centerX = sx,
                        centerY = sy - (12f * scale),
                        scale = scale,
                        animTime = animTime,
                        furnitureRotation = furn.rotation
                    )
                }
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
                onClick = { scale = (scale + 0.2f).coerceAtMost(2.2f) },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            FilledTonalIconButton(
                onClick = { scale = (scale - 0.2f).coerceAtLeast(0.65f) },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            FilledTonalIconButton(
                onClick = {
                    scale = 1.0f
                    panOffset = Offset.Zero
                    selectedFurnitureId = null
                    isMoveMode = false
                },
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)
                )
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset Camera", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
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
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Text(
                    text = state.officeTier.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.employees.size}/${state.totalWorkstations} Staff",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
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
                .padding(bottom = 12.dp)
        ) {
            val selectedFurn = state.furniture.find { it.id == selectedFurnitureId }
            if (selectedFurn != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Text(
                                text = selectedFurn.type.title,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tile (${selectedFurn.gridX}, ${selectedFurn.gridY})",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        // Rotate button
                        Button(
                            onClick = { onRotateFurniture(selectedFurn.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rotate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Move button
                        Button(
                            onClick = { isMoveMode = !isMoveMode },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isMoveMode) Color(0xFF10B981) else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(1.dp, if (isMoveMode) Color(0xFF10B981) else Color(0xFF475569)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.Default.OpenWith, contentDescription = null, tint = if (isMoveMode) Color.White else Color(0xFFCBD5E1), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isMoveMode) "Tap Tile" else "Move", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Close selection
                        IconButton(
                            onClick = {
                                selectedFurnitureId = null
                                isMoveMode = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Deselect", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
