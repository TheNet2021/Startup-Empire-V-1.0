package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameOverDialog(
    debtAmount: Double,
    onRestructureBailout: () -> Unit,
    onRestartNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force action */ },
        containerColor = Color(0xFF1F1212),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                Text("BANKRUPTCY FORECLOSURE", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Your company was unable to service its debt and payroll over the 7-day grace period. Final balance: -$%,.0f.".format(debtAmount),
                    color = Color(0xFFFCA5A5),
                    fontSize = 13.sp
                )

                Text(
                    text = "A private distress fund offers a Chapter 11 restructuring bailout: $30,000 fresh capital in exchange for 35% equity dilution.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onRestructureBailout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Accept Restructuring Bailout ($30k)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestartNewGame,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Liquidate & Start New Startup", fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {}
    )
}
