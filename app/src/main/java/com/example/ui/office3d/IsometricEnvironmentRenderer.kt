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
 * Renders five genuinely different architectural environments:
 * 1. GARAGE: Raw industrial concrete, cinder block mortar wall, breaker box, tools, corrugated roll-up garage door.
 * 2. COWORKING: Scandinavian herringbone parquet, exposed red brick, glowing neon sign, Crittall factory windows, Edison bulbs.
 * 3. TECH_LOFT: Polished dark walnut planks, charcoal acoustic slatted wall with brass plaque, arched loft windows with city skyline.
 * 4. CAMPUS_HQ: Geometric acoustic carpet tiles, frosted glass partition with digital status board, floor-to-ceiling garden curtain wall.
 * 5. CYBER_TOWER: Polished Calacatta marble with gold veining, fluted marble wall with holographic crest, curved penthouse skyline glass.
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

            val baseColor = when (tier) {
                OfficeTier.GARAGE -> {
                    // Poured industrial concrete slab
                    if (isAlternate) Color(0xFFC7CBD1) else Color(0xFFBDC2C9)
                }
                OfficeTier.COWORKING -> {
                    // Scandinavian warm oak parquet
                    if (isAlternate) Color(0xFFE4CBB4) else Color(0xFFD8BC9E)
                }
                OfficeTier.TECH_LOFT -> {
                    // Deep rich American walnut hardwood
                    if (isAlternate) Color(0xFF936946) else Color(0xFF815735)
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Modern acoustic carpet tiles
                    if (isAlternate) Color(0xFFCBD5E1) else Color(0xFFB0BDCF)
                }
                OfficeTier.CYBER_TOWER -> {
                    // Polished Italian Calacatta White Marble
                    if (isAlternate) Color(0xFFF8FAFC) else Color(0xFFEEF2F6)
                }
            }

            val tileFill = when {
                isSelected -> Color(0xFF00E5FF).copy(alpha = 0.45f)
                isMoveTarget -> Color(0xFF10B981).copy(alpha = 0.4f)
                else -> baseColor
            }

            drawPath(path, tileFill)

            // Floor texture details based on tier
            when (tier) {
                OfficeTier.GARAGE -> {
                    // Expansion joint seam lines
                    val seamColor = Color(0xFF94A3B8).copy(alpha = 0.45f)
                    drawPath(path, seamColor, style = Stroke(width = 1f))
                    // Subtle yellow caution perimeter border on edge tiles
                    if (gx == 0 || gy == 0) {
                        drawPath(path, Color(0xFFEAB308).copy(alpha = 0.35f), style = Stroke(width = 2f))
                    }
                }
                OfficeTier.COWORKING, OfficeTier.TECH_LOFT -> {
                    // Wood board plank seams
                    val woodSeamColor = Color(0xFF5A381E).copy(alpha = 0.4f)
                    drawPath(path, woodSeamColor, style = Stroke(width = 1f))
                }
                OfficeTier.CAMPUS_HQ -> {
                    // High-end commercial carpet tile seams
                    drawPath(path, Color(0xFF64748B).copy(alpha = 0.4f), style = Stroke(width = 1f))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Marble grout seam with golden/specular bevel
                    drawPath(path, Color(0xFFD4AF37).copy(alpha = 0.3f), style = Stroke(width = 1.2f))
                    // Specular gloss reflection diagonal streak
                    if (isAlternate) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.25f),
                            start = Offset(centerX - tileW * 0.2f, centerY - tileH * 0.1f),
                            end = Offset(centerX + tileW * 0.2f, centerY + tileH * 0.1f),
                            strokeWidth = 2.5f
                        )
                    }
                }
            }

            // Interactive highlights
            if (isSelected) {
                drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 2.5f))
            } else if (isMoveTarget) {
                drawPath(path, Color(0xFF10B981), style = Stroke(width = 2.5f))
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
            val wallHeight = 165f * scale

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

            // Wall base finishes per tier
            when (tier) {
                OfficeTier.GARAGE -> {
                    // Cinder block / industrial grey drywall
                    drawPath(leftWall, Color(0xFF94A3B8))
                    drawPath(rightWall, Color(0xFFA1AEC0))
                    // Mortar horizontal courses
                    for (hStep in 1..8) {
                        val yOffset = wallHeight * (hStep / 9f)
                        drawLine(Color(0xFF64748B), start = Offset(topX, topY - yOffset), end = Offset(leftCornerX, leftCornerY - yOffset), strokeWidth = 1f * scale)
                    }
                }
                OfficeTier.COWORKING -> {
                    // Exposed warm red brick wall
                    drawPath(leftWall, Color(0xFFB94A33))
                    drawPath(rightWall, Color(0xFFC75C46))
                    // Brick courses
                    for (hStep in 1..10) {
                        val yOffset = wallHeight * (hStep / 11f)
                        drawLine(Color(0xFF8F3220), start = Offset(topX, topY - yOffset), end = Offset(leftCornerX, leftCornerY - yOffset), strokeWidth = 1.2f * scale)
                    }
                }
                OfficeTier.TECH_LOFT -> {
                    // Charcoal slatted acoustic wood paneling
                    drawPath(leftWall, Color(0xFF1E293B))
                    drawPath(rightWall, Color(0xFF334155))
                    // Slats
                    val slatCount = 14
                    for (i in 0 until slatCount) {
                        val t = i.toFloat() / slatCount
                        val sx = topX + (leftCornerX - topX) * t
                        val sy = topY + (leftCornerY - topY) * t
                        drawLine(Color(0xFF0F172A), start = Offset(sx, sy), end = Offset(sx, sy - wallHeight), strokeWidth = 2f * scale)
                    }
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Modern matte architectural off-white drywall
                    drawPath(leftWall, Color(0xFFE2E8F0))
                    drawPath(rightWall, Color(0xFFF1F5F9))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Luxury fluted Italian Carrara marble with gold trim
                    drawPath(leftWall, Color(0xFFF8FAFC))
                    drawPath(rightWall, Color(0xFFFFFFFF))
                    // Gold vertical channel lines
                    for (i in 1..8) {
                        val t = i / 9f
                        val sx = topX + (leftCornerX - topX) * t
                        val sy = topY + (leftCornerY - topY) * t
                        drawLine(Color(0xFFD4AF37).copy(alpha = 0.6f), start = Offset(sx, sy), end = Offset(sx, sy - wallHeight), strokeWidth = 1.5f * scale)
                    }
                }
            }

            // Wall architectural skirting baseboard
            val baseboardH = 9f * scale
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
            val baseboardColor = if (tier == OfficeTier.CYBER_TOWER) Color(0xFFD4AF37) else Color(0xFF1E293B)
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
            // Industrial roll-up corrugated steel garage door
            val t1 = 0.2f
            val t2 = 0.85f
            val gx1 = topX + (rightCornerX - topX) * t1
            val gy1 = topY + (rightCornerY - topY) * t1
            val gx2 = topX + (rightCornerX - topX) * t2
            val gy2 = topY + (rightCornerY - topY) * t2

            val doorH = wallHeight * 0.75f
            val doorPoly = Path().apply {
                moveTo(gx1, gy1 - 10f * s)
                lineTo(gx2, gy2 - 10f * s)
                lineTo(gx2, gy2 - 10f * s - doorH)
                lineTo(gx1, gy1 - 10f * s - doorH)
                close()
            }

            drawPath(doorPoly, Color(0xFF475569))
            // Corrugated steel horizontal ribs
            for (r in 1..9) {
                val ry = doorH * (r / 10f)
                drawLine(Color(0xFF334155), start = Offset(gx1, gy1 - 10f * s - ry), end = Offset(gx2, gy2 - 10f * s - ry), strokeWidth = 2.5f * s)
            }
            // Vertical steel guide tracks & frame
            drawPath(doorPoly, Color(0xFF1E293B), style = Stroke(width = 3.5f * s))
            // Lift handle
            val midX = (gx1 + gx2) / 2f
            val midY = (gy1 + gy2) / 2f - 18f * s
            drawRoundRect(Color(0xFFE2E8F0), topLeft = Offset(midX - 8f * s, midY), size = Size(16f * s, 4f * s), cornerRadius = CornerRadius(1f * s, 1f * s))
            return
        }

        // Panoramic windows for COWORKING, TECH_LOFT, CAMPUS_HQ, CYBER_TOWER
        val winCount = (gridW / 3).coerceIn(2, 4)
        for (i in 0 until winCount) {
            val t1 = (i + 0.35f) / winCount
            val t2 = (i + 0.88f) / winCount

            val wx1 = topX + (rightCornerX - topX) * t1
            val wy1 = topY + (rightCornerY - topY) * t1
            val wx2 = topX + (rightCornerX - topX) * t2
            val wy2 = topY + (rightCornerY - topY) * t2

            val winTopY1 = wy1 - wallHeight * 0.82f
            val winTopY2 = wy2 - wallHeight * 0.82f
            val winBottomY1 = wy1 - wallHeight * 0.18f
            val winBottomY2 = wy2 - wallHeight * 0.18f

            val windowPoly = Path().apply {
                moveTo(wx1, winBottomY1)
                lineTo(wx2, winBottomY2)
                lineTo(wx2, winTopY2)
                lineTo(wx1, winTopY1)
                close()
            }

            // Sky gradient through glass
            val skyTop = when {
                !isDay -> Color(0xFF090D16)
                tier == OfficeTier.CYBER_TOWER -> Color(0xFF1E3A8A) // Deep stratospheric azure
                else -> Color(0xFF38BDF8)
            }
            val skyBottom = when {
                !isDay -> Color(0xFF1E293B)
                tier == OfficeTier.CYBER_TOWER -> Color(0xFFF59E0B) // Golden hour sunset horizon
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

            // Skyline view through window
            val bldgW = (wx2 - wx1) * 0.35f
            val bldgColor = if (isDay) Color(0xFF475569) else Color(0xFF1E293B)
            drawRect(bldgColor, topLeft = Offset(wx1 + bldgW * 0.2f, winBottomY1 - 50f * s), size = Size(bldgW, 50f * s))
            drawRect(if (isDay) Color(0xFF334155) else Color(0xFF0F172A), topLeft = Offset(wx1 + bldgW * 1.5f, winBottomY1 - 70f * s), size = Size(bldgW * 1.2f, 70f * s))

            // Window frames per architectural style
            when (tier) {
                OfficeTier.COWORKING -> {
                    // Crittall black factory grid
                    drawPath(windowPoly, Color(0xFF1E293B), style = Stroke(width = 3f * s))
                    val midX = (wx1 + wx2) / 2f
                    val midY1 = (winBottomY1 + winBottomY2) / 2f
                    val midY2 = (winTopY1 + winTopY2) / 2f
                    drawLine(Color(0xFF1E293B), start = Offset(midX, midY1), end = Offset(midX, midY2), strokeWidth = 2f * s)
                }
                OfficeTier.TECH_LOFT -> {
                    // Dramatic arched loft window
                    drawPath(windowPoly, Color(0xFF0F172A), style = Stroke(width = 3.5f * s))
                }
                OfficeTier.CAMPUS_HQ -> {
                    // Sleek frameless glass curtain
                    drawPath(windowPoly, Color(0xFF64748B), style = Stroke(width = 2f * s))
                }
                OfficeTier.CYBER_TOWER -> {
                    // Luxury curved gold trim glass
                    drawPath(windowPoly, Color(0xFFD4AF37), style = Stroke(width = 3f * s))
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
        val logoT = 0.48f
        val lx = topX + (leftCornerX - topX) * logoT
        val ly = topY + (leftCornerY - topY) * logoT - wallHeight * 0.62f
        val plaqueW = 86f * s
        val plaqueH = 34f * s

        val plaqueBg = when (tier) {
            OfficeTier.GARAGE -> Color(0xFF334155)
            OfficeTier.COWORKING -> Color(0xFF0F172A)
            OfficeTier.TECH_LOFT -> Color(0xFF1E293B)
            OfficeTier.CAMPUS_HQ -> Color(0xFF0284C7)
            OfficeTier.CYBER_TOWER -> Color(0xFF0F172A)
        }
        val plaqueBorder = when (tier) {
            OfficeTier.CYBER_TOWER -> Color(0xFFD4AF37)
            OfficeTier.COWORKING -> Color(0xFFEC4899) // Neon pink
            else -> Color(0xFF00E5FF)
        }

        drawRoundRect(
            color = plaqueBg,
            topLeft = Offset(lx - plaqueW / 2f, ly - plaqueH / 2f),
            size = Size(plaqueW, plaqueH),
            cornerRadius = CornerRadius(5f * s, 5f * s)
        )
        drawRoundRect(
            color = plaqueBorder,
            topLeft = Offset(lx - plaqueW / 2f, ly - plaqueH / 2f),
            size = Size(plaqueW, plaqueH),
            cornerRadius = CornerRadius(5f * s, 5f * s),
            style = Stroke(width = 2f * s)
        )

        val brandLayout = textMeasurer.measure(
            text = AnnotatedString(companyName.take(16)),
            style = TextStyle(
                color = Color.White,
                fontSize = (11f * scale).coerceAtLeast(9f).sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        drawText(
            textLayoutResult = brandLayout,
            topLeft = Offset(lx - brandLayout.size.width / 2f, ly - brandLayout.size.height / 2f)
        )

        // 2. Wall Clock with ticking hands
        val clockT = 0.22f
        val cx = topX + (leftCornerX - topX) * clockT
        val cy = topY + (leftCornerY - topY) * clockT - wallHeight * 0.68f
        val clockR = 14f * s

        drawCircle(Color(0xFF1E293B), radius = clockR, center = Offset(cx, cy))
        drawCircle(Color.White, radius = clockR - 2.5f * s, center = Offset(cx, cy))
        val hourAngle = ((hour % 12) / 12f) * 6.283f - 1.5707f
        val minAngle = 1.5707f
        drawLine(Color(0xFF0F172A), start = Offset(cx, cy), end = Offset(cx + cos(hourAngle) * clockR * 0.5f, cy + sin(hourAngle) * clockR * 0.5f), strokeWidth = 2.2f * s)
        drawLine(Color(0xFFEF4444), start = Offset(cx, cy), end = Offset(cx + cos(minAngle) * clockR * 0.75f, cy + sin(minAngle) * clockR * 0.75f), strokeWidth = 1.5f * s)
        drawCircle(Color(0xFF0F172A), radius = 2f * s, center = Offset(cx, cy))
    }
}
