package com.example.ui.office3d

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Employee
import com.example.model.FurnitureType
import com.example.model.PlacedFurniture
import kotlin.math.sin

/**
 * High-fidelity rendering of 3D isometric office furniture and equipment.
 * Enlarged for phone viewing clarity, featuring realistic materials, beveled depth,
 * multi-monitor displays with active code/graphs, ergonomic chairs, and soft contact shadows.
 */
object IsometricFurnitureRenderer {

    fun drawFurniture(
        scope: DrawScope,
        furn: PlacedFurniture,
        centerX: Float,
        centerY: Float,
        scale: Float,
        animTime: Float,
        serverLoad: Float,
        hasOutage: Boolean,
        assignedEmp: Employee?
    ) {
        scope.apply {
            when (furn.type) {
                FurnitureType.DESK_STANDARD,
                FurnitureType.DESK_DUAL_SCREEN,
                FurnitureType.DESK_EXECUTIVE -> {
                    drawWorkstation(
                        centerX = centerX,
                        centerY = centerY,
                        scale = scale,
                        type = furn.type,
                        rotation = furn.rotation,
                        animTime = animTime,
                        isOccupied = assignedEmp != null
                    )
                }
                FurnitureType.SERVER_RACK_BLADE,
                FurnitureType.SERVER_RACK_ENTERPRISE -> {
                    drawServerCabinet(
                        centerX = centerX,
                        centerY = centerY,
                        scale = scale,
                        serverLoad = serverLoad,
                        hasOutage = hasOutage,
                        animTime = animTime,
                        isQuantum = furn.type == FurnitureType.SERVER_RACK_ENTERPRISE
                    )
                }
                FurnitureType.ESPRESSO_MACHINE -> {
                    drawEspressoBar(centerX, centerY, scale, animTime)
                }
                FurnitureType.WATER_COOLER -> {
                    drawWaterCooler(centerX, centerY, scale, animTime)
                }
                FurnitureType.BONSAI_PLANT -> {
                    drawFicusPlant(centerX, centerY, scale)
                }
                FurnitureType.WHITEBOARD -> {
                    drawWhiteboard(centerX, centerY, scale)
                }
                FurnitureType.BOOKSHELF -> {
                    drawBookshelf(centerX, centerY, scale)
                }
                FurnitureType.ARCADE_CABINET -> {
                    drawArcade(centerX, centerY, scale, animTime)
                }
                FurnitureType.RECEPTION_DESK -> {
                    drawReception(centerX, centerY, scale)
                }
                FurnitureType.MEETING_TABLE -> {
                    drawMeetingTable(centerX, centerY, scale)
                }
            }
        }
    }

