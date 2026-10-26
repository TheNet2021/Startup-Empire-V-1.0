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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import kotlin.math.cos
import kotlin.math.sin

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
    var hoveredGridTile by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Animation transition for typing hands, LED pulses, screen scrolls, clock
    val infiniteTransition = rememberInfiniteTransition(label = "office_gameplay_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
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
                        Color(0xFF1E222D),
                        Color(0xFF161A24),
                        Color(0xFF10131A)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.65f, 2.4f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(state.furniture, scale, panOffset, isMoveMode, selectedFurnitureId) {
                    detectTapGestures { tapOffset ->
                        val tileW = 96f * scale
                        val tileH = 48f * scale
                        val originX = (size.width / 2f) + panOffset.x
                        val originY = (size.height / 3.4f) + panOffset.y

                        // Compute tapped tile coordinates from screen tap
                        // sx - originX = (gx - gy) * (tileW / 2)
                        // sy - originY = (gx + gy) * (tileH / 2)
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

                        // Check if tapped near an existing furniture
                        var hitFurniture: PlacedFurniture? = null
                        for (item in state.furniture) {
                            val sx = (item.gridX - item.gridY) * (tileW / 2f) + originX
                            val sy = (item.gridX + item.gridY) * (tileH / 2f) + originY

                            val distSq = (tapOffset.x - sx) * (tapOffset.x - sx) + (tapOffset.y - (sy - 22f * scale)) * (tapOffset.y - (sy - 22f * scale))
                            if (distSq < 52f * 52f * scale * scale) {
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
                            // Tapped on open floor
                            selectedFurnitureId = null
                            isMoveMode = false
                        }
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val tileW = 96f * scale
            val tileH = 48f * scale
            val originX = (canvasW / 2f) + panOffset.x
            val originY = (canvasH / 3.4f) + panOffset.y

            val gridW = state.officeTier.gridWidth
            val gridH = state.officeTier.gridHeight

            // 1. Draw Office Walls & Panoramic Daylight Windows
            drawModernOfficeWalls(
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

            // 2. Draw Realistic Office Floor Grid (Modern Carpet/Parquet with visible seams)
            for (gx in 0 until gridW) {
                for (gy in 0 until gridH) {
                    val sx = (gx - gy) * (tileW / 2f) + originX
                    val sy = (gx + gy) * (tileH / 2f) + originY

                    val isOccupied = state.furniture.any { it.gridX == gx && it.gridY == gy }
                    val isSelectedTile = selectedFurnitureId != null && state.furniture.any { it.id == selectedFurnitureId && it.gridX == gx && it.gridY == gy }

                    drawModernFloorTile(
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

            // 3. Depth-Sorted Rendering of Furniture and Employees
            val sortedFurniture = state.furniture.sortedBy { it.gridX + it.gridY }

            for (furn in sortedFurniture) {
                val sx = (furn.gridX - furn.gridY) * (tileW / 2f) + originX
                val sy = (furn.gridX + furn.gridY) * (tileH / 2f) + originY

                val assignedEmp = state.employees.find { it.assignedDeskId == furn.id }
                val isSelected = furn.id == selectedFurnitureId

                // Soft contact shadow underneath furniture
                drawOval(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(sx - 34f * scale, sy - 8f * scale),
                    size = Size(68f * scale, 24f * scale)
                )

                // Selection highlight ring
                if (isSelected) {
                    drawOval(
                        color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                        topLeft = Offset(sx - 38f * scale, sy - 12f * scale),
                        size = Size(76f * scale, 30f * scale),
                        style = Stroke(width = 3f * scale)
                    )
                }

                drawDetailedOfficeFurniture(
                    furn = furn,
                    centerX = sx,
                    centerY = sy,
                    scale = scale,
                    animTime = animTime,
                    serverLoad = state.serverLoad,
                    hasOutage = state.server.isOutageActive,
                    assignedEmp = assignedEmp
                )

                // Draw detailed employee if seated at this desk
                if (assignedEmp != null) {
                    drawDetailedEmployee(
                        emp = assignedEmp,
                        centerX = sx,
                        centerY = sy - (14f * scale),
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
                onClick = { scale = (scale + 0.2f).coerceAtMost(2.4f) },
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
            color = Color(0xFF0F172A).copy(alpha = 0.9f),
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
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
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
                                text = "Tile (${selectedFurn.gridX}, ${selectedFurn.gridY}) • ${selectedFurn.rotation}°",
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
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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

// -------------------------------------------------------------
// REALISTIC OFFICE FLOOR RENDERING
// -------------------------------------------------------------
private fun DrawScope.drawModernFloorTile(
    centerX: Float,
    centerY: Float,
    tileW: Float,
    tileH: Float,
    tier: OfficeTier,
    gx: Int,
    gy: Int,
    isSelected: Boolean,
    isMoveTarget: Boolean
) {
    val path = Path().apply {
        moveTo(centerX, centerY - tileH / 2f)
        lineTo(centerX + tileW / 2f, centerY)
        lineTo(centerX, centerY + tileH / 2f)
        lineTo(centerX - tileW / 2f, centerY)
        close()
    }

    val isAlternate = (gx + gy) % 2 == 0

    // High quality modern office flooring:
    // Light corporate carpet tiles or warm oak parquet wood boards with bevel seams
    val baseColor = when (tier) {
        OfficeTier.GARAGE -> {
            // Modern warm grey commercial office carpet
            if (isAlternate) Color(0xFFD6DBE4) else Color(0xFFC7CDD8)
        }
        OfficeTier.COWORKING -> {
            // Natural Honey Oak Parquet
            if (isAlternate) Color(0xFFE4CBB4) else Color(0xFFD8BC9E)
        }
        OfficeTier.TECH_LOFT -> {
            // Rich Chestnut Hardwood Floorboards
            if (isAlternate) Color(0xFFC4A076) else Color(0xFFB58E62)
        }
        OfficeTier.CAMPUS_HQ -> {
            // Premium light Scandinavian Birch & Grey Slate
            if (isAlternate) Color(0xFFE2E8F0) else Color(0xFFCBD5E1)
        }
        OfficeTier.CYBER_TOWER -> {
            // High-end Executive Penthouse Marble & Polished Porcelain
            if (isAlternate) Color(0xFFF1F5F9) else Color(0xFFE2E8F0)
        }
    }

    val tileColor = when {
        isSelected -> Color(0xFF00E5FF).copy(alpha = 0.45f)
        isMoveTarget -> Color(0xFF10B981).copy(alpha = 0.4f)
        else -> baseColor
    }

    drawPath(path, tileColor)

    // Seam borders between tiles
    val seamColor = when (tier) {
        OfficeTier.GARAGE -> Color(0xFFA0AAB8).copy(alpha = 0.5f)
        OfficeTier.COWORKING, OfficeTier.TECH_LOFT -> Color(0xFF9E7C52).copy(alpha = 0.5f)
        else -> Color(0xFF94A3B8).copy(alpha = 0.6f)
    }

    drawPath(path, seamColor, style = Stroke(width = 1f))

    if (isSelected) {
        drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 2.5f))
    } else if (isMoveTarget) {
        drawPath(path, Color(0xFF10B981), style = Stroke(width = 2.5f))
    }
}

// -------------------------------------------------------------
// REALISTIC OFFICE WALLS & PANORAMIC DAYLIGHT WINDOWS
// -------------------------------------------------------------
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawModernOfficeWalls(
    originX: Float,
    originY: Float,
    tileW: Float,
    tileH: Float,
    gridW: Int,
    gridH: Int,
    tier: OfficeTier,
    hour: Int,
    companyName: String,
    textMeasurer: TextMeasurer,
    scale: Float
) {
    val wallHeight = 135f * scale

    val topX = originX
    val topY = originY - tileH / 2f

    val leftCornerX = (-gridH) * (tileW / 2f) + originX
    val leftCornerY = (gridH) * (tileH / 2f) + originY

    val rightCornerX = (gridW) * (tileW / 2f) + originX
    val rightCornerY = (gridW) * (tileH / 2f) + originY

    // Left wall polygon
    val leftWall = Path().apply {
        moveTo(topX, topY)
        lineTo(leftCornerX, leftCornerY)
        lineTo(leftCornerX, leftCornerY - wallHeight)
        lineTo(topX, topY - wallHeight)
        close()
    }

    // Right wall polygon
    val rightWall = Path().apply {
        moveTo(topX, topY)
        lineTo(rightCornerX, rightCornerY)
        lineTo(rightCornerX, rightCornerY - wallHeight)
        lineTo(topX, topY - wallHeight)
        close()
    }

    // Modern clean drywall wall colors (Off-white / Architectural light slate)
    drawPath(leftWall, Color(0xFFE2E8F0))
    drawPath(rightWall, Color(0xFFF1F5F9))

    // Wall outlines
    drawPath(leftWall, Color(0xFF94A3B8), style = Stroke(width = 1.5f))
    drawPath(rightWall, Color(0xFF94A3B8), style = Stroke(width = 1.5f))

    // Architectural Skirting Baseboard (Dark grey / Mahogany)
    val baseboardH = 7f * scale
    val baseboardLeft = Path().apply {
        moveTo(topX, topY)
        lineTo(leftCornerX, leftCornerY)
        lineTo(leftCornerX, leftCornerY - baseboardH)
        lineTo(topX, topY - baseboardH)
        close()
    }
    val baseboardRight = Path().apply {
        moveTo(topX, topY)
        lineTo(rightCornerX, rightCornerY)
        lineTo(rightCornerX, rightCornerY - baseboardH)
        lineTo(topX, topY - baseboardH)
        close()
    }
    drawPath(baseboardLeft, Color(0xFF334155))
    drawPath(baseboardRight, Color(0xFF475569))

    // -------------------------------------------------------------
    // RIGHT WALL: LARGE PANORAMIC DAYLIGHT OFFICE WINDOWS
    // -------------------------------------------------------------
    val winCount = (gridW / 3).coerceIn(2, 4)
    for (i in 0 until winCount) {
        val t1 = (i + 0.35f) / winCount
        val t2 = (i + 0.85f) / winCount

        val wx1 = topX + (rightCornerX - topX) * t1
        val wy1 = topY + (rightCornerY - topY) * t1
        val wx2 = topX + (rightCornerX - topX) * t2
        val wy2 = topY + (rightCornerY - topY) * t2

        val winTopY1 = wy1 - wallHeight * 0.78f
        val winTopY2 = wy2 - wallHeight * 0.78f
        val winBottomY1 = wy1 - wallHeight * 0.22f
        val winBottomY2 = wy2 - wallHeight * 0.22f

        val windowPolygon = Path().apply {
            moveTo(wx1, winBottomY1)
            lineTo(wx2, winBottomY2)
            lineTo(wx2, winTopY2)
            lineTo(wx1, winTopY1)
            close()
        }

        // Sky background through window (Daylight vs Night)
        val isDay = hour in 6..19
        val skyTop = if (isDay) Color(0xFF38BDF8) else Color(0xFF0F172A)
        val skyBottom = if (isDay) Color(0xFF93C5FD) else Color(0xFF1E293B)

        drawPath(
            windowPolygon,
            Brush.verticalGradient(
                colors = listOf(skyTop, skyBottom),
                startY = winTopY1,
                endY = winBottomY1
            )
        )

        // Modern city skyscrapers silhouette through window
        val bldgW = (wx2 - wx1) * 0.32f
        drawRect(
            color = if (isDay) Color(0xFF64748B) else Color(0xFF1E293B),
            topLeft = Offset(wx1 + bldgW * 0.2f, winBottomY1 - 42f * scale),
            size = Size(bldgW, 42f * scale)
        )
        drawRect(
            color = if (isDay) Color(0xFF475569) else Color(0xFF0F172A),
            topLeft = Offset(wx1 + bldgW * 1.5f, winBottomY1 - 58f * scale),
            size = Size(bldgW * 1.1f, 58f * scale)
        )

        // Window mullion steel frame
        drawPath(windowPolygon, Color(0xFF334155), style = Stroke(width = 3.5f * scale))
        // Window vertical center divider
        val midX = (wx1 + wx2) / 2f
        val midYBottom = (winBottomY1 + winBottomY2) / 2f
        val midYTop = (winTopY1 + winTopY2) / 2f
        drawLine(
            color = Color(0xFF334155),
            start = Offset(midX, midYBottom),
            end = Offset(midX, midYTop),
            strokeWidth = 2f * scale
        )
    }

    // -------------------------------------------------------------
    // LEFT WALL: COMPANY LOGO BOARD, WALL CLOCK & DASHBOARD SCREEN
    // -------------------------------------------------------------
    // 1. Company Logo Board in center of left wall
    val logoCenterT = 0.45f
    val lx = topX + (leftCornerX - topX) * logoCenterT
    val ly = topY + (leftCornerY - topY) * logoCenterT - wallHeight * 0.62f
    val signW = 68f * scale
    val signH = 28f * scale

    // Wood/Matte backdrop plate
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(lx - signW / 2f, ly - signH / 2f),
        size = Size(signW, signH),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(lx - signW / 2f, ly - signH / 2f),
        size = Size(signW, signH),
        cornerRadius = CornerRadius(4f * scale, 4f * scale),
        style = Stroke(width = 1.5f * scale)
    )

    // Sign text
    val signLayout = textMeasurer.measure(
        text = AnnotatedString(companyName.take(14)),
        style = TextStyle(
            color = Color.White,
            fontSize = (9f * scale).coerceAtLeast(8f).sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        textLayoutResult = signLayout,
        topLeft = Offset(lx - signLayout.size.width / 2f, ly - signLayout.size.height / 2f)
    )

    // 2. Wall Clock with ticking hands
    val clockX = topX + (leftCornerX - topX) * 0.18f
    val clockY = topY + (leftCornerY - topY) * 0.18f - wallHeight * 0.68f
    val clockR = 12f * scale

    drawCircle(Color(0xFF334155), radius = clockR, center = Offset(clockX, clockY))
    drawCircle(Color.White, radius = clockR - 2f * scale, center = Offset(clockX, clockY))
    // Hour hand & Minute hand
    val hourAngle = ((hour % 12) / 12f) * 6.283f - 1.5707f
    val minAngle = 1.5707f
    drawLine(
        color = Color(0xFF0F172A),
        start = Offset(clockX, clockY),
        end = Offset(clockX + cos(hourAngle) * clockR * 0.55f, clockY + sin(hourAngle) * clockR * 0.55f),
        strokeWidth = 2f * scale
    )
    drawLine(
        color = Color(0xFFEF4444),
        start = Offset(clockX, clockY),
        end = Offset(clockX + cos(minAngle) * clockR * 0.75f, clockY + sin(minAngle) * clockR * 0.75f),
        strokeWidth = 1.2f * scale
    )
    drawCircle(Color(0xFF0F172A), radius = 1.5f * scale, center = Offset(clockX, clockY))
}

// -------------------------------------------------------------
// DETAILED MODERN BUSINESS FURNITURE RENDERING
// -------------------------------------------------------------
private fun DrawScope.drawDetailedOfficeFurniture(
    furn: PlacedFurniture,
    centerX: Float,
    centerY: Float,
    scale: Float,
    animTime: Float,
    serverLoad: Float,
    hasOutage: Boolean,
    assignedEmp: Employee?
) {
    val s = scale
    val rot = furn.rotation

    when (furn.type) {
        FurnitureType.DESK_STANDARD, FurnitureType.DESK_DUAL_SCREEN, FurnitureType.DESK_EXECUTIVE -> {
            drawRealisticWorkstation(
                centerX = centerX,
                centerY = centerY,
                scale = s,
                type = furn.type,
                rotation = rot,
                animTime = animTime,
                isOccupied = assignedEmp != null
            )
        }

        FurnitureType.RECEPTION_DESK -> {
            drawReceptionDesk(centerX, centerY, s, rot)
        }

        FurnitureType.MEETING_TABLE -> {
            drawConferenceTable(centerX, centerY, s)
        }

        FurnitureType.SERVER_RACK_BLADE, FurnitureType.SERVER_RACK_ENTERPRISE -> {
            drawITServerCabinet(centerX, centerY, s, serverLoad, hasOutage, animTime, furn.type == FurnitureType.SERVER_RACK_ENTERPRISE)
        }

        FurnitureType.ESPRESSO_MACHINE -> {
            drawEspressoKitchenette(centerX, centerY, s, animTime)
        }

        FurnitureType.WATER_COOLER -> {
            drawWaterCoolerDispenser(centerX, centerY, s, animTime)
        }

        FurnitureType.BONSAI_PLANT -> {
            drawIndoorFicusPlant(centerX, centerY, s)
        }

        FurnitureType.WHITEBOARD -> {
            drawAgileWhiteboard(centerX, centerY, s)
        }

        FurnitureType.BOOKSHELF -> {
            drawTechBookshelf(centerX, centerY, s)
        }

        FurnitureType.ARCADE_CABINET -> {
            drawArcadeCabinet(centerX, centerY, s, animTime)
        }
    }
}

// -------------------------------------------------------------
// REALISTIC WORKSTATION (Wood/Laminate Desk, Steel Frame, Ergonomic Chair, Monitors, Keyboard, PC Tower)
// -------------------------------------------------------------
private fun DrawScope.drawRealisticWorkstation(
    centerX: Float,
    centerY: Float,
    scale: Float,
    type: FurnitureType,
    rotation: Int,
    animTime: Float,
    isOccupied: Boolean
) {
    val s = scale

    // Desk dimensions
    val deskW = 54f * s
    val deskD = 30f * s
    val deskTopY = centerY - 18f * s

    // Tabletop finish: Natural Birch Oak or Crisp White Laminate
    val topSurfaceColor = when (type) {
        FurnitureType.DESK_EXECUTIVE -> Color(0xFF334155) // Executive Slate Dark Wood
        FurnitureType.DESK_DUAL_SCREEN -> Color(0xFFE2D6C5) // Warm Birch Wood
        else -> Color(0xFFF8FAFC) // Crisp Modern White Laminate
    }

    // Steel leg frame under desk
    val legColor = Color(0xFF334155)
    // Left leg
    drawLine(
        color = legColor,
        start = Offset(centerX - deskW / 2f + 4f * s, deskTopY + deskD * 0.7f),
        end = Offset(centerX - deskW / 2f + 4f * s, centerY + 8f * s),
        strokeWidth = 3f * s
    )
    // Right leg
    drawLine(
        color = legColor,
        start = Offset(centerX + deskW / 2f - 4f * s, deskTopY + deskD * 0.7f),
        end = Offset(centerX + deskW / 2f - 4f * s, centerY + 8f * s),
        strokeWidth = 3f * s
    )
    // Crossbar
    drawLine(
        color = legColor,
        start = Offset(centerX - deskW / 2f + 4f * s, centerY + 2f * s),
        end = Offset(centerX + deskW / 2f - 4f * s, centerY + 2f * s),
        strokeWidth = 2f * s
    )

    // Tabletop with 3D beveled thickness
    val deskBevelH = 5f * s
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(centerX - deskW / 2f, deskTopY + deskBevelH),
        size = Size(deskW, deskD),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
    drawRoundRect(
        color = topSurfaceColor,
        topLeft = Offset(centerX - deskW / 2f, deskTopY),
        size = Size(deskW, deskD),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
    drawRoundRect(
        color = Color(0xFF64748B).copy(alpha = 0.5f),
        topLeft = Offset(centerX - deskW / 2f, deskTopY),
        size = Size(deskW, deskD),
        cornerRadius = CornerRadius(4f * s, 4f * s),
        style = Stroke(width = 1f * s)
    )

    // Desk Accessories:
    // 1. Black ergonomic desk mat
    val matW = 34f * s
    val matD = 18f * s
    drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(centerX - matW / 2f, deskTopY + 7f * s),
        size = Size(matW, matD),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )

    // 2. Keyboard & Optical Mouse
    val kbW = 16f * s
    val kbD = 6f * s
    drawRoundRect(
        color = Color(0xFFCBD5E1),
        topLeft = Offset(centerX - kbW / 2f - 3f * s, deskTopY + 15f * s),
        size = Size(kbW, kbD),
        cornerRadius = CornerRadius(1f * s, 1f * s)
    )
    // Mouse
    drawOval(
        color = Color(0xFF64748B),
        topLeft = Offset(centerX + 8f * s, deskTopY + 16f * s),
        size = Size(4f * s, 6f * s)
    )

    // 3. Desktop Tower PC sitting beside/under desk
    val towerW = 10f * s
    val towerH = 20f * s
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX + deskW / 2f - 2f * s, centerY - 8f * s),
        size = Size(towerW, towerH),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )
    // Blue power LED on PC tower
    drawCircle(Color(0xFF38BDF8), radius = 1.2f * s, center = Offset(centerX + deskW / 2f + 3f * s, centerY - 4f * s))

    // 4. Ceramic Coffee Mug
    drawCircle(Color(0xFFEF4444), radius = 2.5f * s, center = Offset(centerX - deskW / 2f + 7f * s, deskTopY + 8f * s))

    // 5. Widescreen or Dual Monitors
    val isDual = type == FurnitureType.DESK_DUAL_SCREEN || type == FurnitureType.DESK_EXECUTIVE
    val monW = if (isDual) 20f * s else 26f * s
    val monH = 16f * s
    val monStandY = deskTopY + 5f * s

    if (!isDual) {
        // Single Ultrawide Curved Display
        // Stand
        drawLine(Color(0xFF334155), start = Offset(centerX, monStandY), end = Offset(centerX, deskTopY - 4f * s), strokeWidth = 3f * s)
        drawRect(Color(0xFF1E293B), topLeft = Offset(centerX - 6f * s, monStandY), size = Size(12f * s, 3f * s))

        // Monitor bezel
        drawRoundRect(Color(0xFF0F172A), topLeft = Offset(centerX - monW / 2f, deskTopY - monH - 2f * s), size = Size(monW, monH), cornerRadius = CornerRadius(2f * s, 2f * s))

        // Active glowing IDE screen
        val screenColor = if (isOccupied) Color(0xFF0F172A) else Color(0xFF1E293B)
        drawRect(screenColor, topLeft = Offset(centerX - monW / 2f + 2f * s, deskTopY - monH), size = Size(monW - 4f * s, monH - 4f * s))

        // Syntax-highlighted code lines on monitor
        if (isOccupied) {
            val lineShift = (animTime * 3f) % 6f
            val codeColors = listOf(Color(0xFF38BDF8), Color(0xFF4ADE80), Color(0xFFFBBF24), Color(0xFFF472B6))
            for (line in 0 until 3) {
                val ly = deskTopY - monH + 3f * s + (line * 3.5f * s)
                val c = codeColors[(line + lineShift.toInt()) % codeColors.size]
                drawLine(c, start = Offset(centerX - monW / 2f + 4f * s, ly), end = Offset(centerX + monW / 2f - 6f * s - (line * 3f * s), ly), strokeWidth = 1.5f * s)
            }
        }
    } else {
        // Dual Widescreen Displays angled toward user
        val m1X = centerX - 12f * s
        val m2X = centerX + 12f * s

        // Screen 1
        drawRoundRect(Color(0xFF0F172A), topLeft = Offset(m1X - monW / 2f, deskTopY - monH - 2f * s), size = Size(monW, monH), cornerRadius = CornerRadius(2f * s, 2f * s))
        drawRect(Color(0xFF0284C7).copy(alpha = if (isOccupied) 0.9f else 0.4f), topLeft = Offset(m1X - monW / 2f + 1.5f * s, deskTopY - monH), size = Size(monW - 3f * s, monH - 4f * s))

        // Screen 2
        drawRoundRect(Color(0xFF0F172A), topLeft = Offset(m2X - monW / 2f, deskTopY - monH - 2f * s), size = Size(monW, monH), cornerRadius = CornerRadius(2f * s, 2f * s))
        drawRect(Color(0xFF10B981).copy(alpha = if (isOccupied) 0.9f else 0.4f), topLeft = Offset(m2X - monW / 2f + 1.5f * s, deskTopY - monH), size = Size(monW - 3f * s, monH - 4f * s))
    }

    // 6. Ergonomic Office Chair tucked at workstation
    val chairY = centerY + 10f * s
    // 5-spoke star caster base
    for (ang in 0 until 5) {
        val a = (ang / 5f) * 6.283f
        drawLine(Color(0xFF334155), start = Offset(centerX, chairY), end = Offset(centerX + cos(a) * 9f * s, chairY + sin(a) * 5f * s), strokeWidth = 2f * s)
    }
    // Gas cylinder
    drawLine(Color(0xFF475569), start = Offset(centerX, chairY), end = Offset(centerX, chairY - 6f * s), strokeWidth = 3f * s)
    // Seat cushion
    drawOval(Color(0xFF1E293B), topLeft = Offset(centerX - 10f * s, chairY - 10f * s), size = Size(20f * s, 10f * s))
    // Mesh backrest with lumbar arch
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX - 9f * s, chairY - 26f * s),
        size = Size(18f * s, 18f * s),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
    drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(centerX - 7f * s, chairY - 24f * s),
        size = Size(14f * s, 14f * s),
        cornerRadius = CornerRadius(3f * s, 3f * s),
        style = Stroke(width = 1.5f * s)
    )
}

// -------------------------------------------------------------
// DETAILED EMPLOYEE (Realistic Head, Hair, Shirt, Animated Typing Hands)
// -------------------------------------------------------------
private fun DrawScope.drawDetailedEmployee(
    emp: Employee,
    centerX: Float,
    centerY: Float,
    scale: Float,
    animTime: Float,
    furnitureRotation: Int
) {
    val s = scale

    // Employee seated at chair
    val torsoY = centerY - 10f * s
    val torsoW = 16f * s
    val torsoH = 18f * s

    // Shirt / Torso with collar
    drawRoundRect(
        color = Color(emp.shirtColor),
        topLeft = Offset(centerX - torsoW / 2f, torsoY),
        size = Size(torsoW, torsoH),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
    // Collar detail
    drawLine(Color.White.copy(alpha = 0.6f), start = Offset(centerX - 3f * s, torsoY), end = Offset(centerX, torsoY + 4f * s), strokeWidth = 1.5f * s)
    drawLine(Color.White.copy(alpha = 0.6f), start = Offset(centerX + 3f * s, torsoY), end = Offset(centerX, torsoY + 4f * s), strokeWidth = 1.5f * s)

    // Head
    val headRadius = 8.5f * s
    val headCenter = Offset(centerX, torsoY - headRadius)
    drawCircle(Color(emp.skinTone), radius = headRadius, center = headCenter)

    // Hair
    drawArc(
        color = Color(emp.hairColor),
        startAngle = 170f,
        sweepAngle = 200f,
        useCenter = true,
        topLeft = Offset(headCenter.x - headRadius - 1f * s, headCenter.y - headRadius - 2f * s),
        size = Size((headRadius * 2f) + 2f * s, headRadius * 1.8f)
    )

    // Animated Arms and Hands Typing on Keyboard
    if (emp.state == EmployeeState.WORKING) {
        val leftBob = (sin(animTime * 9.0) * 2.5f * s).toFloat()
        val rightBob = (cos(animTime * 9.0) * 2.5f * s).toFloat()

        // Left arm -> hand
        drawLine(Color(emp.shirtColor), start = Offset(centerX - torsoW / 2f, torsoY + 5f * s), end = Offset(centerX - 6f * s, torsoY + 16f * s + leftBob), strokeWidth = 3f * s)
        drawCircle(Color(emp.skinTone), radius = 2f * s, center = Offset(centerX - 6f * s, torsoY + 17f * s + leftBob))

        // Right arm -> hand
        drawLine(Color(emp.shirtColor), start = Offset(centerX + torsoW / 2f, torsoY + 5f * s), end = Offset(centerX + 6f * s, torsoY + 16f * s + rightBob), strokeWidth = 3f * s)
        drawCircle(Color(emp.skinTone), radius = 2f * s, center = Offset(centerX + 6f * s, torsoY + 17f * s + rightBob))
    }

    // Overhead Speech/Thought Bubble with role status
    val bubbleY = headCenter.y - headRadius - 14f * s
    val bubbleR = 9f * s
    drawCircle(Color(0xFF0F172A), radius = bubbleR, center = Offset(centerX, bubbleY))
    drawCircle(Color(0xFF38BDF8), radius = bubbleR, center = Offset(centerX, bubbleY), style = Stroke(width = 1.5f * s))

    // Bubble pointer triangle down to head
    val pointer = Path().apply {
        moveTo(centerX - 3f * s, bubbleY + bubbleR - 1f * s)
        lineTo(centerX + 3f * s, bubbleY + bubbleR - 1f * s)
        lineTo(centerX, bubbleY + bubbleR + 4f * s)
        close()
    }
    drawPath(pointer, Color(0xFF0F172A))

    // Role Icon Dot
    val dotColor = when (emp.role) {
        EmployeeRole.DEVELOPER -> Color(0xFF00E5FF)
        EmployeeRole.DESIGNER -> Color(0xFFEC4899)
        EmployeeRole.QA_TESTER -> Color(0xFF10B981)
        EmployeeRole.MARKETER -> Color(0xFFF59E0B)
        EmployeeRole.SYSADMIN -> Color(0xFF38BDF8)
        EmployeeRole.PROJECT_MANAGER -> Color(0xFFA855F7)
    }
    drawCircle(dotColor, radius = 4f * s, center = Offset(centerX, bubbleY))
}

// -------------------------------------------------------------
// RECEPTION FRONT DESK
// -------------------------------------------------------------
private fun DrawScope.drawReceptionDesk(centerX: Float, centerY: Float, scale: Float, rotation: Int) {
    val s = scale
    val w = 56f * s
    val h = 34f * s
    val topY = centerY - 18f * s

    // Curved wood slat reception counter
    drawRoundRect(
        color = Color(0xFF8B5A2B),
        topLeft = Offset(centerX - w / 2f, topY),
        size = Size(w, h),
        cornerRadius = CornerRadius(8f * s, 8f * s)
    )
    // Marble transaction top ledge
    drawRoundRect(
        color = Color(0xFFF8FAFC),
        topLeft = Offset(centerX - w / 2f - 2f * s, topY - 3f * s),
        size = Size(w + 4f * s, 10f * s),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )
    // Visitor sign-in tablet
    drawRect(Color(0xFF0F172A), topLeft = Offset(centerX - 8f * s, topY - 1f * s), size = Size(8f * s, 6f * s))
    drawCircle(Color(0xFF10B981), radius = 1.5f * s, center = Offset(centerX + 12f * s, topY + 2f * s))
}

// -------------------------------------------------------------
// EXECUTIVE CONFERENCE TABLE (Oval Boardroom Table with 4 Chairs)
// -------------------------------------------------------------
private fun DrawScope.drawConferenceTable(centerX: Float, centerY: Float, scale: Float) {
    val s = scale
    val tableW = 60f * s
    val tableH = 34f * s
    val topY = centerY - 14f * s

    // 4 Surrounding conference chairs
    val chairColor = Color(0xFF1E293B)
    drawOval(chairColor, topLeft = Offset(centerX - 24f * s, topY - 12f * s), size = Size(14f * s, 10f * s))
    drawOval(chairColor, topLeft = Offset(centerX + 10f * s, topY - 12f * s), size = Size(14f * s, 10f * s))
    drawOval(chairColor, topLeft = Offset(centerX - 24f * s, topY + tableH + 2f * s), size = Size(14f * s, 10f * s))
    drawOval(chairColor, topLeft = Offset(centerX + 10f * s, topY + tableH + 2f * s), size = Size(14f * s, 10f * s))

    // Long oval mahogany boardroom table
    drawOval(
        color = Color(0xFF78350F),
        topLeft = Offset(centerX - tableW / 2f, topY),
        size = Size(tableW, tableH)
    )
    drawOval(
        color = Color(0xFF92400E),
        topLeft = Offset(centerX - tableW / 2f + 3f * s, topY + 2f * s),
        size = Size(tableW - 6f * s, tableH - 4f * s)
    )
    // Conference speakerphone puck in center
    drawCircle(Color(0xFF0F172A), radius = 4f * s, center = Offset(centerX, topY + tableH / 2f))
    drawCircle(Color(0xFF00E5FF), radius = 1.5f * s, center = Offset(centerX, topY + tableH / 2f))
}

// -------------------------------------------------------------
// IT SERVER BLADE ENCLOSURES (Vented Glass Cabinet with Blinking LEDs)
// -------------------------------------------------------------
private fun DrawScope.drawITServerCabinet(
    centerX: Float,
    centerY: Float,
    scale: Float,
    serverLoad: Float,
    hasOutage: Boolean,
    animTime: Float,
    isQuantum: Boolean
) {
    val s = scale
    val w = 32f * s
    val h = 64f * s
    val topY = centerY - h + 6f * s

    // Tall metallic server cabinet
    drawRoundRect(
        color = Color(0xFF0F172A),
        topLeft = Offset(centerX - w / 2f, topY),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )

    // Dark tinted glass front door
    drawRoundRect(
        color = Color(0xFF1E293B).copy(alpha = 0.85f),
        topLeft = Offset(centerX - w / 2f + 3f * s, topY + 4f * s),
        size = Size(w - 6f * s, h - 8f * s),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )

    // 6 Stacked blade server units
    val ledStatusColor = when {
        hasOutage -> Color(0xFFEF4444)
        serverLoad > 0.85f -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    val ledPulse = (sin(animTime * 4.0) * 0.4 + 0.6).toFloat()
    for (i in 0 until 6) {
        val by = topY + 8f * s + (i * 8.5f * s)
        drawRect(Color(0xFF334155), topLeft = Offset(centerX - w / 2f + 5f * s, by), size = Size(w - 10f * s, 6.5f * s))

        // Activity blinking LEDs
        drawCircle(
            color = ledStatusColor.copy(alpha = ledPulse),
            radius = 1.8f * s,
            center = Offset(centerX - w / 2f + 9f * s, by + 3.2f * s)
        )
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = 0.8f),
            radius = 1.4f * s,
            center = Offset(centerX - w / 2f + 14f * s, by + 3.2f * s)
        )
    }

    if (isQuantum) {
        // Cyan quantum cooling tube
        drawLine(
            color = Color(0xFF00E5FF).copy(alpha = 0.8f),
            start = Offset(centerX + w / 2f - 4f * s, topY + 6f * s),
            end = Offset(centerX + w / 2f - 4f * s, topY + h - 6f * s),
            strokeWidth = 2.5f * s
        )
    }
}

// -------------------------------------------------------------
// ESPRESSO KITCHENETTE (Countertop, Espresso Machine, Steam Wand, Cups)
// -------------------------------------------------------------
private fun DrawScope.drawEspressoKitchenette(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
    val s = scale
    val w = 36f * s
    val h = 38f * s
    val topY = centerY - h + 8f * s

    // Wooden breakroom counter base
    drawRoundRect(
        color = Color(0xFF78350F),
        topLeft = Offset(centerX - w / 2f, topY + 14f * s),
        size = Size(w, h - 14f * s),
        cornerRadius = CornerRadius(3f * s, 3f * s)
    )
    // Stainless steel countertop
    drawRect(
        color = Color(0xFFCBD5E1),
        topLeft = Offset(centerX - w / 2f - 2f * s, topY + 12f * s),
        size = Size(w + 4f * s, 4f * s)
    )

    // Italian Espresso Machine
    val machW = 20f * s
    val machH = 18f * s
    drawRoundRect(
        color = Color(0xFF475569),
        topLeft = Offset(centerX - machW / 2f, topY - 2f * s),
        size = Size(machW, machH),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )
    // Chrome group head and steam pipe
    drawRect(Color(0xFFE2E8F0), topLeft = Offset(centerX - 3f * s, topY + 4f * s), size = Size(6f * s, 6f * s))
    // Coffee cup underneath
    drawRect(Color.White, topLeft = Offset(centerX - 2.5f * s, topY + 10f * s), size = Size(5f * s, 4f * s))

    // Stack of coffee cups on top
    drawRect(Color.White, topLeft = Offset(centerX - machW / 2f + 2f * s, topY - 6f * s), size = Size(6f * s, 5f * s))
}

// -------------------------------------------------------------
// WATER COOLER DISPENSER
// -------------------------------------------------------------
private fun DrawScope.drawWaterCoolerDispenser(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
    val s = scale
    val w = 18f * s
    val h = 44f * s
    val topY = centerY - h + 6f * s

    // White floor stand
    drawRoundRect(
        color = Color(0xFFF1F5F9),
        topLeft = Offset(centerX - w / 2f, topY + 18f * s),
        size = Size(w, h - 18f * s),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )
    // Dispenser recess
    drawRect(Color(0xFF334155), topLeft = Offset(centerX - 4f * s, topY + 22f * s), size = Size(8f * s, 8f * s))
    // Red & Blue taps
    drawCircle(Color(0xFFEF4444), radius = 1.2f * s, center = Offset(centerX - 2f * s, topY + 23f * s))
    drawCircle(Color(0xFF3B82F6), radius = 1.2f * s, center = Offset(centerX + 2f * s, topY + 23f * s))

    // Clear Cyan Water Bottle on top with bubble animation
    val jugRadius = 8.5f * s
    val jugCenter = Offset(centerX, topY + 8f * s)
    drawCircle(Color(0xFF0284C7).copy(alpha = 0.85f), radius = jugRadius, center = jugCenter)
    drawCircle(Color.White.copy(alpha = 0.5f), radius = jugRadius * 0.7f, center = Offset(jugCenter.x - 2f * s, jugCenter.y - 2f * s))

    // Animated bubble
    val bubbleBob = (sin(animTime * 3.0) * 2f * s).toFloat()
    drawCircle(Color.White.copy(alpha = 0.7f), radius = 1.5f * s, center = Offset(jugCenter.x, jugCenter.y + bubbleBob))
}

// -------------------------------------------------------------
// INDOOR FICUS / MONSTERA POTTED PLANT
// -------------------------------------------------------------
private fun DrawScope.drawIndoorFicusPlant(centerX: Float, centerY: Float, scale: Float) {
    val s = scale
    val potW = 18f * s
    val potH = 16f * s
    val potTopY = centerY - potH

    // Terracotta ceramic pot
    drawRoundRect(
        color = Color(0xFFC2410C),
        topLeft = Offset(centerX - potW / 2f, potTopY),
        size = Size(potW, potH),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )
    // Pot rim
    drawRect(Color(0xFF9A3412), topLeft = Offset(centerX - potW / 2f - 1.5f * s, potTopY), size = Size(potW + 3f * s, 3f * s))

    // Lush layered green leaves
    drawCircle(Color(0xFF166534), radius = 11f * s, center = Offset(centerX, potTopY - 10f * s))
    drawCircle(Color(0xFF15803D), radius = 9f * s, center = Offset(centerX - 7f * s, potTopY - 6f * s))
    drawCircle(Color(0xFF22C55E), radius = 8f * s, center = Offset(centerX + 7f * s, potTopY - 8f * s))
    drawCircle(Color(0xFF4ADE80), radius = 6f * s, center = Offset(centerX, potTopY - 14f * s))
}

// -------------------------------------------------------------
// KANBAN AGILE WHITEBOARD
// -------------------------------------------------------------
private fun DrawScope.drawAgileWhiteboard(centerX: Float, centerY: Float, scale: Float) {
    val s = scale
    val boardW = 44f * s
    val boardH = 46f * s
    val topY = centerY - boardH + 4f * s

    // Aluminum rolling stand with caster wheels
    drawLine(Color(0xFF64748B), start = Offset(centerX - boardW / 2f, centerY + 2f * s), end = Offset(centerX - boardW / 2f, topY), strokeWidth = 3f * s)
    drawLine(Color(0xFF64748B), start = Offset(centerX + boardW / 2f, centerY + 2f * s), end = Offset(centerX + boardW / 2f, topY), strokeWidth = 3f * s)
    // Caster wheels
    drawCircle(Color(0xFF1E293B), radius = 2.5f * s, center = Offset(centerX - boardW / 2f, centerY + 2f * s))
    drawCircle(Color(0xFF1E293B), radius = 2.5f * s, center = Offset(centerX + boardW / 2f, centerY + 2f * s))

    // White dry-erase surface
    drawRoundRect(
        color = Color(0xFFF8FAFC),
        topLeft = Offset(centerX - boardW / 2f + 2f * s, topY + 2f * s),
        size = Size(boardW - 4f * s, boardH - 14f * s),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )
    drawRoundRect(
        color = Color(0xFF94A3B8),
        topLeft = Offset(centerX - boardW / 2f + 2f * s, topY + 2f * s),
        size = Size(boardW - 4f * s, boardH - 14f * s),
        cornerRadius = CornerRadius(2f * s, 2f * s),
        style = Stroke(width = 1.5f * s)
    )

    // Kanban columns: TODO | IN PROGRESS | DONE
    val colW = (boardW - 8f * s) / 3f
    drawLine(Color(0xFFE2E8F0), start = Offset(centerX - boardW / 2f + 4f * s + colW, topY + 4f * s), end = Offset(centerX - boardW / 2f + 4f * s + colW, topY + boardH - 14f * s), strokeWidth = 1f * s)
    drawLine(Color(0xFFE2E8F0), start = Offset(centerX - boardW / 2f + 4f * s + colW * 2f, topY + 4f * s), end = Offset(centerX - boardW / 2f + 4f * s + colW * 2f, topY + boardH - 14f * s), strokeWidth = 1f * s)

    // Colorful Post-it sticky notes
    drawRect(Color(0xFFFBBF24), topLeft = Offset(centerX - boardW / 2f + 6f * s, topY + 8f * s), size = Size(6f * s, 6f * s))
    drawRect(Color(0xFFF472B6), topLeft = Offset(centerX - boardW / 2f + 6f * s, topY + 16f * s), size = Size(6f * s, 6f * s))
    drawRect(Color(0xFF38BDF8), topLeft = Offset(centerX - boardW / 2f + 6f * s + colW, topY + 10f * s), size = Size(6f * s, 6f * s))
    drawRect(Color(0xFF4ADE80), topLeft = Offset(centerX - boardW / 2f + 6f * s + colW * 2f, topY + 8f * s), size = Size(6f * s, 6f * s))
}

// -------------------------------------------------------------
// TECH BOOKSHELF & STORAGE CABINET
// -------------------------------------------------------------
private fun DrawScope.drawTechBookshelf(centerX: Float, centerY: Float, scale: Float) {
    val s = scale
    val w = 36f * s
    val h = 50f * s
    val topY = centerY - h + 6f * s

    // Wooden bookshelf cabinet
    drawRoundRect(
        color = Color(0xFF78350F),
        topLeft = Offset(centerX - w / 2f, topY),
        size = Size(w, h),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )

    // 3 shelves with books
    for (shelf in 0 until 3) {
        val sy = topY + 6f * s + (shelf * 14f * s)
        drawRect(Color(0xFF451A03), topLeft = Offset(centerX - w / 2f + 3f * s, sy + 9f * s), size = Size(w - 6f * s, 2f * s))

        // Row of colorful book spines
        val bookColors = listOf(Color(0xFF0284C7), Color(0xFFDC2626), Color(0xFF16A34A), Color(0xFFD97706), Color(0xFF7C3AED))
        for (b in 0 until 5) {
            val bx = centerX - w / 2f + 4f * s + (b * 5.5f * s)
            drawRect(bookColors[b % bookColors.size], topLeft = Offset(bx, sy), size = Size(4.5f * s, 9f * s))
        }
    }
}

// -------------------------------------------------------------
// RETRO ARCADE MACHINE
// -------------------------------------------------------------
private fun DrawScope.drawArcadeCabinet(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
    val s = scale
    val w = 30f * s
    val h = 56f * s
    val topY = centerY - h + 6f * s

    // Arcade wooden cabinet
    drawRoundRect(
        color = Color(0xFF581C87),
        topLeft = Offset(centerX - w / 2f, topY),
        size = Size(w, h),
        cornerRadius = CornerRadius(4f * s, 4f * s)
    )

    // Glowing retro marquee art on top
    drawRoundRect(
        color = Color(0xFFEC4899),
        topLeft = Offset(centerX - w / 2f + 3f * s, topY + 3f * s),
        size = Size(w - 6f * s, 8f * s),
        cornerRadius = CornerRadius(2f * s, 2f * s)
    )

    // Glowing CRT Game Screen
    val screenPulse = (sin(animTime * 6.0) * 0.3 + 0.7).toFloat()
    drawRect(
        color = Color(0xFF00E5FF).copy(alpha = screenPulse),
        topLeft = Offset(centerX - w / 2f + 4f * s, topY + 14f * s),
        size = Size(w - 8f * s, 18f * s)
    )

    // Control panel ledge with joystick & buttons
    drawRect(Color(0xFF1E1B4B), topLeft = Offset(centerX - w / 2f + 2f * s, topY + 33f * s), size = Size(w - 4f * s, 8f * s))
    // Joystick
    drawCircle(Color(0xFFEF4444), radius = 2f * s, center = Offset(centerX - 6f * s, topY + 36f * s))
    // Buttons
    drawCircle(Color(0xFFFBBF24), radius = 1.2f * s, center = Offset(centerX + 3f * s, topY + 36f * s))
    drawCircle(Color(0xFF38BDF8), radius = 1.2f * s, center = Offset(centerX + 7f * s, topY + 36f * s))
}
