package com.arnyminerz.paraulogic.ui.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@ExperimentalMaterial3Api
fun CardWithIcon(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    contentDescription: String = title,
    iconSize: Dp = 52.dp,
    colors: CardColors = CardDefaults.cardColors(),
) {
    Card(
        modifier,
        colors = colors,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            val contentColor by colors.contentColor(true)

            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(iconSize)
                    .padding(end = 8.dp),
                tint = contentColor,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    title,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = 18.sp,
                )
                Text(
                    message,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                )
            }
        }
    }
}