    private fun DrawScope.drawWorkstation(
        centerX: Float,
        centerY: Float,
        scale: Float,
        type: FurnitureType,
        rotation: Int,
        animTime: Float,
        isOccupied: Boolean
    ) {
        val s = scale

        // Scaled-up workstation dimensions for normal phone viewing
        val deskW = 74f * s
        val deskD = 38f * s
        val deskTopY = centerY - 20f * s

        // 1. Ergonomic Mesh Chair (Backrest drawn first so employee sits in front of it)
        val chairY = centerY + 14f * s
        drawErgonomicChairBack(centerX, chairY, s)

        // 2. Heavy steel leg frame under desk
        val legColor = Color(0xFF1E293B)
        // Left frame
        drawRoundRect(legColor, topLeft = Offset(centerX - deskW / 2f + 5f * s, deskTopY + deskD * 0.4f), size = Size(4.5f * s, 26f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        // Right frame
        drawRoundRect(legColor, topLeft = Offset(centerX + deskW / 2f - 9.5f * s, deskTopY + deskD * 0.4f), size = Size(4.5f * s, 26f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        // Sturdy crossbeam
        drawRect(Color(0xFF334155), topLeft = Offset(centerX - deskW / 2f + 8f * s, centerY + 8f * s), size = Size(deskW - 16f * s, 3.5f * s))

        // 3. Desktop Surface with 3D Bevel and Realistic Materials
        val topFinish = when (type) {
            FurnitureType.DESK_EXECUTIVE -> Color(0xFF1E293B) // Premium Matte Charcoal Slate
            FurnitureType.DESK_DUAL_SCREEN -> Color(0xFFD4A373) // Warm Natural Oak Wood
            else -> Color(0xFFF1F5F9) // Crisp Scandinavian White Laminate
        }
        val bevelColor = when (type) {
            FurnitureType.DESK_EXECUTIVE -> Color(0xFF0F172A)
            FurnitureType.DESK_DUAL_SCREEN -> Color(0xFFA67C52)
            else -> Color(0xFF94A3B8)
        }

        // 3D desktop edge bevel thickness
        val bevelH = 6f * s
        drawRoundRect(
            color = bevelColor,
            topLeft = Offset(centerX - deskW / 2f, deskTopY + bevelH),
            size = Size(deskW, deskD),
            cornerRadius = CornerRadius(5f * s, 5f * s)
        )
        drawRoundRect(
            color = topFinish,
            topLeft = Offset(centerX - deskW / 2f, deskTopY),
            size = Size(deskW, deskD),
            cornerRadius = CornerRadius(5f * s, 5f * s)
        )
        // Chamfer outline highlight
        drawRoundRect(
            color = Color.White.copy(alpha = 0.25f),
            topLeft = Offset(centerX - deskW / 2f, deskTopY),
            size = Size(deskW, deskD),
            cornerRadius = CornerRadius(5f * s, 5f * s),
            style = Stroke(width = 1.2f * s)
        )

        // 4. Desk Accessories
        // Large micro-textured black desk mat with stitched border
        val matW = 46f * s
        val matD = 22f * s
        drawRoundRect(
            color = Color(0xFF0B0F19),
            topLeft = Offset(centerX - matW / 2f, deskTopY + 10f * s),
            size = Size(matW, matD),
            cornerRadius = CornerRadius(3f * s, 3f * s)
        )
        drawRoundRect(
            color = Color(0xFF00E5FF).copy(alpha = 0.35f),
            topLeft = Offset(centerX - matW / 2f, deskTopY + 10f * s),
            size = Size(matW, matD),
            cornerRadius = CornerRadius(3f * s, 3f * s),
            style = Stroke(width = 0.8f * s)
        )

        // Mechanical Keyboard with individual illuminated keycaps
        val kbW = 22f * s
        val kbD = 8f * s
        val kbX = centerX - kbW / 2f - 4f * s
        val kbY = deskTopY + 18f * s
        drawRoundRect(Color(0xFF1E293B), topLeft = Offset(kbX, kbY), size = Size(kbW, kbD), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        // RGB backlighting under keys
        drawRoundRect(Color(0xFF00E5FF).copy(alpha = 0.5f), topLeft = Offset(kbX + 1.5f * s, kbY + 1f * s), size = Size(kbW - 3f * s, kbD - 2f * s), cornerRadius = CornerRadius(1f * s, 1f * s))
        // White keycaps row
        drawRect(Color(0xFFE2E8F0), topLeft = Offset(kbX + 2f * s, kbY + 2f * s), size = Size(kbW - 4f * s, 3.5f * s))

        // Ergonomic vertical mouse
        drawOval(Color(0xFF475569), topLeft = Offset(centerX + 12f * s, deskTopY + 20f * s), size = Size(5.5f * s, 8f * s))
        drawOval(Color(0xFF00E5FF), topLeft = Offset(centerX + 13.5f * s, deskTopY + 21f * s), size = Size(2.5f * s, 3f * s))

        // High-performance PC Tower with tempered glass side panel and RGB fans
        val pcW = 14f * s
        val pcH = 26f * s
        val pcX = centerX + deskW / 2f - pcW - 3f * s
        val pcY = deskTopY - 2f * s
        drawRoundRect(Color(0xFF0F172A), topLeft = Offset(pcX, pcY), size = Size(pcW, pcH), cornerRadius = CornerRadius(3f * s, 3f * s))
        // Tempered glass panel
        drawRoundRect(Color(0xFF1E293B).copy(alpha = 0.9f), topLeft = Offset(pcX + 2f * s, pcY + 3f * s), size = Size(pcW - 4f * s, pcH - 6f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        // Glowing RGB cooling fan
        val fanPulse = (sin(animTime * 5.0) * 0.3 + 0.7).toFloat()
        drawCircle(Color(0xFF00E5FF).copy(alpha = fanPulse), radius = 3.5f * s, center = Offset(pcX + pcW / 2f, pcY + 9f * s))
        drawCircle(Color(0xFFA855F7).copy(alpha = fanPulse), radius = 3.5f * s, center = Offset(pcX + pcW / 2f, pcY + 18f * s))

        // Ceramic Coffee Mug with startup logo
        val mugX = centerX - deskW / 2f + 8f * s
        val mugY = deskTopY + 12f * s
        drawCircle(Color(0xFFE2E8F0), radius = 3.5f * s, center = Offset(mugX, mugY))
        drawCircle(Color(0xFF78350F), radius = 2.4f * s, center = Offset(mugX, mugY)) // Hot coffee
        // Handle
        drawArc(Color(0xFFCBD5E1), startAngle = 90f, sweepAngle = 180f, useCenter = false, topLeft = Offset(mugX - 5.5f * s, mugY - 2.5f * s), size = Size(3f * s, 5f * s), style = Stroke(width = 1.2f * s))

        // 5. Multi-Monitor Displays with Active Content
        drawMonitorSetup(centerX, deskTopY, s, type, animTime)
    }

    private fun DrawScope.drawMonitorSetup(centerX: Float, deskTopY: Float, scale: Float, type: FurnitureType, animTime: Float) {
        val s = scale

        // Heavy-duty monitor arm clamped to back of desk
        drawLine(Color(0xFF0F172A), start = Offset(centerX, deskTopY + 4f * s), end = Offset(centerX, deskTopY - 14f * s), strokeWidth = 5f * s, cap = StrokeCap.Round)

        when (type) {
            FurnitureType.DESK_EXECUTIVE -> {
                // Curved Ultrawide 49-inch curved monitor
                val monW = 54f * s
                val monH = 26f * s
                val monY = deskTopY - monH - 8f * s

                // Metallic curved bezel
                drawRoundRect(Color(0xFF0F172A), topLeft = Offset(centerX - monW / 2f, monY), size = Size(monW, monH), cornerRadius = CornerRadius(4f * s, 4f * s))
                // Vibrant IPS display screen
                drawRoundRect(Color(0xFF0A101D), topLeft = Offset(centerX - monW / 2f + 2f * s, monY + 2f * s), size = Size(monW - 4f * s, monH - 4f * s), cornerRadius = CornerRadius(2f * s, 2f * s))

                // Left pane: Code Editor with syntax highlighted code lines
                val codeX = centerX - monW / 2f + 5f * s
                val codeColors = listOf(Color(0xFF38BDF8), Color(0xFF34D399), Color(0xFFFBBF24), Color(0xFFF472B6))
                for (row in 0..4) {
                    val rw = (10f + (row * 3f) % 14f) * s
                    drawRect(codeColors[row % codeColors.size].copy(alpha = 0.85f), topLeft = Offset(codeX, monY + 5f * s + (row * 3.5f * s)), size = Size(rw, 1.8f * s))
                }

                // Center divider
                drawLine(Color(0xFF1E293B), start = Offset(centerX - 2f * s, monY + 3f * s), end = Offset(centerX - 2f * s, monY + monH - 3f * s), strokeWidth = 1f * s)

                // Right pane: Business Analytics Chart with glowing green line
                val chartX = centerX + 4f * s
                val chartW = monW / 2f - 8f * s
                val chartH = monH - 10f * s
                val chartY = monY + 5f * s
                // Grid lines
                drawLine(Color(0xFF1E293B), start = Offset(chartX, chartY + chartH / 2f), end = Offset(chartX + chartW, chartY + chartH / 2f), strokeWidth = 0.8f * s)
                // Trend curve
                val chartPath = Path().apply {
                    moveTo(chartX, chartY + chartH * 0.8f)
                    lineTo(chartX + chartW * 0.35f, chartY + chartH * 0.5f)
                    lineTo(chartX + chartW * 0.65f, chartY + chartH * 0.65f)
                    lineTo(chartX + chartW, chartY + chartH * 0.2f)
                }
                drawPath(chartPath, Color(0xFF10B981), style = Stroke(width = 2f * s))
                drawCircle(Color(0xFF10B981), radius = 2f * s, center = Offset(chartX + chartW, chartY + chartH * 0.2f))
            }

            FurnitureType.DESK_DUAL_SCREEN -> {
                // Dual 27-inch frameless displays angled slightly inward
                val singleW = 28f * s
                val singleH = 22f * s
                val monY = deskTopY - singleH - 8f * s

                // Left Monitor: Terminal & Code Editor
                val leftX = centerX - singleW - 2f * s
                drawRoundRect(Color(0xFF0F172A), topLeft = Offset(leftX, monY), size = Size(singleW, singleH), cornerRadius = CornerRadius(3f * s, 3f * s))
                drawRoundRect(Color(0xFF0B132B), topLeft = Offset(leftX + 1.5f * s, monY + 1.5f * s), size = Size(singleW - 3f * s, singleH - 3f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                // Code lines
                for (row in 0..4) {
                    val rw = (8f + (row * 3.5f) % 12f) * s
                    drawRect(Color(0xFF00E5FF).copy(alpha = 0.8f), topLeft = Offset(leftX + 3.5f * s, monY + 4f * s + (row * 3f * s)), size = Size(rw, 1.6f * s))
                }

                // Right Monitor: UI Mockups & Web browser
                val rightX = centerX + 2f * s
                drawRoundRect(Color(0xFF0F172A), topLeft = Offset(rightX, monY), size = Size(singleW, singleH), cornerRadius = CornerRadius(3f * s, 3f * s))
                drawRoundRect(Color(0xFF1E293B), topLeft = Offset(rightX + 1.5f * s, monY + 1.5f * s), size = Size(singleW - 3f * s, singleH - 3f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                // App window layout preview
                drawRect(Color(0xFF3B82F6), topLeft = Offset(rightX + 4f * s, monY + 4f * s), size = Size(singleW - 8f * s, 5f * s))
                drawRect(Color(0xFF10B981), topLeft = Offset(rightX + 4f * s, monY + 11f * s), size = Size(singleW * 0.45f, 6f * s))
                drawRect(Color(0xFFF59E0B), topLeft = Offset(rightX + singleW * 0.55f, monY + 11f * s), size = Size(singleW * 0.35f, 6f * s))
            }

            else -> {
                // Single 32-inch High-Res Display
                val monW = 34f * s
                val monH = 24f * s
                val monY = deskTopY - monH - 8f * s

                drawRoundRect(Color(0xFF0F172A), topLeft = Offset(centerX - monW / 2f, monY), size = Size(monW, monH), cornerRadius = CornerRadius(3f * s, 3f * s))
                drawRoundRect(Color(0xFF0F172A), topLeft = Offset(centerX - monW / 2f + 1.5f * s, monY + 1.5f * s), size = Size(monW - 3f * s, monH - 3f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                // IDE Code screen
                drawRect(Color(0xFF0F172A), topLeft = Offset(centerX - monW / 2f + 2f * s, monY + 2f * s), size = Size(monW - 4f * s, monH - 4f * s))
                for (row in 0..5) {
                    val rw = (7f + (row * 4f) % 18f) * s
                    val c = if (row % 2 == 0) Color(0xFF38BDF8) else Color(0xFF34D399)
                    drawRect(c.copy(alpha = 0.85f), topLeft = Offset(centerX - monW / 2f + 4f * s, monY + 4f * s + (row * 3f * s)), size = Size(rw, 1.6f * s))
                }
            }
        }
    }

    private fun DrawScope.drawErgonomicChairBack(centerX: Float, chairY: Float, scale: Float) {
        val s = scale
        val chairW = 24f * s
        val chairH = 28f * s

        // Chrome 5-star base underneath
        drawLine(Color(0xFF64748B), start = Offset(centerX - 10f * s, chairY + 12f * s), end = Offset(centerX + 10f * s, chairY + 12f * s), strokeWidth = 2.5f * s)
        // Pneumatic cylinder
        drawLine(Color(0xFF334155), start = Offset(centerX, chairY + 6f * s), end = Offset(centerX, chairY + 12f * s), strokeWidth = 4f * s)

        // Breathable mesh ergonomic backrest
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(centerX - chairW / 2f, chairY - chairH),
            size = Size(chairW, chairH),
            cornerRadius = CornerRadius(6f * s, 6f * s)
        )
        // Mesh insert
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(centerX - chairW / 2f + 2.5f * s, chairY - chairH + 3f * s),
            size = Size(chairW - 5f * s, chairH - 6f * s),
            cornerRadius = CornerRadius(4f * s, 4f * s)
        )
        // Lumbar support pillow band
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(centerX - chairW / 2f + 2f * s, chairY - chairH * 0.45f),
            size = Size(chairW - 4f * s, 4.5f * s)
        )
        // 3D adjustable armrests
        drawRoundRect(Color(0xFF334155), topLeft = Offset(centerX - chairW / 2f - 4f * s, chairY - chairH * 0.4f), size = Size(3.5f * s, 10f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        drawRoundRect(Color(0xFF334155), topLeft = Offset(centerX + chairW / 2f + 0.5f * s, chairY - chairH * 0.4f), size = Size(3.5f * s, 10f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
    }

    private fun DrawScope.drawServerCabinet(
        centerX: Float,
        centerY: Float,
        scale: Float,
        serverLoad: Float,
        hasOutage: Boolean,
        animTime: Float,
        isQuantum: Boolean
    ) {
        val s = scale
        val rackW = 44f * s
        val rackH = 68f * s
        val topY = centerY - rackH + 8f * s

        // Industrial cabinet chassis
        drawRoundRect(Color(0xFF0B0F19), topLeft = Offset(centerX - rackW / 2f, topY), size = Size(rackW, rackH), cornerRadius = CornerRadius(4f * s, 4f * s))
        // Steel frame bevel
        drawRoundRect(Color(0xFF1E293B), topLeft = Offset(centerX - rackW / 2f + 2.5f * s, topY + 3f * s), size = Size(rackW - 5f * s, rackH - 6f * s), cornerRadius = CornerRadius(2f * s, 2f * s), style = Stroke(width = 1.5f * s))

        // 6 Stacked Server Blade Modules
        val ledColor = when {
            hasOutage -> Color(0xFFEF4444)
            serverLoad > 0.85f -> Color(0xFFF59E0B)
            else -> Color(0xFF10B981)
        }
        val ledPulse = (sin(animTime * 6.0) * 0.4 + 0.6).toFloat()

        for (i in 0 until 6) {
            val bladeY = topY + 7f * s + (i * 9f * s)
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(centerX - rackW / 2f + 4f * s, bladeY), size = Size(rackW - 8f * s, 7.5f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
            // Blade metal handles
            drawLine(Color(0xFF94A3B8), start = Offset(centerX - rackW / 2f + 6f * s, bladeY + 2f * s), end = Offset(centerX - rackW / 2f + 6f * s, bladeY + 5.5f * s), strokeWidth = 1.2f * s)
            drawLine(Color(0xFF94A3B8), start = Offset(centerX + rackW / 2f - 6f * s, bladeY + 2f * s), end = Offset(centerX + rackW / 2f - 6f * s, bladeY + 5.5f * s), strokeWidth = 1.2f * s)
            // Flashing Ethernet & Drive activity LEDs
            drawCircle(ledColor.copy(alpha = ledPulse), radius = 1.8f * s, center = Offset(centerX - rackW / 2f + 11f * s, bladeY + 3.8f * s))
            drawCircle(Color(0xFF00E5FF).copy(alpha = 0.85f), radius = 1.5f * s, center = Offset(centerX - rackW / 2f + 16f * s, bladeY + 3.8f * s))
            drawCircle(Color(0xFFFBBF24).copy(alpha = 0.7f), radius = 1.2f * s, center = Offset(centerX - rackW / 2f + 20f * s, bladeY + 3.8f * s))
        }

        if (isQuantum) {
            // Illuminated Cryogenic Liquid Coolant Tubes
            val tubePulse = (sin(animTime * 3.5) * 0.2 + 0.8).toFloat()
            drawLine(Color(0xFF00E5FF).copy(alpha = tubePulse), start = Offset(centerX + rackW / 2f - 7f * s, topY + 6f * s), end = Offset(centerX + rackW / 2f - 7f * s, topY + rackH - 8f * s), strokeWidth = 3f * s, cap = StrokeCap.Round)
            // Coolant tank glow
            drawCircle(Color(0xFF00E5FF).copy(alpha = 0.5f), radius = 5f * s, center = Offset(centerX + rackW / 2f - 7f * s, topY + rackH - 12f * s))
        }
    }

    private fun DrawScope.drawEspressoBar(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
        val s = scale
        val w = 48f * s
        val h = 48f * s
        val topY = centerY - h + 10f * s

        // Warm walnut counter base
        drawRoundRect(Color(0xFF78350F), topLeft = Offset(centerX - w / 2f, topY + 16f * s), size = Size(w, h - 16f * s), cornerRadius = CornerRadius(4f * s, 4f * s))
        // Brushed stainless steel countertop
        drawRoundRect(Color(0xFFE2E8F0), topLeft = Offset(centerX - w / 2f - 2f * s, topY + 14f * s), size = Size(w + 4f * s, 5f * s), cornerRadius = CornerRadius(2f * s, 2f * s))

        // Commercial Italian espresso machine
        val machW = 26f * s
        val machH = 22f * s
        drawRoundRect(Color(0xFF334155), topLeft = Offset(centerX - machW / 2f, topY - 4f * s), size = Size(machW, machH), cornerRadius = CornerRadius(3f * s, 3f * s))
        // Polished chrome face
        drawRect(Color(0xFFCBD5E1), topLeft = Offset(centerX - machW / 2f + 3f * s, topY - 1f * s), size = Size(machW - 6f * s, 9f * s))
        // Twin pressure dials
        drawCircle(Color.White, radius = 2.2f * s, center = Offset(centerX - 4f * s, topY + 3.5f * s))
        drawCircle(Color.White, radius = 2.2f * s, center = Offset(centerX + 4f * s, topY + 3.5f * s))
        // Twin portafilters and cups
        drawRect(Color(0xFF475569), topLeft = Offset(centerX - 5f * s, topY + 8f * s), size = Size(4f * s, 5f * s))
        drawRect(Color(0xFF475569), topLeft = Offset(centerX + 1f * s, topY + 8f * s), size = Size(4f * s, 5f * s))
        // White espresso cups
        drawRoundRect(Color.White, topLeft = Offset(centerX - 5.5f * s, topY + 12f * s), size = Size(5f * s, 4f * s), cornerRadius = CornerRadius(1f * s, 1f * s))
        drawRoundRect(Color.White, topLeft = Offset(centerX + 0.5f * s, topY + 12f * s), size = Size(5f * s, 4f * s), cornerRadius = CornerRadius(1f * s, 1f * s))

        // Steam rising animation
        val steamBob = (sin(animTime * 4.0) * 2f * s).toFloat()
        drawCircle(Color.White.copy(alpha = 0.35f), radius = 2.5f * s, center = Offset(centerX - 3f * s, topY - 7f * s + steamBob))
    }

    private fun DrawScope.drawWaterCooler(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
        val s = scale
        val w = 22f * s
        val h = 52f * s
        val topY = centerY - h + 8f * s

        // Sleek white matte dispenser tower
        drawRoundRect(Color(0xFFF8FAFC), topLeft = Offset(centerX - w / 2f, topY + 20f * s), size = Size(w, h - 20f * s), cornerRadius = CornerRadius(3f * s, 3f * s))
        drawRect(Color(0xFF334155), topLeft = Offset(centerX - 5f * s, topY + 26f * s), size = Size(10f * s, 10f * s))
        // Hot & Cold levers
        drawCircle(Color(0xFFEF4444), radius = 1.6f * s, center = Offset(centerX - 2.5f * s, topY + 28f * s))
        drawCircle(Color(0xFF3B82F6), radius = 1.6f * s, center = Offset(centerX + 2.5f * s, topY + 28f * s))

        // Blue transparent inverted water carboy with animated bubble
        val carboyR = 10f * s
        val carboyCenter = Offset(centerX, topY + 9f * s)
        drawCircle(Color(0xFF0284C7).copy(alpha = 0.85f), radius = carboyR, center = carboyCenter)
        drawCircle(Color.White.copy(alpha = 0.45f), radius = carboyR * 0.65f, center = Offset(carboyCenter.x - 2f * s, carboyCenter.y - 2f * s))
        val bubbleBob = (sin(animTime * 3.0) * 2.5f * s).toFloat()
        drawCircle(Color.White.copy(alpha = 0.75f), radius = 1.8f * s, center = Offset(carboyCenter.x, carboyCenter.y + bubbleBob))
    }

    private fun DrawScope.drawFicusPlant(centerX: Float, centerY: Float, scale: Float) {
        val s = scale
        val potW = 24f * s
        val potH = 20f * s
        val potTopY = centerY - potH

        // Modern ribbed ceramic planter
        drawRoundRect(Color(0xFFC2410C), topLeft = Offset(centerX - potW / 2f, potTopY), size = Size(potW, potH), cornerRadius = CornerRadius(3f * s, 3f * s))
        drawRoundRect(Color(0xFF9A3412), topLeft = Offset(centerX - potW / 2f - 2f * s, potTopY), size = Size(potW + 4f * s, 4f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        // Rich potting soil
        drawOval(Color(0xFF292524), topLeft = Offset(centerX - potW / 2f + 2f * s, potTopY - 1f * s), size = Size(potW - 4f * s, 4f * s))

        // Multi-tonal lush tropical leaves
        drawCircle(Color(0xFF14532D), radius = 14f * s, center = Offset(centerX, potTopY - 12f * s))
        drawCircle(Color(0xFF166534), radius = 12f * s, center = Offset(centerX - 9f * s, potTopY - 8f * s))
        drawCircle(Color(0xFF15803D), radius = 11f * s, center = Offset(centerX + 9f * s, potTopY - 9f * s))
        drawCircle(Color(0xFF22C55E), radius = 9f * s, center = Offset(centerX, potTopY - 18f * s))
        drawCircle(Color(0xFF4ADE80), radius = 6f * s, center = Offset(centerX - 3f * s, potTopY - 14f * s))
    }

    private fun DrawScope.drawWhiteboard(centerX: Float, centerY: Float, scale: Float) {
        val s = scale
        val w = 54f * s
        val h = 56f * s
        val topY = centerY - h + 6f * s

        // Aluminum mobile frame
        drawLine(Color(0xFF64748B), start = Offset(centerX - w / 2f + 2f * s, centerY + 4f * s), end = Offset(centerX - w / 2f + 2f * s, topY), strokeWidth = 3f * s)
        drawLine(Color(0xFF64748B), start = Offset(centerX + w / 2f - 2f * s, centerY + 4f * s), end = Offset(centerX + w / 2f - 2f * s, topY), strokeWidth = 3f * s)
        // Wheels
        drawCircle(Color(0xFF0F172A), radius = 3f * s, center = Offset(centerX - w / 2f + 2f * s, centerY + 4f * s))
        drawCircle(Color(0xFF0F172A), radius = 3f * s, center = Offset(centerX + w / 2f - 2f * s, centerY + 4f * s))

        // Porcelain dry-erase surface
        drawRoundRect(Color(0xFFF8FAFC), topLeft = Offset(centerX - w / 2f + 3f * s, topY + 2f * s), size = Size(w - 6f * s, h - 16f * s), cornerRadius = CornerRadius(3f * s, 3f * s))
        drawRoundRect(Color(0xFF94A3B8), topLeft = Offset(centerX - w / 2f + 3f * s, topY + 2f * s), size = Size(w - 6f * s, h - 16f * s), cornerRadius = CornerRadius(3f * s, 3f * s), style = Stroke(width = 1.5f * s))

        // Sprint columns: TODO | DOING | DONE
        val colW = (w - 12f * s) / 3f
        drawLine(Color(0xFFE2E8F0), start = Offset(centerX - w / 2f + 5f * s + colW, topY + 4f * s), end = Offset(centerX - w / 2f + 5f * s + colW, topY + h - 16f * s), strokeWidth = 1f * s)
        drawLine(Color(0xFFE2E8F0), start = Offset(centerX - w / 2f + 5f * s + colW * 2f, topY + 4f * s), end = Offset(centerX - w / 2f + 5f * s + colW * 2f, topY + h - 16f * s), strokeWidth = 1f * s)

        // Colorful sprint sticky notes
        drawRect(Color(0xFFFBBF24), topLeft = Offset(centerX - w / 2f + 7f * s, topY + 8f * s), size = Size(7f * s, 7f * s))
        drawRect(Color(0xFFF472B6), topLeft = Offset(centerX - w / 2f + 7f * s, topY + 18f * s), size = Size(7f * s, 7f * s))
        drawRect(Color(0xFF38BDF8), topLeft = Offset(centerX - w / 2f + 8f * s + colW, topY + 10f * s), size = Size(7f * s, 7f * s))
        drawRect(Color(0xFF34D399), topLeft = Offset(centerX - w / 2f + 8f * s + colW * 2f, topY + 8f * s), size = Size(7f * s, 7f * s))
    }

    private fun DrawScope.drawBookshelf(centerX: Float, centerY: Float, scale: Float) {
        val s = scale
        val w = 44f * s
        val h = 60f * s
        val topY = centerY - h + 8f * s

        // Hardwood bookcase cabinet
        drawRoundRect(Color(0xFF78350F), topLeft = Offset(centerX - w / 2f, topY), size = Size(w, h), cornerRadius = CornerRadius(3f * s, 3f * s))
        // Shelves and books
        val bookColors = listOf(Color(0xFF0284C7), Color(0xFFDC2626), Color(0xFF16A34A), Color(0xFFD97706), Color(0xFF7C3AED), Color(0xFF2563EB))
        for (sh in 0..2) {
            val sy = topY + 8f * s + (sh * 17f * s)
            drawRect(Color(0xFF451A03), topLeft = Offset(centerX - w / 2f + 3f * s, sy + 11f * s), size = Size(w - 6f * s, 2.5f * s))
            for (b in 0..5) {
                val bx = centerX - w / 2f + 5f * s + (b * 5.5f * s)
                drawRect(bookColors[(b + sh) % bookColors.size], topLeft = Offset(bx, sy), size = Size(4.8f * s, 11f * s))
            }
        }
    }

    private fun DrawScope.drawArcade(centerX: Float, centerY: Float, scale: Float, animTime: Float) {
        val s = scale
        val w = 36f * s
        val h = 66f * s
        val topY = centerY - h + 8f * s

        // Purple retro arcade cabinet
        drawRoundRect(Color(0xFF581C87), topLeft = Offset(centerX - w / 2f, topY), size = Size(w, h), cornerRadius = CornerRadius(5f * s, 5f * s))
        // Neon marquee
        drawRoundRect(Color(0xFFEC4899), topLeft = Offset(centerX - w / 2f + 4f * s, topY + 4f * s), size = Size(w - 8f * s, 9f * s), cornerRadius = CornerRadius(2.5f * s, 2.5f * s))
        // Glowing CRT screen
        val screenPulse = (sin(animTime * 6.0) * 0.25 + 0.75).toFloat()
        drawRect(Color(0xFF00E5FF).copy(alpha = screenPulse), topLeft = Offset(centerX - w / 2f + 5f * s, topY + 16f * s), size = Size(w - 10f * s, 22f * s))
        // Control deck with joystick and buttons
        drawRect(Color(0xFF1E1B4B), topLeft = Offset(centerX - w / 2f + 3f * s, topY + 39f * s), size = Size(w - 6f * s, 9f * s))
        drawCircle(Color(0xFFEF4444), radius = 2.4f * s, center = Offset(centerX - 7f * s, topY + 43f * s))
        drawCircle(Color(0xFFFBBF24), radius = 1.5f * s, center = Offset(centerX + 3f * s, topY + 43f * s))
        drawCircle(Color(0xFF38BDF8), radius = 1.5f * s, center = Offset(centerX + 8f * s, topY + 43f * s))
    }

    private fun DrawScope.drawReception(centerX: Float, centerY: Float, scale: Float) {
        val s = scale
        val w = 68f * s
        val h = 40f * s
        val topY = centerY - 20f * s

        // Curved fluted wood reception counter
        drawRoundRect(Color(0xFF8B5A2B), topLeft = Offset(centerX - w / 2f, topY), size = Size(w, h), cornerRadius = CornerRadius(10f * s, 10f * s))
        // Italian marble transaction counter
        drawRoundRect(Color(0xFFF8FAFC), topLeft = Offset(centerX - w / 2f - 3f * s, topY - 4f * s), size = Size(w + 6f * s, 12f * s), cornerRadius = CornerRadius(5f * s, 5f * s))
        // Visitor sign-in tablet display
        drawRoundRect(Color(0xFF0F172A), topLeft = Offset(centerX - 10f * s, topY - 2f * s), size = Size(10f * s, 7f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
        drawCircle(Color(0xFF10B981), radius = 2f * s, center = Offset(centerX + 16f * s, topY + 2f * s))
    }

    private fun DrawScope.drawMeetingTable(centerX: Float, centerY: Float, scale: Float) {
        val s = scale
        val w = 74f * s
        val h = 42f * s
        val topY = centerY - 18f * s

        // 4 Conference chairs around table
        val chairColor = Color(0xFF1E293B)
        drawOval(chairColor, topLeft = Offset(centerX - 30f * s, topY - 14f * s), size = Size(16f * s, 12f * s))
        drawOval(chairColor, topLeft = Offset(centerX + 14f * s, topY - 14f * s), size = Size(16f * s, 12f * s))
        drawOval(chairColor, topLeft = Offset(centerX - 30f * s, topY + h + 2f * s), size = Size(16f * s, 12f * s))
        drawOval(chairColor, topLeft = Offset(centerX + 14f * s, topY + h + 2f * s), size = Size(16f * s, 12f * s))

        // Large mahogany oval boardroom table
        drawOval(Color(0xFF78350F), topLeft = Offset(centerX - w / 2f, topY), size = Size(w, h))
        drawOval(Color(0xFF92400E), topLeft = Offset(centerX - w / 2f + 3f * s, topY + 2.5f * s), size = Size(w - 6f * s, h - 5f * s))
        // Central conference speaker puck
        drawCircle(Color(0xFF0F172A), radius = 5f * s, center = Offset(centerX, topY + h / 2f))
        drawCircle(Color(0xFF00E5FF), radius = 2f * s, center = Offset(centerX, topY + h / 2f))
    }
}
