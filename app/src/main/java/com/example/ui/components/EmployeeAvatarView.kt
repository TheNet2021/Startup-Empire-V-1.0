package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.*

/**
 * High-fidelity, completely distinctive vector avatar portrait for every employee.
 * Renders the employee's exact skin tone, hair style, hair color, clothing neckline,
 * facial hair, and signature accessories (glasses, headphones, beanies, caps, earbuds).
 */
@Composable
fun EmployeeAvatarView(
    employee: Employee,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, Color(employee.skinTone).copy(alpha = 0.8f), CircleShape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = this.size.width
            val h = this.size.height
            val s = w / 44f // normalized scale factor

            // 1. Subtle radial gradient backdrop
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                    center = Offset(w * 0.5f, h * 0.4f),
                    radius = w * 0.7f
                )
            )

            // 2. Clothing & Shoulders
            val shirtBase = Color(employee.shirtColor)
            val shirtDark = Color(
                (shirtBase.red * 0.7f).coerceIn(0f, 1f),
                (shirtBase.green * 0.7f).coerceIn(0f, 1f),
                (shirtBase.blue * 0.7f).coerceIn(0f, 1f)
            )
            val skinBase = Color(employee.skinTone)
            val skinDark = Color(
                (skinBase.red * 0.75f).coerceIn(0f, 1f),
                (skinBase.green * 0.75f).coerceIn(0f, 1f),
                (skinBase.blue * 0.75f).coerceIn(0f, 1f)
            )
            val skinLight = Color(
                (skinBase.red * 1.15f).coerceIn(0f, 1f),
                (skinBase.green * 1.15f).coerceIn(0f, 1f),
                (skinBase.blue * 1.15f).coerceIn(0f, 1f)
            )
            val hairBase = Color(employee.hairColor)
            val hairLight = Color(
                (hairBase.red * 1.3f).coerceIn(0f, 1f),
                (hairBase.green * 1.3f).coerceIn(0f, 1f),
                (hairBase.blue * 1.3f).coerceIn(0f, 1f)
            )
            val hairDark = Color(
                (hairBase.red * 0.65f).coerceIn(0f, 1f),
                (hairBase.green * 0.65f).coerceIn(0f, 1f),
                (hairBase.blue * 0.65f).coerceIn(0f, 1f)
            )

            // Shoulders path
            val shoulderPath = Path().apply {
                moveTo(2f * s, h)
                cubicTo(6f * s, h - 14f * s, w - 6f * s, h - 14f * s, w - 2f * s, h)
                close()
            }
            drawPath(shoulderPath, shirtBase)

            // Neckline specific to ClothingStyle
            when (employee.clothingStyle) {
                ClothingStyle.TECH_HOODIE -> {
                    // Kangaroo zipper & hood outline
                    drawLine(Color(0xFFCBD5E1), start = Offset(w * 0.5f, h - 11f * s), end = Offset(w * 0.5f, h), strokeWidth = 1.2f * s)
                    drawLine(Color.White.copy(alpha = 0.9f), start = Offset(w * 0.5f - 4f * s, h - 10f * s), end = Offset(w * 0.5f - 4f * s, h - 2f * s), strokeWidth = 0.9f * s)
                    drawLine(Color.White.copy(alpha = 0.9f), start = Offset(w * 0.5f + 4f * s, h - 10f * s), end = Offset(w * 0.5f + 4f * s, h - 2f * s), strokeWidth = 0.9f * s)
                }
                ClothingStyle.BLAZER_CREW -> {
                    // White inner tee triangle
                    val innerTee = Path().apply {
                        moveTo(w * 0.5f - 5f * s, h - 12f * s)
                        lineTo(w * 0.5f + 5f * s, h - 12f * s)
                        lineTo(w * 0.5f, h - 5f * s)
                        close()
                    }
                    drawPath(innerTee, Color.White)
                    // Blazer lapels
                    drawLine(shirtDark, start = Offset(w * 0.5f - 8f * s, h - 12f * s), end = Offset(w * 0.5f - 1f * s, h), strokeWidth = 1.8f * s)
                    drawLine(shirtDark, start = Offset(w * 0.5f + 8f * s, h - 12f * s), end = Offset(w * 0.5f + 1f * s, h), strokeWidth = 1.8f * s)
                }
                ClothingStyle.OXFORD_BUTTON_DOWN -> {
                    // Collar points & center placket
                    drawLine(Color.White.copy(alpha = 0.8f), start = Offset(w * 0.5f, h - 11f * s), end = Offset(w * 0.5f, h), strokeWidth = 1.2f * s)
                    drawCircle(Color.White, radius = 0.8f * s, center = Offset(w * 0.5f, h - 7f * s))
                    drawCircle(Color.White, radius = 0.8f * s, center = Offset(w * 0.5f, h - 3f * s))
                    drawLine(shirtDark, start = Offset(w * 0.5f - 5f * s, h - 12f * s), end = Offset(w * 0.5f, h - 9f * s), strokeWidth = 1.4f * s)
                    drawLine(shirtDark, start = Offset(w * 0.5f + 5f * s, h - 12f * s), end = Offset(w * 0.5f, h - 9f * s), strokeWidth = 1.4f * s)
                }
                ClothingStyle.COZY_TURTLENECK -> {
                    drawRoundRect(
                        color = shirtDark,
                        topLeft = Offset(w * 0.5f - 6f * s, h - 13f * s),
                        size = Size(12f * s, 6f * s),
                        cornerRadius = CornerRadius(2f * s, 2f * s)
                    )
                }
                ClothingStyle.CASUAL_GRAPHIC_TEE -> {
                    // Chest cyan logo accent
                    drawCircle(Color(0xFF00E5FF), radius = 2f * s, center = Offset(w * 0.5f, h - 6f * s))
                }
                ClothingStyle.FLANNEL_PLAID -> {
                    // Plaid cross-stripes
                    drawLine(shirtDark, start = Offset(w * 0.5f - 7f * s, h - 14f * s), end = Offset(w * 0.5f - 7f * s, h), strokeWidth = 1f * s)
                    drawLine(shirtDark, start = Offset(w * 0.5f + 7f * s, h - 14f * s), end = Offset(w * 0.5f + 7f * s, h), strokeWidth = 1f * s)
                    drawLine(shirtDark, start = Offset(2f * s, h - 7f * s), end = Offset(w - 2f * s, h - 7f * s), strokeWidth = 1f * s)
                }
                ClothingStyle.LAYERED_CARDIGAN -> {
                    val inner = Path().apply {
                        moveTo(w * 0.5f - 4f * s, h - 12f * s)
                        lineTo(w * 0.5f + 4f * s, h - 12f * s)
                        lineTo(w * 0.5f, h - 6f * s)
                        close()
                    }
                    drawPath(inner, Color(0xFFF1F5F9))
                    drawLine(shirtDark, start = Offset(w * 0.5f - 6f * s, h - 12f * s), end = Offset(w * 0.5f, h), strokeWidth = 1.5f * s)
                    drawLine(shirtDark, start = Offset(w * 0.5f + 6f * s, h - 12f * s), end = Offset(w * 0.5f, h), strokeWidth = 1.5f * s)
                }
                ClothingStyle.DEV_POLO -> {
                    // Polo placket and badge
                    drawLine(shirtDark, start = Offset(w * 0.5f, h - 12f * s), end = Offset(w * 0.5f, h - 5f * s), strokeWidth = 1.2f * s)
                    drawRect(Color(0xFF38BDF8), topLeft = Offset(w * 0.5f + 5f * s, h - 9f * s), size = Size(3f * s, 4f * s))
                }
            }

            // 3. Neck
            drawRect(
                color = skinDark,
                topLeft = Offset(w * 0.5f - 4.5f * s, h - 14f * s),
                size = Size(9f * s, 6f * s)
            )

            // 4. Head & Face
            val headW = 18f * s
            val headH = 22f * s
            val headCenter = Offset(w * 0.5f, h * 0.44f)

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(skinLight, skinBase, skinDark),
                    center = Offset(headCenter.x - 2f * s, headCenter.y - 2f * s),
                    radius = headH * 0.75f
                ),
                topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH / 2f),
                size = Size(headW, headH)
            )

            // Ears
            drawOval(skinDark, topLeft = Offset(headCenter.x - headW / 2f - 2f * s, headCenter.y - 1f * s), size = Size(3f * s, 5f * s))
            drawOval(skinDark, topLeft = Offset(headCenter.x + headW / 2f - 1f * s, headCenter.y - 1f * s), size = Size(3f * s, 5f * s))

            // Eyes & Eyebrows
            val eyeY = headCenter.y + 0.5f * s
            // Eyebrows
            drawLine(hairBase, start = Offset(headCenter.x - 6f * s, eyeY - 3f * s), end = Offset(headCenter.x - 2f * s, eyeY - 3.5f * s), strokeWidth = 1.2f * s, cap = StrokeCap.Round)
            drawLine(hairBase, start = Offset(headCenter.x + 2f * s, eyeY - 3.5f * s), end = Offset(headCenter.x + 6f * s, eyeY - 3f * s), strokeWidth = 1.2f * s, cap = StrokeCap.Round)

            // Eyes
            drawOval(Color.White, topLeft = Offset(headCenter.x - 5.5f * s, eyeY - 1.5f * s), size = Size(3.5f * s, 2.5f * s))
            drawOval(Color.White, topLeft = Offset(headCenter.x + 2f * s, eyeY - 1.5f * s), size = Size(3.5f * s, 2.5f * s))
            drawCircle(Color(0xFF0F172A), radius = 1.2f * s, center = Offset(headCenter.x - 3.8f * s, eyeY - 0.2f * s))
            drawCircle(Color(0xFF0F172A), radius = 1.2f * s, center = Offset(headCenter.x + 3.8f * s, eyeY - 0.2f * s))
            drawCircle(Color.White, radius = 0.5f * s, center = Offset(headCenter.x - 4.2f * s, eyeY - 0.6f * s))
            drawCircle(Color.White, radius = 0.5f * s, center = Offset(headCenter.x + 3.4f * s, eyeY - 0.6f * s))

            // Nose
            drawLine(skinDark, start = Offset(headCenter.x - 0.5f * s, eyeY - 0.5f * s), end = Offset(headCenter.x, eyeY + 3f * s), strokeWidth = 1f * s)

            // Facial Hair
            when (employee.facialHair) {
                FacialHairStyle.FIVE_O_CLOCK_SHADOW -> {
                    drawArc(
                        color = hairDark.copy(alpha = 0.35f),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = true,
                        topLeft = Offset(headCenter.x - 6f * s, headCenter.y + 2f * s),
                        size = Size(12f * s, 8f * s)
                    )
                }
                FacialHairStyle.TRIMMED_BEARD -> {
                    val beard = Path().apply {
                        moveTo(headCenter.x - headW / 2f + 1f * s, headCenter.y + 2f * s)
                        lineTo(headCenter.x - 4f * s, headCenter.y + headH / 2f + 1f * s)
                        lineTo(headCenter.x + 4f * s, headCenter.y + headH / 2f + 1f * s)
                        lineTo(headCenter.x + headW / 2f - 1f * s, headCenter.y + 2f * s)
                        lineTo(headCenter.x + headW / 2f - 3f * s, headCenter.y + 5f * s)
                        lineTo(headCenter.x, headCenter.y + 8f * s)
                        lineTo(headCenter.x - headW / 2f + 3f * s, headCenter.y + 5f * s)
                        close()
                    }
                    drawPath(beard, hairDark)
                }
                FacialHairStyle.CHIN_GOATEE -> {
                    drawOval(hairDark, topLeft = Offset(headCenter.x - 3f * s, headCenter.y + 5f * s), size = Size(6f * s, 5f * s))
                }
                FacialHairStyle.MOUSTACHE -> {
                    drawLine(hairDark, start = Offset(headCenter.x - 4f * s, eyeY + 4f * s), end = Offset(headCenter.x + 4f * s, eyeY + 4f * s), strokeWidth = 2f * s, cap = StrokeCap.Round)
                }
                FacialHairStyle.NONE -> {
                    // Just mouth
                    drawLine(skinDark, start = Offset(headCenter.x - 2f * s, eyeY + 5.5f * s), end = Offset(headCenter.x + 2f * s, eyeY + 5.5f * s), strokeWidth = 1f * s, cap = StrokeCap.Round)
                }
            }

            // 5. Hair Style
            when (employee.hairStyle) {
                HairStyle.SHORT_POMPADOUR -> {
                    val pomp = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 1f * s, headCenter.y - 1f * s)
                        cubicTo(
                            headCenter.x - headW / 2f, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f + 1f * s, headCenter.y - 1f * s
                        )
                        lineTo(headCenter.x + headW / 2f - 1f * s, headCenter.y - 4f * s)
                        cubicTo(headCenter.x + 3f * s, headCenter.y - 6f * s, headCenter.x - 3f * s, headCenter.y - 5f * s, headCenter.x - headW / 2f + 1f * s, headCenter.y - 3f * s)
                        close()
                    }
                    drawPath(pomp, hairBase)
                    drawArc(hairLight, startAngle = 190f, sweepAngle = 100f, useCenter = false, topLeft = Offset(headCenter.x - 6f * s, headCenter.y - 16f * s), size = Size(12f * s, 6f * s), style = Stroke(width = 1.5f * s))
                }
                HairStyle.TEXTURED_CROP -> {
                    drawRoundRect(hairBase, topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.75f), size = Size(headW, headH * 0.6f), cornerRadius = CornerRadius(6f * s, 6f * s))
                    drawCircle(hairLight, radius = 2.5f * s, center = Offset(headCenter.x - 2f * s, headCenter.y - 9f * s))
                }
                HairStyle.SLEEK_BOB -> {
                    val bob = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 3f * s, headCenter.y + 6f * s)
                        lineTo(headCenter.x - headW / 2f - 1f * s, headCenter.y - headH * 0.7f)
                        cubicTo(headCenter.x - 4f * s, headCenter.y - headH * 0.9f, headCenter.x + 4f * s, headCenter.y - headH * 0.9f, headCenter.x + headW / 2f + 1f * s, headCenter.y - headH * 0.7f)
                        lineTo(headCenter.x + headW / 2f + 3f * s, headCenter.y + 6f * s)
                        lineTo(headCenter.x + headW / 2f, headCenter.y + 2f * s)
                        lineTo(headCenter.x - headW / 2f, headCenter.y + 2f * s)
                        close()
                    }
                    drawPath(bob, hairBase)
                }
                HairStyle.CURLY_AFRO -> {
                    drawCircle(hairDark, radius = headW * 0.65f, center = Offset(headCenter.x, headCenter.y - 3f * s))
                    drawCircle(hairBase, radius = headW * 0.6f, center = Offset(headCenter.x, headCenter.y - 3.5f * s))
                    drawCircle(hairLight.copy(alpha = 0.5f), radius = headW * 0.35f, center = Offset(headCenter.x - 3f * s, headCenter.y - 6f * s))
                }
                HairStyle.PONYTAIL_BUN -> {
                    drawCircle(hairBase, radius = 5.5f * s, center = Offset(headCenter.x, headCenter.y - headH * 0.65f))
                    drawRoundRect(hairDark, topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.55f), size = Size(headW, 7f * s), cornerRadius = CornerRadius(4f * s, 4f * s))
                }
                HairStyle.SPIKY_MODERN -> {
                    for (spike in -3..3) {
                        val sx = headCenter.x + (spike * 2.5f * s)
                        val sh = if (spike % 2 == 0) 14f * s else 11f * s
                        drawLine(hairBase, start = Offset(sx, headCenter.y - 6f * s), end = Offset(sx + (spike * 0.5f * s), headCenter.y - sh), strokeWidth = 2.5f * s, cap = StrokeCap.Round)
                    }
                }
                HairStyle.LONG_WAVY -> {
                    val wavy = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 3f * s, headCenter.y + 10f * s)
                        cubicTo(headCenter.x - headW / 2f, headCenter.y - headH * 0.8f, headCenter.x + headW / 2f, headCenter.y - headH * 0.8f, headCenter.x + headW / 2f + 3f * s, headCenter.y + 10f * s)
                        lineTo(headCenter.x + headW / 2f - 1f * s, headCenter.y + 3f * s)
                        lineTo(headCenter.x - headW / 2f + 1f * s, headCenter.y + 3f * s)
                        close()
                    }
                    drawPath(wavy, hairBase)
                }
                HairStyle.BUZZ_FADE -> {
                    drawRoundRect(hairDark.copy(alpha = 0.8f), topLeft = Offset(headCenter.x - headW / 2f + 1f * s, headCenter.y - headH * 0.55f), size = Size(headW - 2f * s, 6f * s), cornerRadius = CornerRadius(3f * s, 3f * s))
                }
                HairStyle.SIDE_BRAIDS -> {
                    drawRoundRect(hairBase, topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.6f), size = Size(headW, 8f * s), cornerRadius = CornerRadius(4f * s, 4f * s))
                    for (b in -2..2) {
                        drawLine(hairLight, start = Offset(headCenter.x + (b * 3f * s), headCenter.y - 10f * s), end = Offset(headCenter.x + (b * 3f * s), headCenter.y - 4f * s), strokeWidth = 1.2f * s)
                    }
                }
                HairStyle.MESSY_SHAG -> {
                    drawRoundRect(hairBase, topLeft = Offset(headCenter.x - headW / 2f - 2f * s, headCenter.y - headH * 0.7f), size = Size(headW + 4f * s, 10f * s), cornerRadius = CornerRadius(5f * s, 5f * s))
                    drawLine(hairLight, start = Offset(headCenter.x - 4f * s, headCenter.y - 8f * s), end = Offset(headCenter.x - 1f * s, headCenter.y - 2f * s), strokeWidth = 1.5f * s)
                }
            }

            // 6. Accessories
            when (employee.accessory) {
                EmployeeAccessory.BLACK_ACETATE_GLASSES -> {
                    val frameColor = Color(0xFF0F172A)
                    // Left and right rims
                    drawRoundRect(frameColor, topLeft = Offset(headCenter.x - 6.5f * s, eyeY - 2.5f * s), size = Size(5.5f * s, 4.5f * s), cornerRadius = CornerRadius(1f * s, 1f * s), style = Stroke(width = 1.2f * s))
                    drawRoundRect(frameColor, topLeft = Offset(headCenter.x + 1f * s, eyeY - 2.5f * s), size = Size(5.5f * s, 4.5f * s), cornerRadius = CornerRadius(1f * s, 1f * s), style = Stroke(width = 1.2f * s))
                    // Bridge
                    drawLine(frameColor, start = Offset(headCenter.x - 1f * s, eyeY - 0.5f * s), end = Offset(headCenter.x + 1f * s, eyeY - 0.5f * s), strokeWidth = 1.2f * s)
                    // Glass reflection
                    drawLine(Color.White.copy(alpha = 0.5f), start = Offset(headCenter.x - 5.5f * s, eyeY - 1.5f * s), end = Offset(headCenter.x - 2.5f * s, eyeY + 0.5f * s), strokeWidth = 0.8f * s)
                }
                EmployeeAccessory.GOLD_WIRE_GLASSES -> {
                    val frameColor = Color(0xFFFBBF24)
                    drawCircle(frameColor, radius = 2.4f * s, center = Offset(headCenter.x - 3.8f * s, eyeY), style = Stroke(width = 1f * s))
                    drawCircle(frameColor, radius = 2.4f * s, center = Offset(headCenter.x + 3.8f * s, eyeY), style = Stroke(width = 1f * s))
                    drawLine(frameColor, start = Offset(headCenter.x - 1.4f * s, eyeY), end = Offset(headCenter.x + 1.4f * s, eyeY), strokeWidth = 1f * s)
                }
                EmployeeAccessory.STUDIO_HEADPHONES -> {
                    val hpColor = Color(0xFF0F172A)
                    val accentColor = Color(0xFF00E5FF)
                    // Headband arch around neck
                    drawArc(hpColor, startAngle = 10f, sweepAngle = 160f, useCenter = false, topLeft = Offset(w * 0.5f - 10f * s, h - 18f * s), size = Size(20f * s, 12f * s), style = Stroke(width = 2.5f * s))
                    // Earcups resting on shoulders
                    drawOval(hpColor, topLeft = Offset(w * 0.5f - 11f * s, h - 16f * s), size = Size(5f * s, 7f * s))
                    drawOval(accentColor, topLeft = Offset(w * 0.5f - 10f * s, h - 15f * s), size = Size(3f * s, 5f * s))
                    drawOval(hpColor, topLeft = Offset(w * 0.5f + 6f * s, h - 16f * s), size = Size(5f * s, 7f * s))
                    drawOval(accentColor, topLeft = Offset(w * 0.5f + 7f * s, h - 15f * s), size = Size(3f * s, 5f * s))
                }
                EmployeeAccessory.AIRPOD_PROS -> {
                    drawCircle(Color.White, radius = 1.2f * s, center = Offset(headCenter.x - headW / 2f - 0.5f * s, eyeY + 1f * s))
                    drawLine(Color.White, start = Offset(headCenter.x - headW / 2f - 0.5f * s, eyeY + 1f * s), end = Offset(headCenter.x - headW / 2f - 1.5f * s, eyeY + 3.5f * s), strokeWidth = 0.9f * s)
                    drawCircle(Color.White, radius = 1.2f * s, center = Offset(headCenter.x + headW / 2f + 0.5f * s, eyeY + 1f * s))
                    drawLine(Color.White, start = Offset(headCenter.x + headW / 2f + 0.5f * s, eyeY + 1f * s), end = Offset(headCenter.x + headW / 2f + 1.5f * s, eyeY + 3.5f * s), strokeWidth = 0.9f * s)
                }
                EmployeeAccessory.STREETWEAR_CAP -> {
                    // Cap crown & brim
                    drawRoundRect(shirtBase, topLeft = Offset(headCenter.x - headW / 2f - 1f * s, headCenter.y - headH * 0.7f), size = Size(headW + 2f * s, 9f * s), cornerRadius = CornerRadius(5f * s, 5f * s))
                    // Visor
                    drawRoundRect(shirtDark, topLeft = Offset(headCenter.x - headW / 2f - 3f * s, headCenter.y - 2f * s), size = Size(headW + 6f * s, 2.5f * s), cornerRadius = CornerRadius(1.2f * s, 1.2f * s))
                }
                EmployeeAccessory.HIPSTER_BEANIE -> {
                    // Slouchy beanie covering top of head
                    val beanieColor = Color(0xFFF97316)
                    drawRoundRect(beanieColor, topLeft = Offset(headCenter.x - headW / 2f - 1f * s, headCenter.y - headH * 0.85f), size = Size(headW + 2f * s, 11f * s), cornerRadius = CornerRadius(7f * s, 7f * s))
                    // Beanie cuff
                    drawRoundRect(Color(0xFFEA580C), topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - 4f * s), size = Size(headW + 3f * s, 3f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                }
                EmployeeAccessory.SMART_WATCH -> {
                    // Small smart wrist indicator in bottom right
                    drawRoundRect(Color(0xFF0F172A), topLeft = Offset(w - 8f * s, h - 8f * s), size = Size(6f * s, 6f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                    drawCircle(Color(0xFF00E5FF), radius = 1.2f * s, center = Offset(w - 5f * s, h - 5f * s))
                }
                EmployeeAccessory.NONE -> {}
            }
        }
    }
}
