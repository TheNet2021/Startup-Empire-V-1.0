package com.example.ui.office3d

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.Employee
import com.example.model.EmployeeRole
import com.example.model.EmployeeState
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity rendering of stylized 3D adult characters for the isometric office.
 * Features natural adult body proportions, detailed faces, multiple hairstyles,
 * modern professional tech clothing, natural ergonomic chair sitting posture,
 * and smooth typing/working animations without floating debug labels.
 */
object IsometricCharacterRenderer {

    fun drawCharacter(
        scope: DrawScope,
        emp: Employee,
        centerX: Float,
        centerY: Float,
        scale: Float,
        animTime: Float,
        furnitureRotation: Int
    ) {
        scope.apply {
            val s = scale

            // Micro-timing offsets derived from employee id for unsynchronized natural animations
            val idSeed = emp.id.hashCode() % 100
            val localTime = animTime + (idSeed * 0.1f)

            // Natural adult proportions (head-to-body ~1:6.5 seated)
            // Seated posture: torso is slightly forward, hands resting on desk / keyboard
            val torsoW = 22f * s
            val torsoH = 24f * s
            val torsoY = centerY - 14f * s

            // Subtle breathing rhythm (gentle chest movement)
            val breathOffset = (sin(localTime * 2.2) * 0.8f * s).toFloat()

            // 1. Soft contact shadow under character and chair
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x55000000), Color(0x00000000)),
                    center = Offset(centerX, centerY + 8f * s),
                    radius = 20f * s
                ),
                topLeft = Offset(centerX - 18f * s, centerY - 2f * s),
                size = Size(36f * s, 18f * s)
            )

            // 2. Seated legs and trousers (dark modern denim / charcoal chinos)
            val trouserColor = Color(0xFF1E293B)
            val legW = 8f * s
            val legL = 16f * s
            // Left thigh
            drawRoundRect(
                color = trouserColor,
                topLeft = Offset(centerX - 9f * s, torsoY + torsoH - 5f * s),
                size = Size(legW, legL),
                cornerRadius = CornerRadius(3f * s, 3f * s)
            )
            // Right thigh
            drawRoundRect(
                color = trouserColor,
                topLeft = Offset(centerX + 1f * s, torsoY + torsoH - 5f * s),
                size = Size(legW, legL),
                cornerRadius = CornerRadius(3f * s, 3f * s)
            )

            // 3. Torso with Modern Professional Clothing
            val shirtBaseColor = Color(emp.shirtColor)
            val shirtShadowColor = Color(
                red = (shirtBaseColor.red * 0.75f).coerceIn(0f, 1f),
                green = (shirtBaseColor.green * 0.75f).coerceIn(0f, 1f),
                blue = (shirtBaseColor.blue * 0.75f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Torso shape with natural shoulder taper
            val torsoPath = Path().apply {
                moveTo(centerX - torsoW / 2f, torsoY + 4f * s + breathOffset)
                lineTo(centerX + torsoW / 2f, torsoY + 4f * s + breathOffset)
                lineTo(centerX + torsoW / 2f - 2f * s, torsoY + torsoH + breathOffset)
                lineTo(centerX - torsoW / 2f + 2f * s, torsoY + torsoH + breathOffset)
                close()
            }

            drawPath(
                path = torsoPath,
                brush = Brush.verticalGradient(
                    colors = listOf(shirtBaseColor, shirtShadowColor),
                    startY = torsoY,
                    endY = torsoY + torsoH
                )
            )

            // Clothing details based on employee role:
            when (emp.role) {
                EmployeeRole.DEVELOPER -> {
                    // Modern tech hoodie with hood drawstring and pouch pocket
                    drawRoundRect(
                        color = shirtShadowColor,
                        topLeft = Offset(centerX - 6f * s, torsoY + torsoH - 8f * s + breathOffset),
                        size = Size(12f * s, 6f * s),
                        cornerRadius = CornerRadius(2f * s, 2f * s)
                    )
                    // Drawstrings
                    drawLine(Color.White.copy(alpha = 0.7f), start = Offset(centerX - 2f * s, torsoY + 3f * s), end = Offset(centerX - 2f * s, torsoY + 11f * s), strokeWidth = 1f * s)
                    drawLine(Color.White.copy(alpha = 0.7f), start = Offset(centerX + 2f * s, torsoY + 3f * s), end = Offset(centerX + 2f * s, torsoY + 11f * s), strokeWidth = 1f * s)
                }
                EmployeeRole.DESIGNER -> {
                    // Chic designer blazer with contrasting inner tee
                    drawPath(
                        Path().apply {
                            moveTo(centerX - 3.5f * s, torsoY + breathOffset)
                            lineTo(centerX + 3.5f * s, torsoY + breathOffset)
                            lineTo(centerX, torsoY + 9f * s + breathOffset)
                            close()
                        },
                        Color(0xFF0F172A)
                    )
                    // Blazer lapels
                    drawLine(Color.White.copy(alpha = 0.35f), start = Offset(centerX - 6f * s, torsoY + breathOffset), end = Offset(centerX - 1f * s, torsoY + 12f * s + breathOffset), strokeWidth = 1.5f * s)
                    drawLine(Color.White.copy(alpha = 0.35f), start = Offset(centerX + 6f * s, torsoY + breathOffset), end = Offset(centerX + 1f * s, torsoY + 12f * s + breathOffset), strokeWidth = 1.5f * s)
                }
                EmployeeRole.MARKETER, EmployeeRole.SALES -> {
                    // Crisp tailored button-down shirt with collar and placket
                    drawLine(Color.White.copy(alpha = 0.6f), start = Offset(centerX, torsoY + breathOffset), end = Offset(centerX, torsoY + torsoH + breathOffset), strokeWidth = 1.5f * s)
                    // Buttons
                    for (b in 0..2) {
                        drawCircle(Color.White, radius = 0.8f * s, center = Offset(centerX, torsoY + 4f * s + (b * 5f * s) + breathOffset))
                    }
                }
                EmployeeRole.SYSADMIN -> {
                    // DevOps polo shirt with corporate ID badge lanyard
                    drawLine(Color(0xFF38BDF8), start = Offset(centerX - 4f * s, torsoY + breathOffset), end = Offset(centerX, torsoY + 12f * s + breathOffset), strokeWidth = 1.2f * s)
                    drawLine(Color(0xFF38BDF8), start = Offset(centerX + 4f * s, torsoY + breathOffset), end = Offset(centerX, torsoY + 12f * s + breathOffset), strokeWidth = 1.2f * s)
                    // ID Badge card
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - 2.5f * s, torsoY + 12f * s + breathOffset),
                        size = Size(5f * s, 6.5f * s),
                        cornerRadius = CornerRadius(1f * s, 1f * s)
                    )
                }
                EmployeeRole.PROJECT_MANAGER -> {
                    // Smart corporate vest / layered cardigan
                    drawLine(Color(0xFF64748B), start = Offset(centerX - 5f * s, torsoY + 2f * s + breathOffset), end = Offset(centerX - 2f * s, torsoY + 15f * s + breathOffset), strokeWidth = 1.8f * s)
                    drawLine(Color(0xFF64748B), start = Offset(centerX + 5f * s, torsoY + 2f * s + breathOffset), end = Offset(centerX + 2f * s, torsoY + 15f * s + breathOffset), strokeWidth = 1.8f * s)
                }
                EmployeeRole.QA_TESTER -> {
                    // Modern crew-neck sweater with bug-hunter pocket badge
                    drawLine(Color.White.copy(alpha = 0.5f), start = Offset(centerX - 4f * s, torsoY + 3f * s + breathOffset), end = Offset(centerX + 4f * s, torsoY + 3f * s + breathOffset), strokeWidth = 1.5f * s)
                    drawRoundRect(
                        color = Color(0xFF10B981).copy(alpha = 0.8f),
                        topLeft = Offset(centerX - 6f * s, torsoY + 8f * s + breathOffset),
                        size = Size(4f * s, 4f * s),
                        cornerRadius = CornerRadius(1f * s, 1f * s)
                    )
                }
            }

            // 4. Neck and Realistic Adult Head
            val skinColor = Color(emp.skinTone)
            val skinShadowColor = Color(
                red = (skinColor.red * 0.82f).coerceIn(0f, 1f),
                green = (skinColor.green * 0.82f).coerceIn(0f, 1f),
                blue = (skinColor.blue * 0.82f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Neck
            val neckW = 6f * s
            val neckH = 5f * s
            val neckY = torsoY - neckH + 2f * s + breathOffset
            drawRect(skinShadowColor, topLeft = Offset(centerX - neckW / 2f, neckY), size = Size(neckW, neckH))

            // Head (Oval adult head with jawline taper)
            val headW = 14f * s
            val headH = 17f * s
            val headCenter = Offset(centerX, neckY - headH / 2f + 1f * s)

            val headPath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        left = headCenter.x - headW / 2f,
                        top = headCenter.y - headH / 2f,
                        right = headCenter.x + headW / 2f,
                        bottom = headCenter.y + headH / 2f
                    )
                )
            }

            drawPath(
                path = headPath,
                brush = Brush.radialGradient(
                    colors = listOf(skinColor, skinShadowColor),
                    center = Offset(headCenter.x - 2f * s, headCenter.y - 2f * s),
                    radius = headH
                )
            )

            // Subtle facial contouring: jawline and ears
            // Left ear
            drawOval(skinShadowColor, topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - 2f * s), size = Size(3f * s, 5f * s))
            // Right ear
            drawOval(skinShadowColor, topLeft = Offset(headCenter.x + headW / 2f - 1.5f * s, headCenter.y - 2f * s), size = Size(3f * s, 5f * s))

            // Focused eye line & eyebrows (subtly looking at monitor)
            val browColor = Color(emp.hairColor)
            val eyeY = headCenter.y + 0.5f * s
            // Eyebrows
            drawLine(browColor, start = Offset(headCenter.x - 4.5f * s, eyeY - 2.5f * s), end = Offset(headCenter.x - 1.5f * s, eyeY - 2.8f * s), strokeWidth = 1.2f * s)
            drawLine(browColor, start = Offset(headCenter.x + 1.5f * s, eyeY - 2.8f * s), end = Offset(headCenter.x + 4.5f * s, eyeY - 2.5f * s), strokeWidth = 1.2f * s)
            // Focused eyes
            drawCircle(Color(0xFF1E293B), radius = 1.1f * s, center = Offset(headCenter.x - 3f * s, eyeY))
            drawCircle(Color(0xFF1E293B), radius = 1.1f * s, center = Offset(headCenter.x + 3f * s, eyeY))

            // 5. Stylized 3D Adult Hairstyles
            val hairBaseColor = Color(emp.hairColor)
            val hairHighlightColor = Color(
                red = (hairBaseColor.red * 1.3f).coerceIn(0f, 1f),
                green = (hairBaseColor.green * 1.3f).coerceIn(0f, 1f),
                blue = (hairBaseColor.blue * 1.3f).coerceIn(0f, 1f),
                alpha = 1f
            )

            val hairStyleIndex = (emp.name.hashCode().coerceAtLeast(0)) % 4
            when (hairStyleIndex) {
                0 -> {
                    // Modern textured side-part executive haircut
                    val hairPath = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 1.5f * s, headCenter.y)
                        cubicTo(
                            headCenter.x - headW / 2f, headCenter.y - headH * 0.75f,
                            headCenter.x + headW / 2f, headCenter.y - headH * 0.75f,
                            headCenter.x + headW / 2f + 1.5f * s, headCenter.y
                        )
                        lineTo(headCenter.x + headW / 2f - 2f * s, headCenter.y - 2f * s)
                        cubicTo(
                            headCenter.x + 2f * s, headCenter.y - 5f * s,
                            headCenter.x - 4f * s, headCenter.y - 4f * s,
                            headCenter.x - headW / 2f, headCenter.y - 1f * s
                        )
                        close()
                    }
                    drawPath(hairPath, hairBaseColor)
                    // Highlight sweep
                    drawArc(
                        color = hairHighlightColor.copy(alpha = 0.6f),
                        startAngle = 200f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.75f),
                        size = Size(headW, headH * 0.6f),
                        style = Stroke(width = 2.5f * s)
                    )
                }
                1 -> {
                    // Textured undercut with modern voluminous top
                    drawRoundRect(
                        color = hairBaseColor,
                        topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.7f),
                        size = Size(headW, headH * 0.55f),
                        cornerRadius = CornerRadius(6f * s, 6f * s)
                    )
                    // Side fades
                    drawRect(hairBaseColor.copy(alpha = 0.5f), topLeft = Offset(headCenter.x - headW / 2f - 1f * s, headCenter.y - 2f * s), size = Size(2.5f * s, 4f * s))
                    drawRect(hairBaseColor.copy(alpha = 0.5f), topLeft = Offset(headCenter.x + headW / 2f - 1.5f * s, headCenter.y - 2f * s), size = Size(2.5f * s, 4f * s))
                }
                2 -> {
                    // Elegant layered bob haircut with fringe
                    val bobPath = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 2f * s, headCenter.y + 4f * s)
                        lineTo(headCenter.x - headW / 2f - 1f * s, headCenter.y - headH * 0.6f)
                        cubicTo(
                            headCenter.x - 4f * s, headCenter.y - headH * 0.8f,
                            headCenter.x + 4f * s, headCenter.y - headH * 0.8f,
                            headCenter.x + headW / 2f + 1f * s, headCenter.y - headH * 0.6f
                        )
                        lineTo(headCenter.x + headW / 2f + 2f * s, headCenter.y + 4f * s)
                        lineTo(headCenter.x + headW / 2f, headCenter.y + 2f * s)
                        lineTo(headCenter.x - headW / 2f, headCenter.y + 2f * s)
                        close()
                    }
                    drawPath(bobPath, hairBaseColor)
                }
                else -> {
                    // Voluminous curls / wavy locks with rich texture
                    drawCircle(hairBaseColor, radius = headW * 0.6f, center = Offset(headCenter.x, headCenter.y - 3f * s))
                    drawCircle(hairHighlightColor.copy(alpha = 0.45f), radius = headW * 0.35f, center = Offset(headCenter.x - 3f * s, headCenter.y - 6f * s))
                }
            }

            // 6. Natural Articulated Arms, Forearms & Animated Typing Hands
            // Arms slope down from shoulders to desk surface
            val leftShoulder = Offset(centerX - torsoW / 2f + 2f * s, torsoY + 5f * s + breathOffset)
            val rightShoulder = Offset(centerX + torsoW / 2f - 2f * s, torsoY + 5f * s + breathOffset)

            if (emp.state == EmployeeState.WORKING) {
                // Alternating realistic keystroke cycle
                val leftTypingBob = (sin(localTime * 11.0) * 2.2f * s).toFloat()
                val rightTypingBob = (cos(localTime * 11.0) * 2.2f * s).toFloat()

                // Keyboard desk contact positions
                val leftHandPos = Offset(centerX - 8f * s, torsoY + torsoH + 3f * s + leftTypingBob)
                val rightHandPos = Offset(centerX + 8f * s, torsoY + torsoH + 3f * s + rightTypingBob)

                // Left Arm (Sleeve & Hand)
                val leftElbow = Offset(centerX - torsoW / 2f - 5f * s, torsoY + torsoH * 0.65f)
                drawLine(shirtBaseColor, start = leftShoulder, end = leftElbow, strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                drawLine(shirtBaseColor, start = leftElbow, end = leftHandPos, strokeWidth = 4f * s, cap = StrokeCap.Round)
                // Left hand on keys
                drawCircle(skinColor, radius = 2.8f * s, center = leftHandPos)

                // Right Arm (alternating between keyboard and mouse tracking)
                val isUsingMouse = (sin(localTime * 1.5) > 0.4)
                val actualRightHandPos = if (isUsingMouse) {
                    val mouseJiggle = (cos(localTime * 4.0) * 1.5f * s).toFloat()
                    Offset(centerX + 18f * s + mouseJiggle, torsoY + torsoH + 2f * s)
                } else {
                    rightHandPos
                }

                val rightElbow = Offset(centerX + torsoW / 2f + 5f * s, torsoY + torsoH * 0.65f)
                drawLine(shirtBaseColor, start = rightShoulder, end = rightElbow, strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                drawLine(shirtBaseColor, start = rightElbow, end = actualRightHandPos, strokeWidth = 4f * s, cap = StrokeCap.Round)
                // Right hand
                drawCircle(skinColor, radius = 2.8f * s, center = actualRightHandPos)
            } else {
                // Idle / Brainstorming posture: hand resting naturally on armrest or chin
                val leftElbow = Offset(centerX - torsoW / 2f - 4f * s, torsoY + torsoH * 0.65f)
                val leftHandPos = Offset(centerX - 9f * s, torsoY + torsoH + 2f * s)
                drawLine(shirtBaseColor, start = leftShoulder, end = leftElbow, strokeWidth = 4f * s, cap = StrokeCap.Round)
                drawLine(shirtBaseColor, start = leftElbow, end = leftHandPos, strokeWidth = 3.5f * s, cap = StrokeCap.Round)
                drawCircle(skinColor, radius = 2.6f * s, center = leftHandPos)

                // Right hand thoughtfully resting on chin
                val chinHandPos = Offset(headCenter.x + 3f * s, neckY + 1f * s)
                val rightElbow = Offset(centerX + torsoW / 2f + 3f * s, torsoY + torsoH * 0.7f)
                drawLine(shirtBaseColor, start = rightShoulder, end = rightElbow, strokeWidth = 4f * s, cap = StrokeCap.Round)
                drawLine(shirtBaseColor, start = rightElbow, end = chinHandPos, strokeWidth = 3.5f * s, cap = StrokeCap.Round)
                drawCircle(skinColor, radius = 2.6f * s, center = chinHandPos)
            }
        }
    }
}
