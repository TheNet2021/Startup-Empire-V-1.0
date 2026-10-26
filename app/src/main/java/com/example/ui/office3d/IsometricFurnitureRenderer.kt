package com.example.ui.office3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.model.Employee
import com.example.model.EmployeeRole
import com.example.model.EmployeeState
import com.example.model.FurnitureCategory
import com.example.model.FurnitureType
import com.example.model.PlacedFurniture
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Commercial-quality 3D isometric furniture, equipment, and character renderer.
 * Composites high-resolution pre-rendered 3D isometric models (transparent RGBA)
 * with real-time interactive lighting, shadows, LED activity, and subtle animations.
 */
object IsometricFurnitureRenderer {

    fun drawFurniture(
        scope: DrawScope,
        furn: PlacedFurniture,
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        animTime: Float,
        serverLoad: Float,
        hasOutage: Boolean,
        assignedEmp: Employee?,
        sprites: IsometricSpriteAtlas
    ) {
        scope.apply {
            when (furn.type) {
                FurnitureType.DESK_STANDARD,
                FurnitureType.DESK_DUAL_SCREEN,
                FurnitureType.DESK_EXECUTIVE -> {
                    drawWorkstation(
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        furn = furn,
                        assignedEmp = assignedEmp,
                        animTime = animTime,
                        sprites = sprites
                    )
                }
                FurnitureType.SERVER_RACK_BLADE,
                FurnitureType.SERVER_RACK_ENTERPRISE -> {
                    drawServerRack(
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        rotation = furn.rotation,
                        serverLoad = serverLoad,
                        hasOutage = hasOutage,
                        animTime = animTime,
                        isQuantum = furn.type == FurnitureType.SERVER_RACK_ENTERPRISE,
                        sprite = sprites.serverRack
                    )
                }
                FurnitureType.BONSAI_PLANT -> {
                    drawSpriteItem(
                        sprite = sprites.plant,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 0.75f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
                FurnitureType.WATER_COOLER -> {
                    drawWaterCooler(
                        sprite = sprites.waterCooler,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        rotation = furn.rotation,
                        animTime = animTime
                    )
                }
                FurnitureType.ARCADE_CABINET -> {
                    drawArcade(
                        sprite = sprites.arcade,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        rotation = furn.rotation,
                        animTime = animTime
                    )
                }
                FurnitureType.WHITEBOARD -> {
                    drawSpriteItem(
                        sprite = sprites.whiteboard,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 0.90f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
                FurnitureType.ESPRESSO_MACHINE -> {
                    drawSpriteItem(
                        sprite = sprites.espresso,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 0.78f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
                FurnitureType.MEETING_TABLE -> {
                    drawSpriteItem(
                        sprite = sprites.meetingTable,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 1.15f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
                FurnitureType.BOOKSHELF -> {
                    drawSpriteItem(
                        sprite = sprites.bookshelf,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 0.85f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
                FurnitureType.RECEPTION_DESK -> {
                    drawSpriteItem(
                        sprite = sprites.meetingTable,
                        centerX = centerX,
                        centerY = centerY,
                        tileW = tileW,
                        tileH = tileH,
                        scale = scale,
                        widthMultiplier = 1.05f,
                        anchorYFactor = 0.18f,
                        rotation = furn.rotation
                    )
                }
            }
        }
    }

    private fun DrawScope.drawWorkstation(
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        furn: PlacedFurniture,
        assignedEmp: Employee?,
        animTime: Float,
        sprites: IsometricSpriteAtlas
    ) {
        // Choose active 3D model sprite:
        // If occupied: render stylized 3D adult character seated at workstation
        // If unoccupied: modern sit-stand motorized desk with dual monitors and Aeron chair
        val sprite: ImageBitmap = if (assignedEmp != null) {
            val isFemale = (assignedEmp.name.hashCode() and 0x7FFFFFFF) % 2 == 0
            if (isFemale) sprites.employeeFemale else sprites.employeeMale
        } else {
            sprites.deskEmpty
        }

        val aspect = sprite.height.toFloat() / sprite.width.toFloat()
        // Precise spatial calibration: sits comfortably within diamond boundary without encroaching neighbor cells
        val targetW = (tileW * 1.16f).roundToInt().toFloat()
        val targetH = (targetW * aspect).roundToInt().toFloat()

        // Rock-solid stationary anchor: strictly independent of employee animation or simulation speed
        val dstLeft = (centerX - (targetW / 2f)).roundToInt().toFloat()
        val dstTop = ((centerY + tileH * 0.16f) - targetH).roundToInt().toFloat()

        // Multi-Phase Employee State & Typing Cadence for visual overlay effects (WITHOUT moving the desk)
        var isTypingBurst = false
        if (assignedEmp != null) {
            val seed = (assignedEmp.id.hashCode() and 0x7FFFFFFF) % 100
            if (assignedEmp.state == EmployeeState.WORKING) {
                // Cadence: Alternating between active typing bursts and thinking / code review
                val cadence = sin(animTime * 2.8f + seed * 0.2f)
                isTypingBurst = cadence > -0.25f
            }
        }

        val isFlipped = furn.rotation == 90 || furn.rotation == 270 || furn.rotation == 1 || furn.rotation == 3

        if (isFlipped) {
            withTransform({
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset(centerX, centerY))
            }) {
                drawImage(
                    image = sprite,
                    dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                    dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                    filterQuality = FilterQuality.Medium
                )
            }
        } else {
            drawImage(
                image = sprite,
                dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                filterQuality = FilterQuality.Medium
            )
        }

        // Active Monitor Code Glow, Ambient Electronics & State Effects
        if (assignedEmp != null) {
            val baseRoleColor = when (assignedEmp.role) {
                EmployeeRole.DEVELOPER -> Color(0xFF00E5FF)
                EmployeeRole.DESIGNER -> Color(0xFFEC4899)
                EmployeeRole.SYSADMIN -> Color(0xFF10B981)
                EmployeeRole.MARKETER, EmployeeRole.SALES -> Color(0xFFF59E0B)
                else -> Color(0xFF38BDF8)
            }

            // Screen light dynamic flicker / modulation during typing bursts
            val glowAlpha = if (isTypingBurst) {
                (0.24f + sin(animTime * 12f) * 0.08f).coerceIn(0.16f, 0.35f)
            } else {
                0.16f
            }

            // Subtle ambient dual-screen backlight cast on desktop
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(baseRoleColor.copy(alpha = glowAlpha), Color.Transparent),
                    center = Offset(centerX, centerY - 12f * scale),
                    radius = 28f * scale
                ),
                topLeft = Offset(centerX - 28f * scale, centerY - 26f * scale),
                size = Size(56f * scale, 28f * scale)
            )

            // Animated code reflection lines across desk screen zone when actively working
            if (isTypingBurst) {
                val linePhase = ((animTime * 30f) % 18f) * scale
                drawLine(
                    color = baseRoleColor.copy(alpha = 0.35f),
                    start = Offset(centerX - 14f * scale, centerY - 18f * scale + linePhase),
                    end = Offset(centerX + 6f * scale, centerY - 18f * scale + linePhase),
                    strokeWidth = 1.2f * scale
                )

                // Typing micro-activity sparks on keyboard area (indicates rapid typing fingers)
                val keyPhase = (sin(animTime * 20f) * 2f).toInt()
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = 1.5f * scale,
                    center = Offset(centerX - (4f + keyPhase) * scale, centerY - 4f * scale)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = 1.3f * scale,
                    center = Offset(centerX + (3f - keyPhase) * scale, centerY - 3f * scale)
                )
            }

            // State specific visual indicators (anchored without modifying furniture bounds)
            when (assignedEmp.state) {
                EmployeeState.COFFEE_BREAK -> {
                    // Hot steaming coffee mug on desk corner
                    val cupX = centerX + 18f * scale
                    val cupY = centerY - 6f * scale
                    // Porcelain mug base
                    drawCircle(Color(0xFFE2E8F0), radius = 3.5f * scale, center = Offset(cupX, cupY))
                    drawCircle(Color(0xFF78350F), radius = 2.2f * scale, center = Offset(cupX, cupY))
                    // Rising animated steam wisps (2 delicate translucent paths)
                    val steam1Y = ((animTime * 12f) % 16f) * scale
                    val steam2Y = (((animTime * 12f) + 8f) % 16f) * scale
                    val steam1Alpha = (1f - (steam1Y / (16f * scale))).coerceIn(0f, 0.7f)
                    val steam2Alpha = (1f - (steam2Y / (16f * scale))).coerceIn(0f, 0.7f)
                    drawLine(
                        color = Color.White.copy(alpha = steam1Alpha),
                        start = Offset(cupX - 1f * scale, cupY - 2f * scale - steam1Y),
                        end = Offset(cupX + 1.5f * scale, cupY - 8f * scale - steam1Y),
                        strokeWidth = 1.2f * scale
                    )
                    drawLine(
                        color = Color.White.copy(alpha = steam2Alpha),
                        start = Offset(cupX + 1f * scale, cupY - 2f * scale - steam2Y),
                        end = Offset(cupX - 1.5f * scale, cupY - 8f * scale - steam2Y),
                        strokeWidth = 1.2f * scale
                    )
                }
                EmployeeState.BRAINSTORMING -> {
                    // Soft pulsing idea lightbulb / creative inspiration particle floating above head
                    val sparkPulse = (sin(animTime * 4f) * 0.25f + 0.75f).coerceIn(0.5f, 1f)
                    val sparkY = dstTop + 8f * scale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.7f * sparkPulse), Color.Transparent),
                            center = Offset(centerX, sparkY),
                            radius = 12f * scale
                        ),
                        radius = 12f * scale,
                        center = Offset(centerX, sparkY)
                    )
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = 3.2f * scale,
                        center = Offset(centerX, sparkY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.5f * scale,
                        center = Offset(centerX, sparkY)
                    )
                }
                EmployeeState.RESTING -> {
                    // Gentle restful sleep/relaxation "z" indicator floating upward
                    val zOffset = ((animTime * 8f) % 14f) * scale
                    val zAlpha = (1f - (zOffset / (14f * scale))).coerceIn(0f, 0.7f)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = zAlpha * 0.55f),
                        radius = 2.4f * scale,
                        center = Offset(centerX + 12f * scale + zOffset * 0.3f, dstTop + 18f * scale - zOffset)
                    )
                }
                EmployeeState.EXHAUSTED -> {
                    // Soft pulsing fatigue halo above head
                    val pulse = (sin(animTime * 4f) * 0.35f + 0.65f).coerceIn(0.3f, 1f)
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = pulse * 0.6f),
                        radius = 5.0f * scale,
                        center = Offset(centerX, dstTop + 14f * scale)
                    )
                }
                else -> {}
            }
        }
    }

    private fun DrawScope.drawServerRack(
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        rotation: Int,
        serverLoad: Float,
        hasOutage: Boolean,
        animTime: Float,
        isQuantum: Boolean,
        sprite: ImageBitmap
    ) {
        val aspect = sprite.height.toFloat() / sprite.width.toFloat()
        val targetW = (tileW * 0.95f).roundToInt().toFloat()
        val targetH = (targetW * aspect).roundToInt().toFloat()

        val dstLeft = (centerX - (targetW / 2f)).roundToInt().toFloat()
        val dstTop = ((centerY + tileH * 0.24f) - targetH).roundToInt().toFloat()

        val isFlipped = rotation == 90 || rotation == 270 || rotation == 1 || rotation == 3

        if (isFlipped) {
            withTransform({
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset(centerX, centerY))
            }) {
                drawImage(
                    image = sprite,
                    dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                    dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                    filterQuality = FilterQuality.Medium
                )
            }
        } else {
            drawImage(
                image = sprite,
                dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                filterQuality = FilterQuality.Medium
            )
        }

        // Real-time animated Server LEDs
        val ledCenterX = centerX + (if (isFlipped) -6f else 6f) * scale
        val startY = dstTop + targetH * 0.35f
        val rackH = targetH * 0.45f

        for (i in 0..5) {
            val ledY = startY + (rackH * (i / 5f))
            val blink = sin(animTime * (5f + i * 2f)) > 0f

            val ledColor = when {
                hasOutage -> Color(0xFFEF4444)
                serverLoad > 0.85f -> if (blink) Color(0xFFEF4444) else Color(0xFFF59E0B)
                isQuantum -> if (blink) Color(0xFF00E5FF) else Color(0xFFA855F7)
                else -> if (blink) Color(0xFF10B981) else Color(0xFF00E5FF)
            }

            drawCircle(
                color = ledColor,
                radius = 2.2f * scale,
                center = Offset(ledCenterX, ledY)
            )
        }

        // Outage Warning Beacon on top of server rack
        if (hasOutage) {
            val beaconPulse = (sin(animTime * 8f) * 0.5f + 0.5f)
            drawCircle(
                color = Color(0xFFEF4444).copy(alpha = beaconPulse),
                radius = 8f * scale,
                center = Offset(centerX, dstTop + 8f * scale)
            )
        }
    }

    private fun DrawScope.drawWaterCooler(
        sprite: ImageBitmap,
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        rotation: Int,
        animTime: Float
    ) {
        drawSpriteItem(
            sprite = sprite,
            centerX = centerX,
            centerY = centerY,
            tileW = tileW,
            tileH = tileH,
            scale = scale,
            widthMultiplier = 0.70f,
            anchorYFactor = 0.22f,
            rotation = rotation
        )

        // Animated water bubble inside the dispenser jug
        val bubbleYOffset = ((animTime * 14f) % 24f) * scale
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = 2.0f * scale,
            center = Offset(centerX + 1f * scale, centerY - (45f * scale) - bubbleYOffset)
        )
    }

    private fun DrawScope.drawArcade(
        sprite: ImageBitmap,
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        rotation: Int,
        animTime: Float
    ) {
        drawSpriteItem(
            sprite = sprite,
            centerX = centerX,
            centerY = centerY,
            tileW = tileW,
            tileH = tileH,
            scale = scale,
            widthMultiplier = 0.90f,
            anchorYFactor = 0.22f,
            rotation = rotation
        )

        // Subtle arcade CRT screen light pulse
        val crtGlow = (sin(animTime * 6f) * 0.15f + 0.85f).coerceIn(0.7f, 1f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.20f * crtGlow), Color.Transparent),
                center = Offset(centerX, centerY - 24f * scale),
                radius = 28f * scale
            ),
            radius = 28f * scale,
            center = Offset(centerX, centerY - 24f * scale)
        )
    }

    private fun DrawScope.drawSpriteItem(
        sprite: ImageBitmap,
        centerX: Float,
        centerY: Float,
        tileW: Float,
        tileH: Float,
        scale: Float,
        widthMultiplier: Float,
        anchorYFactor: Float,
        rotation: Int
    ) {
        val aspect = sprite.height.toFloat() / sprite.width.toFloat()
        val targetW = (tileW * widthMultiplier).roundToInt().toFloat()
        val targetH = (targetW * aspect).roundToInt().toFloat()

        val dstLeft = (centerX - (targetW / 2f)).roundToInt().toFloat()
        val dstTop = ((centerY + tileH * anchorYFactor) - targetH).roundToInt().toFloat()

        val isFlipped = rotation == 90 || rotation == 270 || rotation == 1 || rotation == 3

        if (isFlipped) {
            withTransform({
                scale(scaleX = -1f, scaleY = 1f, pivot = Offset(centerX, centerY))
            }) {
                drawImage(
                    image = sprite,
                    dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                    dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                    filterQuality = FilterQuality.Medium
                )
            }
        } else {
            drawImage(
                image = sprite,
                dstOffset = IntOffset(dstLeft.toInt(), dstTop.toInt()),
                dstSize = IntSize(targetW.toInt(), targetH.toInt()),
                filterQuality = FilterQuality.Medium
            )
        }
    }
}
