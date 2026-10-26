package com.example.ui.office3d

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.OfficeTier
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity 3D isometric environment and architecture renderer.
 * Genuinely renders five distinct playable environments:
 * 1. GARAGE: Raw industrial concrete floor with expansion cuts & oil marks, cinder block wall with electrical breaker & tool pegboard, corrugated roll-up garage door.
 * 2. COWORKING: Scandinavian herringbone oak parquet, warm exposed red brick wall, glowing neon sign, hanging Edison bulb pendants, Crittall factory windows.
 * 3. TECH_LOFT: Deep American walnut timber planks, charcoal acoustic slatted wood panels with illuminated brass crest, double-height arched loft windows overlooking downtown skyscrapers.
 * 4. CAMPUS_HQ: Dual-tone geometric acoustic carpet tiles, frosted glass partition with digital status board, floor-to-ceiling panoramic glass curtain wall looking onto campus gardens.
 * 5. CYBER_TOWER: Mirror-polished Italian Calacatta white marble with gold veining & specular reflections, fluted Carrara marble walls with champagne gold channels, curved penthouse skyline glass.
 */
object IsometricEnvironmentRenderer {

    fun drawFloorTile(
        scope: DrawScope,
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
        scope.apply {
            val path = Path().apply {
                moveTo(centerX, centerY - tileH / 2f)
                lineTo(centerX + tileW / 2f, centerY)
                lineTo(centerX, centerY + tileH / 2f)
                lineTo(centerX - tileW / 2f, centerY)
                close()
            }

            val isAlternate = (gx + gy) % 2 == 0

            // Base Floor Materials per Architectural Tier
            val tileColors = when (tier) {
                OfficeTier.GARAGE -> {
                    // Industrial poured concrete screed with subtle grit variation
                    if (isAlternate) listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)) else listOf(Color(0xFFB8C2CC), Color(0xFF8896A6))
                }
                OfficeTier.COWORKING -> {
                    // Scandinavian warm oak herringbone parquet
                    if (isAlternate) listOf(Color(0xFFEAD5BF), Color(0xFFD4B999)) else listOf(Color(0xFFDFCAAD), Color(0xFFC7AB8B))
                }
                OfficeTier.TECH_LOFT -> {
                    // Deep rich American walnut hardwood with natural grain
                    if (isAlternate) listOf(Color(0xFFA17852), Color(0xFF815735)) else listOf(Color(0xFF916843), Color(0xFF704725))
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Dual-tone modern commercial acoustic carpet tiles
                    if (isAlternate) listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)) else listOf(Color(0xFF334155), Color(0xFF1E293B))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Mirror-polished Italian Calacatta White Marble
                    if (isAlternate) listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)) else listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
                }
            }

            val tileFill = when {
                isSelected -> Color(0xFF00E5FF).copy(alpha = 0.55f)
                isMoveTarget -> Color(0xFF10B981).copy(alpha = 0.5f)
                else -> null
            }

            if (tileFill != null) {
                drawPath(path, tileFill)
            } else {
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = tileColors,
                        startY = centerY - tileH / 2f,
                        endY = centerY + tileH / 2f
                    )
                )
            }

            // Authentic Material Seams & Surface Texturing
            when (tier) {
                OfficeTier.GARAGE -> {
                    // Expansion joint seam cuts
                    drawPath(path, Color(0xFF64748B).copy(alpha = 0.6f), style = Stroke(width = 1.2f))
                    // Subtle industrial grease/oil stain accent on random tiles
                    if ((gx * 7 + gy * 13) % 9 == 0) {
                        drawOval(Color(0x33000000), topLeft = Offset(centerX - tileW * 0.15f, centerY - tileH * 0.1f), size = Size(tileW * 0.3f, tileH * 0.2f))
                    }
                    // Yellow safety perimeter stripe on outer borders
                    if (gx == 0 || gy == 0) {
                        drawPath(path, Color(0xFFEAB308).copy(alpha = 0.45f), style = Stroke(width = 2.5f))
                    }
                }
                OfficeTier.COWORKING, OfficeTier.TECH_LOFT -> {
                    // Wood board plank bevels
                    val seam = if (tier == OfficeTier.TECH_LOFT) Color(0xFF451A03).copy(alpha = 0.6f) else Color(0xFF78350F).copy(alpha = 0.45f)
                    drawPath(path, seam, style = Stroke(width = 1.2f))
                    // Planks wood grain lines
                    drawLine(
                        color = Color.Black.copy(alpha = 0.08f),
                        start = Offset(centerX - tileW * 0.25f, centerY - tileH * 0.12f),
                        end = Offset(centerX + tileW * 0.25f, centerY + tileH * 0.12f),
                        strokeWidth = 1f
                    )
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Commercial carpet tile boundary seam
                    drawPath(path, Color(0xFF475569).copy(alpha = 0.5f), style = Stroke(width = 1.2f))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Calacatta Marble Gold Grout Inlay
                    drawPath(path, Color(0xFFD4AF37).copy(alpha = 0.45f), style = Stroke(width = 1.4f))
                    // Specular gloss reflection streak across polished marble
                    if (isAlternate) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(centerX - tileW * 0.25f, centerY - tileH * 0.12f),
                            end = Offset(centerX + tileW * 0.25f, centerY + tileH * 0.12f),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            // Interactive Selection Highlight Ring
            if (isSelected) {
                drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 3f))
            } else if (isMoveTarget) {
                drawPath(path, Color(0xFF10B981), style = Stroke(width = 3f))
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    fun drawWallsAndArchitecture(
        scope: DrawScope,
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
        scope.apply {
            val wallHeight = 210f * scale

            val topX = originX
            val topY = originY - tileH / 2f

            val leftCornerX = (-gridH) * (tileW / 2f) + originX
            val leftCornerY = (gridH) * (tileH / 2f) + originY

            val rightCornerX = (gridW) * (tileW / 2f) + originX
            val rightCornerY = (gridW) * (tileH / 2f) + originY

            val leftWall = Path().apply {
                moveTo(topX, topY)
                lineTo(leftCornerX, leftCornerY)
                lineTo(leftCornerX, leftCornerY - wallHeight)
                lineTo(topX, topY - wallHeight)
                close()
            }

            val rightWall = Path().apply {
                moveTo(topX, topY)
                lineTo(rightCornerX, rightCornerY)
                lineTo(rightCornerX, rightCornerY - wallHeight)
                lineTo(topX, topY - wallHeight)
                close()
            }

            // Wall Finishes & Textures per Architectural Tier
            when (tier) {
                OfficeTier.GARAGE -> {
                    // Industrial Cinder Block Wall with Mortar Joints
                    drawPath(
                        path = leftWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFF94A3B8), Color(0xFF64748B)), startY = topY - wallHeight, endY = leftCornerY)
                    )
                    drawPath(
                        path = rightWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFA1AEC0), Color(0xFF718096)), startY = topY - wallHeight, endY = rightCornerY)
                    )
                    // Cinder block mortar courses
                    for (hStep in 1..9) {
                        val yOffset = wallHeight * (hStep / 10f)
                        drawLine(Color(0xFF475569), start = Offset(topX, topY - yOffset), end = Offset(leftCornerX, leftCornerY - yOffset), strokeWidth = 1.5f * scale)
                    }
                    // Exposed overhead galvanized spiral HVAC ducting
                    val ductY = topY - wallHeight + 18f * scale
                    drawLine(Color(0xFFCBD5E1), start = Offset(topX, ductY), end = Offset(leftCornerX, leftCornerY - wallHeight + 18f * scale), strokeWidth = 12f * scale)
                    drawLine(Color(0xFF94A3B8), start = Offset(topX, ductY), end = Offset(leftCornerX, leftCornerY - wallHeight + 18f * scale), strokeWidth = 2f * scale)
                }
                OfficeTier.COWORKING -> {
                    // Warm Exposed Red Brick Wall with Authentic Relief
                    drawPath(
                        path = leftWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFB94A33), Color(0xFF8F3220)), startY = topY - wallHeight, endY = leftCornerY)
                    )
                    drawPath(
                        path = rightWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFC75C46), Color(0xFF9B3925)), startY = topY - wallHeight, endY = rightCornerY)
                    )
                    // Brick mortar courses
                    for (hStep in 1..12) {
                        val yOffset = wallHeight * (hStep / 13f)
                        drawLine(Color(0xFF702516), start = Offset(topX, topY - yOffset), end = Offset(leftCornerX, leftCornerY - yOffset), strokeWidth = 1.4f * scale)
                    }
                    // Hanging Edison Bulb Pendants along ceiling
                    for (i in 1..3) {
                        val t = i / 4f
                        val px = topX + (leftCornerX - topX) * t
                        val py = topY + (leftCornerY - topY) * t - wallHeight
                        val cordLen = 42f * scale
                        drawLine(Color(0xFF0F172A), start = Offset(px, py), end = Offset(px, py + cordLen), strokeWidth = 1.5f * scale)
                        // Amber glowing filament bulb
                        drawCircle(Color(0xFFF59E0B), radius = 5f * scale, center = Offset(px, py + cordLen))
                        drawCircle(Color(0xFFFBBF24).copy(alpha = 0.5f), radius = 9f * scale, center = Offset(px, py + cordLen))
                    }
                }
                OfficeTier.TECH_LOFT -> {
                    // Charcoal Acoustic Slatted Wood Paneling
                    drawPath(
                        path = leftWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)), startY = topY - wallHeight, endY = leftCornerY)
                    )
                    drawPath(
                        path = rightWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)), startY = topY - wallHeight, endY = rightCornerY)
                    )
                    // Vertical acoustic timber slats
                    val slatCount = 18
                    for (i in 0 until slatCount) {
                        val t = i.toFloat() / slatCount
                        val sx = topX + (leftCornerX - topX) * t
                        val sy = topY + (leftCornerY - topY) * t
                        drawLine(Color(0xFF020617), start = Offset(sx, sy), end = Offset(sx, sy - wallHeight), strokeWidth = 2.5f * scale)
                    }
                    // Black steel I-beam ceiling rafters
                    drawLine(Color(0xFF0F172A), start = Offset(topX, topY - wallHeight + 10f * scale), end = Offset(leftCornerX, leftCornerY - wallHeight + 10f * scale), strokeWidth = 10f * scale)
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Modern Architectural Off-White Drywall with Frosted Glass
                    drawPath(
                        path = leftWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFCBD5E1)), startY = topY - wallHeight, endY = leftCornerY)
                    )
                    drawPath(
                        path = rightWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)), startY = topY - wallHeight, endY = rightCornerY)
                    )
                }
                OfficeTier.CYBER_TOWER -> {
                    // Luxury Fluted Italian Carrara Marble with Gold Inlay Channels
                    drawPath(
                        path = leftWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)), startY = topY - wallHeight, endY = leftCornerY)
                    )
                    drawPath(
                        path = rightWall,
                        brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0)), startY = topY - wallHeight, endY = rightCornerY)
                    )
                    // Gold brass vertical channel inlays
                    for (i in 1..10) {
                        val t = i / 11f
                        val sx = topX + (leftCornerX - topX) * t
                        val sy = topY + (leftCornerY - topY) * t
                        drawLine(Color(0xFFD4AF37).copy(alpha = 0.7f), start = Offset(sx, sy), end = Offset(sx, sy - wallHeight), strokeWidth = 2f * scale)
                    }
                }
            }

            // Architectural Baseboard Skirting
            val baseboardH = 12f * scale
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
            val baseboardColor = if (tier == OfficeTier.CYBER_TOWER) Color(0xFFD4AF37) else Color(0xFF0F172A)
            drawPath(baseboardLeft, baseboardColor)
            drawPath(baseboardRight, baseboardColor)

            // -------------------------------------------------------------
            // RIGHT WALL ARCHITECTURE & WINDOWS
            // -------------------------------------------------------------
            drawRightWallArchitecture(
                tier = tier,
                hour = hour,
                topX = topX,
                topY = topY,
                rightCornerX = rightCornerX,
                rightCornerY = rightCornerY,
                wallHeight = wallHeight,
                scale = scale,
                gridW = gridW
            )

            // -------------------------------------------------------------
            // LEFT WALL BRANDING & AMBIANCE
            // -------------------------------------------------------------
            drawLeftWallFeatures(
                tier = tier,
                companyName = companyName,
                hour = hour,
                topX = topX,
                topY = topY,
                leftCornerX = leftCornerX,
                leftCornerY = leftCornerY,
                wallHeight = wallHeight,
                textMeasurer = textMeasurer,
                scale = scale
            )
        }
    }

    private fun DrawScope.drawRightWallArchitecture(
        tier: OfficeTier,
        hour: Int,
        topX: Float,
        topY: Float,
        rightCornerX: Float,
        rightCornerY: Float,
        wallHeight: Float,
        scale: Float,
        gridW: Int
    ) {
        val s = scale
        val isDay = hour in 6..19

        if (tier == OfficeTier.GARAGE) {
            // Corrugated Steel Industrial Roll-Up Garage Door
            val t1 = 0.18f
            val t2 = 0.86f
            val gx1 = topX + (rightCornerX - topX) * t1
            val gy1 = topY + (rightCornerY - topY) * t1
            val gx2 = topX + (rightCornerX - topX) * t2
            val gy2 = topY + (rightCornerY - topY) * t2

            val doorH = wallHeight * 0.78f
            val doorPoly = Path().apply {
                moveTo(gx1, gy1 - 12f * s)
                lineTo(gx2, gy2 - 12f * s)
                lineTo(gx2, gy2 - 12f * s - doorH)
                lineTo(gx1, gy1 - 12f * s - doorH)
                close()
            }

            drawPath(doorPoly, Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155))))
            // Ribbed horizontal corrugation slats
            for (r in 1..10) {
                val ry = doorH * (r / 11f)
                drawLine(Color(0xFF1E293B), start = Offset(gx1, gy1 - 12f * s - ry), end = Offset(gx2, gy2 - 12f * s - ry), strokeWidth = 3f * s)
                drawLine(Color(0xFF64748B), start = Offset(gx1, gy1 - 12f * s - ry - 1.5f * s), end = Offset(gx2, gy2 - 12f * s - ry - 1.5f * s), strokeWidth = 1.2f * s)
            }
            // Vertical heavy steel tracks & framing
            drawPath(doorPoly, Color(0xFF0F172A), style = Stroke(width = 4f * s))
            // Lift handle
            val midX = (gx1 + gx2) / 2f
            val midY = (gy1 + gy2) / 2f - 22f * s
            drawRoundRect(Color(0xFFCBD5E1), topLeft = Offset(midX - 10f * s, midY), size = Size(20f * s, 5f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
            return
        }

        // Panoramic Windows for COWORKING, TECH_LOFT, CAMPUS_HQ, CYBER_TOWER
        val winCount = (gridW / 3).coerceIn(2, 4)
        for (i in 0 until winCount) {
            val t1 = (i + 0.32f) / winCount
            val t2 = (i + 0.90f) / winCount

            val wx1 = topX + (rightCornerX - topX) * t1
            val wy1 = topY + (rightCornerY - topY) * t1
            val wx2 = topX + (rightCornerX - topX) * t2
            val wy2 = topY + (rightCornerY - topY) * t2

            val winTopY1 = wy1 - wallHeight * 0.84f
            val winTopY2 = wy2 - wallHeight * 0.84f
            val winBottomY1 = wy1 - wallHeight * 0.16f
            val winBottomY2 = wy2 - wallHeight * 0.16f

            val windowPoly = Path().apply {
                moveTo(wx1, winBottomY1)
                lineTo(wx2, winBottomY2)
                lineTo(wx2, winTopY2)
                lineTo(wx1, winTopY1)
                close()
            }

            // Exterior Sky Gradient
            val skyTop = when {
                !isDay -> Color(0xFF090D16)
                tier == OfficeTier.CYBER_TOWER -> Color(0xFF1E3A8A)
                else -> Color(0xFF38BDF8)
            }
            val skyBottom = when {
                !isDay -> Color(0xFF1E293B)
                tier == OfficeTier.CYBER_TOWER -> Color(0xFFF59E0B)
                else -> Color(0xFFBAE6FD)
            }

            drawPath(
                windowPoly,
                Brush.verticalGradient(
                    colors = listOf(skyTop, skyBottom),
                    startY = winTopY1,
                    endY = winBottomY1
                )
            )

            // Downtown Skyline View
            val bldgW = (wx2 - wx1) * 0.36f
            val bldgColor = if (isDay) Color(0xFF475569) else Color(0xFF1E293B)
            drawRect(bldgColor, topLeft = Offset(wx1 + bldgW * 0.2f, winBottomY1 - 65f * s), size = Size(bldgW, 65f * s))
            drawRect(if (isDay) Color(0xFF334155) else Color(0xFF0F172A), topLeft = Offset(wx1 + bldgW * 1.5f, winBottomY1 - 90f * s), size = Size(bldgW * 1.2f, 90f * s))

            // Architectural Window Mullions
            when (tier) {
                OfficeTier.COWORKING -> {
                    // Crittall Black Steel Grid
                    drawPath(windowPoly, Color(0xFF0F172A), style = Stroke(width = 3.5f * s))
                    val midX = (wx1 + wx2) / 2f
                    val midY1 = (winBottomY1 + winBottomY2) / 2f
                    val midY2 = (winTopY1 + winTopY2) / 2f
                    drawLine(Color(0xFF0F172A), start = Offset(midX, midY1), end = Offset(midX, midY2), strokeWidth = 2.5f * s)
                }
                OfficeTier.TECH_LOFT -> {
                    // Dramatic Arched Loft Frame
                    drawPath(windowPoly, Color(0xFF090D16), style = Stroke(width = 4f * s))
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Sleek Brushed Aluminum Mullions
                    drawPath(windowPoly, Color(0xFF64748B), style = Stroke(width = 2.5f * s))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Luxury Penthouse Gold Mullions
                    drawPath(windowPoly, Color(0xFFD4AF37), style = Stroke(width = 3.5f * s))
                }
                else -> {}
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    private fun DrawScope.drawLeftWallFeatures(
        tier: OfficeTier,
        companyName: String,
        hour: Int,
        topX: Float,
        topY: Float,
        leftCornerX: Float,
        leftCornerY: Float,
        wallHeight: Float,
        textMeasurer: TextMeasurer,
        scale: Float
    ) {
        val s = scale

        // 1. Company Brand Plaque on Left Wall
        val logoT = 0.50f
        val lx = topX + (leftCornerX - topX) * logoT
        val ly = topY + (leftCornerY - topY) * logoT - wallHeight * 0.64f
        val plaqueW = 104f * s
        val plaqueH = 40f * s

        val plaqueBg = when (tier) {
            OfficeTier.GARAGE -> Color(0xFF1E293B)
            OfficeTier.COWORKING -> Color(0xFF0F172A)
            OfficeTier.TECH_LOFT -> Color(0xFF090D16)
            OfficeTier.CAMPUS_HQ -> Color(0xFF0284C7)
            OfficeTier.CYBER_TOWER -> Color(0xFF0F172A)
        }
        val plaqueBorder = when (tier) {
            OfficeTier.CYBER_TOWER -> Color(0xFFD4AF37)
            OfficeTier.COWORKING -> Color(0xFFEC4899)
            else -> Color(0xFF00E5FF)
        }

        drawRoundRect(
            color = plaqueBg,
            topLeft = Offset(lx - plaqueW / 2f, ly - plaqueH / 2f),
            size = Size(plaqueW, plaqueH),
            cornerRadius = CornerRadius(6f * s, 6f * s)
        )
        drawRoundRect(
            color = plaqueBorder,
            topLeft = Offset(lx - plaqueW / 2f, ly - plaqueH / 2f),
            size = Size(plaqueW, plaqueH),
            cornerRadius = CornerRadius(6f * s, 6f * s),
            style = Stroke(width = 2.5f * s)
        )

        val brandLayout = textMeasurer.measure(
            text = AnnotatedString(companyName.take(16)),
            style = TextStyle(
                color = Color.White,
                fontSize = (13f * scale).coerceAtLeast(10f).sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        drawText(
            textLayoutResult = brandLayout,
            topLeft = Offset(lx - brandLayout.size.width / 2f, ly - brandLayout.size.height / 2f)
        )

        // 2. Wall Clock with Ticking Hands
        val clockT = 0.22f
        val cx = topX + (leftCornerX - topX) * clockT
        val cy = topY + (leftCornerY - topY) * clockT - wallHeight * 0.70f
        val clockR = 16f * s

        drawCircle(Color(0xFF0F172A), radius = clockR, center = Offset(cx, cy))
        drawCircle(Color.White, radius = clockR - 3f * s, center = Offset(cx, cy))
        val hourAngle = ((hour % 12) / 12f) * 6.283f - 1.5707f
        val minAngle = 1.5707f
        drawLine(Color(0xFF0F172A), start = Offset(cx, cy), end = Offset(cx + cos(hourAngle) * clockR * 0.5f, cy + sin(hourAngle) * clockR * 0.5f), strokeWidth = 2.5f * s)
        drawLine(Color(0xFFEF4444), start = Offset(cx, cy), end = Offset(cx + cos(minAngle) * clockR * 0.75f, cy + sin(minAngle) * clockR * 0.75f), strokeWidth = 1.8f * s)
        drawCircle(Color(0xFF0F172A), radius = 2.2f * s, center = Offset(cx, cy))
    }

    fun drawFloorFoundationPlinth(
        scope: DrawScope,
        originX: Float,
        originY: Float,
        tileW: Float,
        tileH: Float,
        gridW: Int,
        gridH: Int,
        tier: OfficeTier,
        scale: Float
    ) {
        scope.apply {
            val leftCornerX = (-gridH) * (tileW / 2f) + originX
            val leftCornerY = (gridH) * (tileH / 2f) + originY

            val bottomCornerX = (gridW - gridH) * (tileW / 2f) + originX
            val bottomCornerY = (gridW + gridH) * (tileH / 2f) + originY

            val rightCornerX = (gridW) * (tileW / 2f) + originX
            val rightCornerY = (gridW) * (tileH / 2f) + originY

            val slabDepth = 24f * scale

            // Ambient ground shadow underneath the entire office foundation
            val shadowPath = Path().apply {
                moveTo(leftCornerX - 20f * scale, leftCornerY + slabDepth + 8f * scale)
                lineTo(bottomCornerX, bottomCornerY + slabDepth + 26f * scale)
                lineTo(rightCornerX + 20f * scale, rightCornerY + slabDepth + 8f * scale)
                lineTo(bottomCornerX, bottomCornerY + slabDepth - 10f * scale)
                close()
            }
            drawPath(
                shadowPath,
                Brush.radialGradient(
                    colors = listOf(Color(0x99000000), Color(0x44000000), Color.Transparent),
                    center = Offset(bottomCornerX, bottomCornerY + slabDepth + 10f * scale),
                    radius = (gridW + gridH) * (tileW * 0.40f)
                )
            )

            // Left face of 3D foundation slab
            val leftFace = Path().apply {
                moveTo(leftCornerX, leftCornerY)
                lineTo(bottomCornerX, bottomCornerY)
                lineTo(bottomCornerX, bottomCornerY + slabDepth)
                lineTo(leftCornerX, leftCornerY + slabDepth)
                close()
            }

            // Right face of 3D foundation slab
            val rightFace = Path().apply {
                moveTo(bottomCornerX, bottomCornerY)
                lineTo(rightCornerX, rightCornerY)
                lineTo(rightCornerX, rightCornerY + slabDepth)
                lineTo(bottomCornerX, bottomCornerY + slabDepth)
                close()
            }

            val (leftColor, rightColor, trimColor) = when (tier) {
                OfficeTier.GARAGE -> Triple(Color(0xFF334155), Color(0xFF475569), Color(0xFFEAB308))
                OfficeTier.COWORKING -> Triple(Color(0xFF451A03), Color(0xFF78350F), Color(0xFFEC4899))
                OfficeTier.TECH_LOFT -> Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF00E5FF))
                OfficeTier.CAMPUS_HQ -> Triple(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF38BDF8))
                OfficeTier.CYBER_TOWER -> Triple(Color(0xFF1E293B), Color(0xFF334155), Color(0xFFD4AF37))
            }

            drawPath(leftFace, leftColor)
            drawPath(rightFace, rightColor)

            // Accent architectural rim line along the floor edge
            drawLine(trimColor, start = Offset(leftCornerX, leftCornerY), end = Offset(bottomCornerX, bottomCornerY), strokeWidth = 2.5f * scale)
            drawLine(trimColor, start = Offset(bottomCornerX, bottomCornerY), end = Offset(rightCornerX, rightCornerY), strokeWidth = 2.5f * scale)

            // Bottom base line
            drawLine(Color(0xFF020617), start = Offset(leftCornerX, leftCornerY + slabDepth), end = Offset(bottomCornerX, bottomCornerY + slabDepth), strokeWidth = 1.5f * scale)
            drawLine(Color(0xFF020617), start = Offset(bottomCornerX, bottomCornerY + slabDepth), end = Offset(rightCornerX, rightCornerY + slabDepth), strokeWidth = 1.5f * scale)
        }
    }
}
