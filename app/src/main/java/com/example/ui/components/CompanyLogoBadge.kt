package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun getLogoIconVector(iconKey: String): ImageVector {
    return when (iconKey.lowercase()) {
        "rocket" -> Icons.Default.RocketLaunch
        "terminal" -> Icons.Default.Terminal
        "bolt" -> Icons.Default.Bolt
        "brain" -> Icons.Default.Psychology
        "globe" -> Icons.Default.Public
        "building" -> Icons.Default.Apartment
        "diamond" -> Icons.Default.Diamond
        "cpu" -> Icons.Default.Memory
        "code" -> Icons.Default.Code
        "layers" -> Icons.Default.Layers
        "cloud" -> Icons.Default.Cloud
        "shield" -> Icons.Default.Shield
        else -> Icons.Default.RocketLaunch
    }
}

fun getLogoShape(shapeKey: String): Shape {
    return when (shapeKey.lowercase()) {
        "circle" -> CircleShape
        "diamond" -> CutCornerShape(percent = 40)
        "shield" -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
        else -> RoundedCornerShape(12.dp) // default squircle / rounded
    }
}

@Composable
fun CompanyLogoBadge(
    iconKey: String,
    colorLong: Long,
    shapeKey: String = "rounded",
    sizeDp: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val brandColor = Color(colorLong)
    val shape = getLogoShape(shapeKey)
    val iconVector = getLogoIconVector(iconKey)

    Box(
        modifier = modifier
            .size(sizeDp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        brandColor.copy(alpha = 0.28f),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.5.dp, brandColor.copy(alpha = 0.85f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = "Company Crest",
            tint = brandColor,
            modifier = Modifier.size(sizeDp * 0.58f)
        )
    }
}
