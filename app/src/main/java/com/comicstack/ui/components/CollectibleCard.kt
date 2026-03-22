package com.comicstack.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comicstack.data.model.Rarity
import com.comicstack.ui.theme.*

@Composable
fun CollectibleCard(
    coverAsset: String,
    title: String,
    artist: String,
    rarity: Rarity,
    hp: Int, atk: Int, def: Int, spd: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(coverAsset) {
        try {
            val stream = context.assets.open(coverAsset)
            BitmapFactory.decodeStream(stream)?.asImageBitmap()
        } catch (_: Exception) { null }
    }
    val rarColor = Color(rarity.colorHex)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .border(2.dp, rarColor.copy(alpha = 0.6f), shape)
            .background(Surface)
    ) {
        // Cover image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.BottomStart
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🦇", fontSize = 48.sp)
                }
            }
            // Rarity badge overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(
                        rarity.label.uppercase(),
                        fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace, color = rarColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        artist,
                        fontSize = 8.sp, color = OnSurfaceDim,
                        maxLines = 1
                    )
                }
            }
        }

        // Stats bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatText("HP", hp, Color(0xFFE74C3C))
            StatText("ATK", atk, Color(0xFFE67E22))
            StatText("DEF", def, Color(0xFF3498DB))
            StatText("SPD", spd, Color(0xFF2ECC71))
        }
    }
}

@Composable
private fun StatText(label: String, value: Int, color: Color) {
    Text(
        "$label $value",
        fontSize = 9.sp, fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold, color = color
    )
}
