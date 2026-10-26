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
 * High-fidelity 3D isometric stylized adult character rendering.
 * Features realistic adult body proportions, 3D facial modeling, detailed hairstyles,
 * modern professional tech attire, ergonomic chair integration, and smooth multi-joint animations.
 * Floating debug labels and primitive shapes have been completely removed.
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

            // Micro-timing offsets derived from employee id for natural unsynchronized movements
            val idSeed = (emp.id.hashCode() and 0x7FFFFFFF) % 100
            val localTime = animTime + (idSeed * 0.12f)

            // Scaled up natural adult proportions (head-to-body ~1:6.5 seated)
            val torsoW = 30f * s
            val torsoH = 34f * s
            val torsoY = centerY - 18f * s

            // Subtle breathing rhythm (gentle chest expansion/contraction)
            val breath = (sin(localTime * 2.2) * 1.0f * s).toFloat()

            // 1. Soft contact shadow on the floor beneath chair and character
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66000000), Color(0x22000000), Color(0x00000000)),
                    center = Offset(centerX, centerY + 10f * s),
                    radius = 28f * s
                ),
                topLeft = Offset(centerX - 24f * s, centerY - 4f * s),
                size = Size(48f * s, 26f * s)
            )

            // 2. Seated Trousers / Jeans
            val trouserColor = Color(emp.trouserColor)
            val legW = 11f * s
            val legL = 22f * s

            // Left thigh
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(trouserColor, trouserColor.copy(alpha = 0.85f)),
                    startY = torsoY + torsoH - 6f * s,
                    endY = torsoY + torsoH + legL
                ),
                topLeft = Offset(centerX - 13f * s, torsoY + torsoH - 6f * s),
                size = Size(legW, legL),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )
            // Right thigh
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(trouserColor, trouserColor.copy(alpha = 0.85f)),
                    startY = torsoY + torsoH - 6f * s,
                    endY = torsoY + torsoH + legL
                ),
                topLeft = Offset(centerX + 2f * s, torsoY + torsoH - 6f * s),
                size = Size(legW, legL),
                cornerRadius = CornerRadius(4f * s, 4f * s)
            )

            // Stylish sneakers at feet with individual shoeColor
            val shoeColor = Color(emp.shoeColor)
            drawRoundRect(shoeColor, topLeft = Offset(centerX - 13.5f * s, torsoY + torsoH + legL - 5f * s), size = Size(10f * s, 6f * s), cornerRadius = CornerRadius(2.5f * s, 2.5f * s))
            drawRoundRect(shoeColor, topLeft = Offset(centerX + 3.5f * s, torsoY + torsoH + legL - 5f * s), size = Size(10f * s, 6f * s), cornerRadius = CornerRadius(2.5f * s, 2.5f * s))

            // 3. Torso with 3D Contouring and Modern Tech Attire
            val shirtBaseColor = Color(emp.shirtColor)
            val shirtHighlightColor = Color(
                red = (shirtBaseColor.red * 1.25f).coerceIn(0f, 1f),
                green = (shirtBaseColor.green * 1.25f).coerceIn(0f, 1f),
                blue = (shirtBaseColor.blue * 1.25f).coerceIn(0f, 1f),
                alpha = 1f
            )
            val shirtShadowColor = Color(
                red = (shirtBaseColor.red * 0.70f).coerceIn(0f, 1f),
                green = (shirtBaseColor.green * 0.70f).coerceIn(0f, 1f),
                blue = (shirtBaseColor.blue * 0.70f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Contoured anatomical torso
            val torsoPath = Path().apply {
                moveTo(centerX - torsoW / 2f + 1f * s, torsoY + 6f * s + breath)
                cubicTo(
                    centerX - torsoW / 2f - 2f * s, torsoY + torsoH * 0.45f + breath,
                    centerX - torsoW / 2f + 2f * s, torsoY + torsoH * 0.85f + breath,
                    centerX - torsoW / 2f + 3f * s, torsoY + torsoH + breath
                )
                lineTo(centerX + torsoW / 2f - 3f * s, torsoY + torsoH + breath)
                cubicTo(
                    centerX + torsoW / 2f - 2f * s, torsoY + torsoH * 0.85f + breath,
                    centerX + torsoW / 2f + 2f * s, torsoY + torsoH * 0.45f + breath,
                    centerX + torsoW / 2f - 1f * s, torsoY + 6f * s + breath
                )
                close()
            }

            drawPath(
                path = torsoPath,
                brush = Brush.radialGradient(
                    colors = listOf(shirtHighlightColor, shirtBaseColor, shirtShadowColor),
                    center = Offset(centerX - 4f * s, torsoY + 8f * s),
                    radius = torsoH * 0.9f
                )
            )

            // Dynamic Clothing Style Rendering
            when (emp.clothingStyle) {
                com.example.model.ClothingStyle.TECH_HOODIE -> {
                    // Modern tech zip hoodie with drawstring cords and kangaroo pouch
                    drawRoundRect(
                        color = shirtShadowColor,
                        topLeft = Offset(centerX - 8f * s, torsoY + torsoH - 11f * s + breath),
                        size = Size(16f * s, 8f * s),
                        cornerRadius = CornerRadius(2.5f * s, 2.5f * s)
                    )
                    // Zipper line
                    drawLine(Color(0xFFCBD5E1), start = Offset(centerX, torsoY + 4f * s + breath), end = Offset(centerX, torsoY + torsoH + breath), strokeWidth = 1.2f * s)
                    // White drawstring cords
                    drawLine(Color.White.copy(alpha = 0.85f), start = Offset(centerX - 3f * s, torsoY + 5f * s), end = Offset(centerX - 3f * s, torsoY + 14f * s + breath), strokeWidth = 1f * s)
                    drawLine(Color.White.copy(alpha = 0.85f), start = Offset(centerX + 3f * s, torsoY + 5f * s), end = Offset(centerX + 3f * s, torsoY + 14f * s + breath), strokeWidth = 1f * s)
                }
                com.example.model.ClothingStyle.BLAZER_CREW -> {
                    // Tailored designer blazer over crisp contrasting crew-neck tee
                    drawPath(
                        Path().apply {
                            moveTo(centerX - 5f * s, torsoY + breath)
                            lineTo(centerX + 5f * s, torsoY + breath)
                            lineTo(centerX, torsoY + 12f * s + breath)
                            close()
                        },
                        Color.White // Inner white tee
                    )
                    // Blazer lapels with drop shadow
                    drawLine(Color(0xFF0F172A), start = Offset(centerX - 8f * s, torsoY + breath), end = Offset(centerX - 1f * s, torsoY + 16f * s + breath), strokeWidth = 2.2f * s)
                    drawLine(Color(0xFF0F172A), start = Offset(centerX + 8f * s, torsoY + breath), end = Offset(centerX + 1f * s, torsoY + 16f * s + breath), strokeWidth = 2.2f * s)
                }
                com.example.model.ClothingStyle.OXFORD_BUTTON_DOWN -> {
                    // Tailored Oxford button-down shirt with collar and buttons
                    drawLine(Color.White.copy(alpha = 0.7f), start = Offset(centerX, torsoY + breath), end = Offset(centerX, torsoY + torsoH + breath), strokeWidth = 1.8f * s)
                    for (b in 0..3) {
                        drawCircle(Color.White, radius = 1.1f * s, center = Offset(centerX, torsoY + 4f * s + (b * 6f * s) + breath))
                    }
                    // Collar points
                    drawLine(Color.White.copy(alpha = 0.8f), start = Offset(centerX - 5f * s, torsoY + breath), end = Offset(centerX, torsoY + 5f * s + breath), strokeWidth = 2f * s)
                    drawLine(Color.White.copy(alpha = 0.8f), start = Offset(centerX + 5f * s, torsoY + breath), end = Offset(centerX, torsoY + 5f * s + breath), strokeWidth = 2f * s)
                }
                com.example.model.ClothingStyle.COZY_TURTLENECK -> {
                    // Minimalist roll turtleneck collar with horizontal ribbing
                    drawRoundRect(
                        color = shirtShadowColor,
                        topLeft = Offset(centerX - 6f * s, torsoY + 1f * s + breath),
                        size = Size(12f * s, 6f * s),
                        cornerRadius = CornerRadius(2f * s, 2f * s)
                    )
                    drawLine(shirtHighlightColor, start = Offset(centerX - 5f * s, torsoY + 3f * s + breath), end = Offset(centerX + 5f * s, torsoY + 3f * s + breath), strokeWidth = 1f * s)
                }
                com.example.model.ClothingStyle.CASUAL_GRAPHIC_TEE -> {
                    // Casual crewneck tee with stylized chest logo
                    drawCircle(Color(0xFF00E5FF), radius = 3f * s, center = Offset(centerX, torsoY + 12f * s + breath))
                    drawLine(Color.White, start = Offset(centerX - 2f * s, torsoY + 12f * s + breath), end = Offset(centerX + 2f * s, torsoY + 12f * s + breath), strokeWidth = 1f * s)
                }
                com.example.model.ClothingStyle.FLANNEL_PLAID -> {
                    // Plaid pattern overlay
                    drawLine(shirtShadowColor, start = Offset(centerX - 6f * s, torsoY + breath), end = Offset(centerX - 6f * s, torsoY + torsoH + breath), strokeWidth = 1.2f * s)
                    drawLine(shirtShadowColor, start = Offset(centerX + 6f * s, torsoY + breath), end = Offset(centerX + 6f * s, torsoY + torsoH + breath), strokeWidth = 1.2f * s)
                    drawLine(shirtShadowColor, start = Offset(centerX - torsoW / 2f + 2f * s, torsoY + 14f * s + breath), end = Offset(centerX + torsoW / 2f - 2f * s, torsoY + 14f * s + breath), strokeWidth = 1.2f * s)
                }
                com.example.model.ClothingStyle.LAYERED_CARDIGAN -> {
                    // Cardigan open front over light inner shirt
                    drawPath(
                        Path().apply {
                            moveTo(centerX - 4f * s, torsoY + breath)
                            lineTo(centerX + 4f * s, torsoY + breath)
                            lineTo(centerX, torsoY + 15f * s + breath)
                            close()
                        },
                        Color(0xFFF1F5F9)
                    )
                    drawLine(Color(0xFF475569), start = Offset(centerX - 6f * s, torsoY + 2f * s + breath), end = Offset(centerX - 2f * s, torsoY + 18f * s + breath), strokeWidth = 2.2f * s)
                    drawLine(Color(0xFF475569), start = Offset(centerX + 6f * s, torsoY + 2f * s + breath), end = Offset(centerX + 2f * s, torsoY + 18f * s + breath), strokeWidth = 2.2f * s)
                }
                com.example.model.ClothingStyle.DEV_POLO -> {
                    // DevOps technical polo with lanyard and ID badge
                    drawLine(Color(0xFF38BDF8), start = Offset(centerX - 5f * s, torsoY + breath), end = Offset(centerX, torsoY + 14f * s + breath), strokeWidth = 1.5f * s)
                    drawLine(Color(0xFF38BDF8), start = Offset(centerX + 5f * s, torsoY + breath), end = Offset(centerX, torsoY + 14f * s + breath), strokeWidth = 1.5f * s)
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(centerX - 3.5f * s, torsoY + 14f * s + breath),
                        size = Size(7f * s, 9f * s),
                        cornerRadius = CornerRadius(1.5f * s, 1.5f * s)
                    )
                    drawRect(Color(0xFF0284C7), topLeft = Offset(centerX - 2f * s, torsoY + 16f * s + breath), size = Size(4f * s, 3f * s))
                }
            }

            // 4. Anatomical Neck and 3D Stylized Adult Head
            val skinColor = Color(emp.skinTone)
            val skinHighlightColor = Color(
                red = (skinColor.red * 1.15f).coerceIn(0f, 1f),
                green = (skinColor.green * 1.15f).coerceIn(0f, 1f),
                blue = (skinColor.blue * 1.15f).coerceIn(0f, 1f),
                alpha = 1f
            )
            val skinShadowColor = Color(
                red = (skinColor.red * 0.78f).coerceIn(0f, 1f),
                green = (skinColor.green * 0.78f).coerceIn(0f, 1f),
                blue = (skinColor.blue * 0.78f).coerceIn(0f, 1f),
                alpha = 1f
            )

            // Neck with sternocleidomastoid shading
            val neckW = 8f * s
            val neckH = 7f * s
            val neckY = torsoY - neckH + 3f * s + breath
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(skinColor, skinShadowColor),
                    startY = neckY,
                    endY = neckY + neckH
                ),
                topLeft = Offset(centerX - neckW / 2f, neckY),
                size = Size(neckW, neckH),
                cornerRadius = CornerRadius(2f * s, 2f * s)
            )

            // 3D Sculpted Adult Head (Contoured oval with defined jawline)
            val headW = 18f * s
            val headH = 22f * s
            val headCenter = Offset(centerX, neckY - headH / 2f + 2f * s)

            val headOval = androidx.compose.ui.geometry.Rect(
                left = headCenter.x - headW / 2f,
                top = headCenter.y - headH / 2f,
                right = headCenter.x + headW / 2f,
                bottom = headCenter.y + headH / 2f
            )

            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(skinHighlightColor, skinColor, skinShadowColor),
                    center = Offset(headCenter.x - 3f * s, headCenter.y - 3f * s),
                    radius = headH * 0.85f
                ),
                topLeft = Offset(headOval.left, headOval.top),
                size = Size(headW, headH)
            )

            // 3D Ears with inner helix contouring
            drawOval(skinShadowColor, topLeft = Offset(headCenter.x - headW / 2f - 2f * s, headCenter.y - 2f * s), size = Size(3.5f * s, 6.5f * s))
            drawOval(skinShadowColor, topLeft = Offset(headCenter.x + headW / 2f - 1.5f * s, headCenter.y - 2f * s), size = Size(3.5f * s, 6.5f * s))
            drawOval(skinHighlightColor, topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - 1.5f * s), size = Size(2f * s, 4f * s))
            drawOval(skinHighlightColor, topLeft = Offset(headCenter.x + headW / 2f - 0.5f * s, headCenter.y - 1.5f * s), size = Size(2f * s, 4f * s))

            // 3D Facial Features (Focused on Workstation Screens)
            val eyeY = headCenter.y + 0.5f * s
            val browColor = Color(emp.hairColor)

            // Natural arched eyebrows
            drawLine(browColor, start = Offset(headCenter.x - 6.5f * s, eyeY - 3.5f * s), end = Offset(headCenter.x - 1.5f * s, eyeY - 4f * s), strokeWidth = 1.6f * s, cap = StrokeCap.Round)
            drawLine(browColor, start = Offset(headCenter.x + 1.5f * s, eyeY - 4f * s), end = Offset(headCenter.x + 6.5f * s, eyeY - 3.5f * s), strokeWidth = 1.6f * s, cap = StrokeCap.Round)

            // Expressive focused eyes with white sclera, dark iris, and specular catchlight
            drawOval(Color.White, topLeft = Offset(headCenter.x - 5.5f * s, eyeY - 1.8f * s), size = Size(4f * s, 3f * s))
            drawOval(Color.White, topLeft = Offset(headCenter.x + 1.5f * s, eyeY - 1.8f * s), size = Size(4f * s, 3f * s))
            // Iris / Pupil
            drawCircle(Color(0xFF0F172A), radius = 1.4f * s, center = Offset(headCenter.x - 3.5f * s, eyeY - 0.2f * s))
            drawCircle(Color(0xFF0F172A), radius = 1.4f * s, center = Offset(headCenter.x + 3.5f * s, eyeY - 0.2f * s))
            // Specular catchlight reflection
            drawCircle(Color.White, radius = 0.6f * s, center = Offset(headCenter.x - 4f * s, eyeY - 0.8f * s))
            drawCircle(Color.White, radius = 0.6f * s, center = Offset(headCenter.x + 3f * s, eyeY - 0.8f * s))

            // 3D Nose bridge with side shadow and tip highlight
            drawLine(skinShadowColor, start = Offset(headCenter.x - 0.5f * s, eyeY - 1f * s), end = Offset(headCenter.x, eyeY + 3.5f * s), strokeWidth = 1.2f * s)
            drawCircle(skinHighlightColor, radius = 1f * s, center = Offset(headCenter.x, eyeY + 3.5f * s))

            // Facial Hair (Beard, Stubble, Goatee, Moustache)
            when (emp.facialHair) {
                com.example.model.FacialHairStyle.FIVE_O_CLOCK_SHADOW -> {
                    drawArc(
                        color = Color(emp.hairColor).copy(alpha = 0.35f),
                        startAngle = 10f,
                        sweepAngle = 160f,
                        useCenter = true,
                        topLeft = Offset(headCenter.x - 7f * s, headCenter.y + 2f * s),
                        size = Size(14f * s, 10f * s)
                    )
                }
                com.example.model.FacialHairStyle.TRIMMED_BEARD -> {
                    val beardPath = Path().apply {
                        moveTo(headCenter.x - headW / 2f + 1f * s, headCenter.y + 2f * s)
                        lineTo(headCenter.x - 4f * s, headCenter.y + headH / 2f + 1.5f * s)
                        lineTo(headCenter.x + 4f * s, headCenter.y + headH / 2f + 1.5f * s)
                        lineTo(headCenter.x + headW / 2f - 1f * s, headCenter.y + 2f * s)
                        lineTo(headCenter.x + headW / 2f - 3f * s, headCenter.y + 6f * s)
                        lineTo(headCenter.x, headCenter.y + 9f * s)
                        lineTo(headCenter.x - headW / 2f + 3f * s, headCenter.y + 6f * s)
                        close()
                    }
                    drawPath(beardPath, Color(emp.hairColor).copy(alpha = 0.9f))
                }
                com.example.model.FacialHairStyle.CHIN_GOATEE -> {
                    drawOval(Color(emp.hairColor).copy(alpha = 0.9f), topLeft = Offset(headCenter.x - 3.5f * s, headCenter.y + 5.5f * s), size = Size(7f * s, 6f * s))
                }
                com.example.model.FacialHairStyle.MOUSTACHE -> {
                    drawLine(Color(emp.hairColor), start = Offset(headCenter.x - 4.5f * s, eyeY + 4.5f * s), end = Offset(headCenter.x + 4.5f * s, eyeY + 4.5f * s), strokeWidth = 2.2f * s, cap = StrokeCap.Round)
                }
                com.example.model.FacialHairStyle.NONE -> {}
            }

            // Clean mouth / chin contour
            drawLine(skinShadowColor, start = Offset(headCenter.x - 2f * s, eyeY + 6.5f * s), end = Offset(headCenter.x + 2f * s, eyeY + 6.5f * s), strokeWidth = 1.2f * s, cap = StrokeCap.Round)

            // 5. Stylized 3D Adult Hairstyles with Layered Strands & Specular Highlights
            val hairBaseColor = Color(emp.hairColor)
            val hairHighlightColor = Color(
                red = (hairBaseColor.red * 1.35f).coerceIn(0f, 1f),
                green = (hairBaseColor.green * 1.35f).coerceIn(0f, 1f),
                blue = (hairBaseColor.blue * 1.35f).coerceIn(0f, 1f),
                alpha = 1f
            )
            val hairShadowColor = Color(
                red = (hairBaseColor.red * 0.65f).coerceIn(0f, 1f),
                green = (hairBaseColor.green * 0.65f).coerceIn(0f, 1f),
                blue = (hairBaseColor.blue * 0.65f).coerceIn(0f, 1f),
                alpha = 1f
            )

            when (emp.hairStyle) {
                com.example.model.HairStyle.SHORT_POMPADOUR -> {
                    // Executive Side-Part Pompadour with taper fade
                    val pompadour = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 2f * s, headCenter.y - 1f * s)
                        cubicTo(
                            headCenter.x - headW / 2f, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f + 2f * s, headCenter.y - 1f * s
                        )
                        lineTo(headCenter.x + headW / 2f - 2f * s, headCenter.y - 4f * s)
                        cubicTo(
                            headCenter.x + 4f * s, headCenter.y - 7f * s,
                            headCenter.x - 4f * s, headCenter.y - 6f * s,
                            headCenter.x - headW / 2f + 1f * s, headCenter.y - 3f * s
                        )
                        close()
                    }
                    drawPath(
                        path = pompadour,
                        brush = Brush.verticalGradient(
                            colors = listOf(hairHighlightColor, hairBaseColor, hairShadowColor),
                            startY = headCenter.y - headH * 0.85f,
                            endY = headCenter.y
                        )
                    )
                    // Specular sheen sweep arc
                    drawArc(
                        color = hairHighlightColor.copy(alpha = 0.7f),
                        startAngle = 190f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(headCenter.x - headW * 0.45f, headCenter.y - headH * 0.85f),
                        size = Size(headW * 0.9f, headH * 0.5f),
                        style = Stroke(width = 3f * s)
                    )
                }
                com.example.model.HairStyle.TEXTURED_CROP -> {
                    // Modern Textured Undercut Crop
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(hairHighlightColor, hairBaseColor),
                            startY = headCenter.y - headH * 0.8f,
                            endY = headCenter.y - headH * 0.2f
                        ),
                        topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.78f),
                        size = Size(headW, headH * 0.65f),
                        cornerRadius = CornerRadius(8f * s, 8f * s)
                    )
                    // Tapered temple fades
                    drawRect(hairShadowColor.copy(alpha = 0.5f), topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - 3f * s), size = Size(3f * s, 6f * s))
                    drawRect(hairShadowColor.copy(alpha = 0.5f), topLeft = Offset(headCenter.x + headW / 2f - 1.5f * s, headCenter.y - 3f * s), size = Size(3f * s, 6f * s))
                }
                com.example.model.HairStyle.SLEEK_BOB -> {
                    // Sleek Layered Bob with fringe and curve
                    val bob = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 3f * s, headCenter.y + 6f * s)
                        lineTo(headCenter.x - headW / 2f - 1f * s, headCenter.y - headH * 0.7f)
                        cubicTo(
                            headCenter.x - 5f * s, headCenter.y - headH * 0.9f,
                            headCenter.x + 5f * s, headCenter.y - headH * 0.9f,
                            headCenter.x + headW / 2f + 1f * s, headCenter.y - headH * 0.7f
                        )
                        lineTo(headCenter.x + headW / 2f + 3f * s, headCenter.y + 6f * s)
                        lineTo(headCenter.x + headW / 2f, headCenter.y + 2f * s)
                        lineTo(headCenter.x - headW / 2f, headCenter.y + 2f * s)
                        close()
                    }
                    drawPath(
                        path = bob,
                        brush = Brush.verticalGradient(
                            colors = listOf(hairHighlightColor, hairBaseColor, hairShadowColor),
                            startY = headCenter.y - headH * 0.9f,
                            endY = headCenter.y + 6f * s
                        )
                    )
                }
                com.example.model.HairStyle.CURLY_AFRO -> {
                    // Voluminous Textured Curly Afro with multi-tonal highlights
                    drawCircle(hairShadowColor, radius = headW * 0.68f, center = Offset(headCenter.x, headCenter.y - 4f * s))
                    drawCircle(hairBaseColor, radius = headW * 0.62f, center = Offset(headCenter.x, headCenter.y - 4.5f * s))
                    drawCircle(hairHighlightColor.copy(alpha = 0.55f), radius = headW * 0.42f, center = Offset(headCenter.x - 4f * s, headCenter.y - 8f * s))
                }
                com.example.model.HairStyle.PONYTAIL_BUN -> {
                    // High Bun with sleek pulled sides
                    drawCircle(hairBaseColor, radius = 6f * s, center = Offset(headCenter.x, headCenter.y - headH * 0.75f))
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(hairHighlightColor, hairBaseColor)),
                        topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.6f),
                        size = Size(headW, 8f * s),
                        cornerRadius = CornerRadius(5f * s, 5f * s)
                    )
                }
                com.example.model.HairStyle.SPIKY_MODERN -> {
                    for (spike in -3..3) {
                        val sx = headCenter.x + (spike * 2.8f * s)
                        val sh = if (spike % 2 == 0) 16f * s else 13f * s
                        drawLine(hairBaseColor, start = Offset(sx, headCenter.y - 6f * s), end = Offset(sx + (spike * 0.6f * s), headCenter.y - sh), strokeWidth = 3f * s, cap = StrokeCap.Round)
                    }
                }
                com.example.model.HairStyle.LONG_WAVY -> {
                    val wavy = Path().apply {
                        moveTo(headCenter.x - headW / 2f - 4f * s, headCenter.y + 12f * s)
                        cubicTo(
                            headCenter.x - headW / 2f - 2f * s, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f + 2f * s, headCenter.y - headH * 0.85f,
                            headCenter.x + headW / 2f + 4f * s, headCenter.y + 12f * s
                        )
                        lineTo(headCenter.x + headW / 2f, headCenter.y + 3f * s)
                        lineTo(headCenter.x - headW / 2f, headCenter.y + 3f * s)
                        close()
                    }
                    drawPath(wavy, hairBaseColor)
                }
                com.example.model.HairStyle.BUZZ_FADE -> {
                    drawRoundRect(
                        hairShadowColor.copy(alpha = 0.85f),
                        topLeft = Offset(headCenter.x - headW / 2f + 1f * s, headCenter.y - headH * 0.65f),
                        size = Size(headW - 2f * s, 8f * s),
                        cornerRadius = CornerRadius(4f * s, 4f * s)
                    )
                }
                com.example.model.HairStyle.SIDE_BRAIDS -> {
                    drawRoundRect(hairBaseColor, topLeft = Offset(headCenter.x - headW / 2f, headCenter.y - headH * 0.7f), size = Size(headW, 10f * s), cornerRadius = CornerRadius(5f * s, 5f * s))
                    for (b in -2..2) {
                        drawLine(hairHighlightColor, start = Offset(headCenter.x + (b * 3.2f * s), headCenter.y - 12f * s), end = Offset(headCenter.x + (b * 3.2f * s), headCenter.y - 5f * s), strokeWidth = 1.4f * s)
                    }
                }
                com.example.model.HairStyle.MESSY_SHAG -> {
                    drawRoundRect(hairBaseColor, topLeft = Offset(headCenter.x - headW / 2f - 2f * s, headCenter.y - headH * 0.8f), size = Size(headW + 4f * s, 12f * s), cornerRadius = CornerRadius(6f * s, 6f * s))
                    drawLine(hairHighlightColor, start = Offset(headCenter.x - 5f * s, headCenter.y - 9f * s), end = Offset(headCenter.x - 1f * s, headCenter.y - 2f * s), strokeWidth = 1.8f * s)
                }
            }

            // 6. Accessories (Glasses, Headphones, Earbuds, Beanie, Cap)
            when (emp.accessory) {
                com.example.model.EmployeeAccessory.BLACK_ACETATE_GLASSES -> {
                    val frameColor = Color(0xFF0F172A)
                    drawRoundRect(frameColor, topLeft = Offset(headCenter.x - 7f * s, eyeY - 3f * s), size = Size(6f * s, 5f * s), cornerRadius = CornerRadius(1.2f * s, 1.2f * s), style = Stroke(width = 1.3f * s))
                    drawRoundRect(frameColor, topLeft = Offset(headCenter.x + 1f * s, eyeY - 3f * s), size = Size(6f * s, 5f * s), cornerRadius = CornerRadius(1.2f * s, 1.2f * s), style = Stroke(width = 1.3f * s))
                    drawLine(frameColor, start = Offset(headCenter.x - 1f * s, eyeY - 0.5f * s), end = Offset(headCenter.x + 1f * s, eyeY - 0.5f * s), strokeWidth = 1.3f * s)
                    drawLine(Color.White.copy(alpha = 0.55f), start = Offset(headCenter.x - 6f * s, eyeY - 2f * s), end = Offset(headCenter.x - 3f * s, eyeY + 1f * s), strokeWidth = 0.8f * s)
                }
                com.example.model.EmployeeAccessory.GOLD_WIRE_GLASSES -> {
                    val frameColor = Color(0xFFFBBF24)
                    drawCircle(frameColor, radius = 2.8f * s, center = Offset(headCenter.x - 4f * s, eyeY), style = Stroke(width = 1f * s))
                    drawCircle(frameColor, radius = 2.8f * s, center = Offset(headCenter.x + 4f * s, eyeY), style = Stroke(width = 1f * s))
                    drawLine(frameColor, start = Offset(headCenter.x - 1.2f * s, eyeY), end = Offset(headCenter.x + 1.2f * s, eyeY), strokeWidth = 1f * s)
                }
                com.example.model.EmployeeAccessory.STUDIO_HEADPHONES -> {
                    val hpColor = Color(0xFF0F172A)
                    val accent = Color(0xFF00E5FF)
                    // Headband around neck
                    drawArc(hpColor, startAngle = 10f, sweepAngle = 160f, useCenter = false, topLeft = Offset(centerX - 11f * s, torsoY - 2f * s + breath), size = Size(22f * s, 14f * s), style = Stroke(width = 3f * s))
                    drawOval(hpColor, topLeft = Offset(centerX - 13f * s, torsoY + breath), size = Size(6f * s, 8f * s))
                    drawOval(accent, topLeft = Offset(centerX - 12f * s, torsoY + 1f * s + breath), size = Size(4f * s, 6f * s))
                    drawOval(hpColor, topLeft = Offset(centerX + 7f * s, torsoY + breath), size = Size(6f * s, 8f * s))
                    drawOval(accent, topLeft = Offset(centerX + 8f * s, torsoY + 1f * s + breath), size = Size(4f * s, 6f * s))
                }
                com.example.model.EmployeeAccessory.AIRPOD_PROS -> {
                    drawCircle(Color.White, radius = 1.3f * s, center = Offset(headCenter.x - headW / 2f - 1f * s, eyeY + 1f * s))
                    drawLine(Color.White, start = Offset(headCenter.x - headW / 2f - 1f * s, eyeY + 1f * s), end = Offset(headCenter.x - headW / 2f - 2f * s, eyeY + 4f * s), strokeWidth = 1f * s)
                    drawCircle(Color.White, radius = 1.3f * s, center = Offset(headCenter.x + headW / 2f + 1f * s, eyeY + 1f * s))
                    drawLine(Color.White, start = Offset(headCenter.x + headW / 2f + 1f * s, eyeY + 1f * s), end = Offset(headCenter.x + headW / 2f + 2f * s, eyeY + 4f * s), strokeWidth = 1f * s)
                }
                com.example.model.EmployeeAccessory.STREETWEAR_CAP -> {
                    drawRoundRect(shirtBaseColor, topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - headH * 0.75f), size = Size(headW + 3f * s, 10f * s), cornerRadius = CornerRadius(5f * s, 5f * s))
                    drawRoundRect(shirtShadowColor, topLeft = Offset(headCenter.x - headW / 2f - 3f * s, headCenter.y - 2f * s), size = Size(headW + 6f * s, 3f * s), cornerRadius = CornerRadius(1.5f * s, 1.5f * s))
                }
                com.example.model.EmployeeAccessory.HIPSTER_BEANIE -> {
                    val beanieColor = Color(0xFFF97316)
                    drawRoundRect(beanieColor, topLeft = Offset(headCenter.x - headW / 2f - 1.5f * s, headCenter.y - headH * 0.9f), size = Size(headW + 3f * s, 12f * s), cornerRadius = CornerRadius(8f * s, 8f * s))
                    drawRoundRect(Color(0xFFEA580C), topLeft = Offset(headCenter.x - headW / 2f - 2f * s, headCenter.y - 4.5f * s), size = Size(headW + 4f * s, 3.5f * s), cornerRadius = CornerRadius(2f * s, 2f * s))
                }
                com.example.model.EmployeeAccessory.SMART_WATCH, com.example.model.EmployeeAccessory.NONE -> {}
            }

            // 6. Articulated Arms, Forearms & Natural Typing / Mouse Movement
            val leftShoulder = Offset(centerX - torsoW / 2f + 3f * s, torsoY + 8f * s + breath)
            val rightShoulder = Offset(centerX + torsoW / 2f - 3f * s, torsoY + 8f * s + breath)

            if (emp.state == EmployeeState.WORKING) {
                // Alternating realistic keystroke timing
                val leftTypingBob = (sin(localTime * 12.0) * 2.8f * s).toFloat()
                val rightTypingBob = (cos(localTime * 12.0) * 2.8f * s).toFloat()

                // Keyboard contact coordinates on desk
                val leftHandPos = Offset(centerX - 10f * s, torsoY + torsoH + 4f * s + leftTypingBob)
                val rightHandPos = Offset(centerX + 10f * s, torsoY + torsoH + 4f * s + rightTypingBob)

                // Left Arm: Shoulder -> Elbow -> Forearm -> Hand
                val leftElbow = Offset(centerX - torsoW / 2f - 7f * s, torsoY + torsoH * 0.62f)
                drawLine(shirtBaseColor, start = leftShoulder, end = leftElbow, strokeWidth = 6f * s, cap = StrokeCap.Round)
                drawLine(shirtHighlightColor, start = leftShoulder, end = leftElbow, strokeWidth = 3f * s, cap = StrokeCap.Round)
                drawLine(skinColor, start = leftElbow, end = leftHandPos, strokeWidth = 5f * s, cap = StrokeCap.Round)
                // Left Hand with articulated fingers resting on keys
                drawCircle(skinColor, radius = 3.6f * s, center = leftHandPos)

                // Right Arm: Alternates between keyboard typing and mouse tracking
                val isUsingMouse = (sin(localTime * 1.8) > 0.35)
                val actualRightHandPos = if (isUsingMouse) {
                    val mouseJiggle = (cos(localTime * 4.5) * 2.0f * s).toFloat()
                    Offset(centerX + 22f * s + mouseJiggle, torsoY + torsoH + 3f * s)
                } else {
                    rightHandPos
                }

                val rightElbow = Offset(centerX + torsoW / 2f + 7f * s, torsoY + torsoH * 0.62f)
                drawLine(shirtBaseColor, start = rightShoulder, end = rightElbow, strokeWidth = 6f * s, cap = StrokeCap.Round)
                drawLine(shirtHighlightColor, start = rightShoulder, end = rightElbow, strokeWidth = 3f * s, cap = StrokeCap.Round)
                drawLine(skinColor, start = rightElbow, end = actualRightHandPos, strokeWidth = 5f * s, cap = StrokeCap.Round)
                // Right Hand on mouse or keys
                drawCircle(skinColor, radius = 3.6f * s, center = actualRightHandPos)
            } else {
                // Thoughtful Brainstorming Posture: Hand on chin
                val leftElbow = Offset(centerX - torsoW / 2f - 6f * s, torsoY + torsoH * 0.65f)
                val leftHandPos = Offset(centerX - 12f * s, torsoY + torsoH + 2f * s)
                drawLine(shirtBaseColor, start = leftShoulder, end = leftElbow, strokeWidth = 5.5f * s, cap = StrokeCap.Round)
                drawLine(skinColor, start = leftElbow, end = leftHandPos, strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                drawCircle(skinColor, radius = 3.4f * s, center = leftHandPos)

                // Right hand thoughtfully resting under chin
                val chinPos = Offset(headCenter.x + 4f * s, neckY + 1f * s)
                val rightElbow = Offset(centerX + torsoW / 2f + 5f * s, torsoY + torsoH * 0.72f)
                drawLine(shirtBaseColor, start = rightShoulder, end = rightElbow, strokeWidth = 5.5f * s, cap = StrokeCap.Round)
                drawLine(skinColor, start = rightElbow, end = chinPos, strokeWidth = 4.5f * s, cap = StrokeCap.Round)
                drawCircle(skinColor, radius = 3.4f * s, center = chinPos)
            }
        }
    }
}
