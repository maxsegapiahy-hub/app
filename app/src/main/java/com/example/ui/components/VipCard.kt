package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MaxVipCard(
    memberName: String = "CARLOS ED. SILVA",
    memberId: String = "ID: MAX-8842-AP",
    modifier: Modifier = Modifier
) {
    var flipped by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable { flipped = !flipped }
            .testTag("vip_member_card")
    ) {
        if (rotation <= 90f) {
            // Front Face
            VipCardFront(
                memberName = memberName,
                memberId = memberId,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Back Face (flipped horizontally so it reads normally)
            VipCardBack(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            )
        }
    }
}

@Composable
private fun VipCardFront(
    memberName: String,
    memberId: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E070D),
                        Color(0xFF0F0B08),
                        Color(0xFF1A1408)
                    )
                )
            )
            .border(1.5.dp, MaxGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Top gold accent line
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, MaxGold, Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Brand & Hologram
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaxBordo)
                            .border(1.dp, MaxGold, RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaxGold,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MAX CLUB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaxGold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "APIAHY VIP MEMBER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Hologram Stamp
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF332600),
                                    Color(0xFF554400),
                                    Color(0xFF332600)
                                )
                            )
                        )
                        .border(1.dp, MaxGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MAX\nTOTAL",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = MaxGoldLight,
                        lineHeight = 10.sp
                    )
                }
            }

            // Middle Row: Chip & Contactless
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip
                Box(
                    modifier = Modifier
                        .width(38.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaxGold.copy(alpha = 0.85f))
                        .border(1.dp, MaxGoldLight, RoundedCornerShape(6.dp))
                        .padding(3.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF7A6000)))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF7A6000)))
                    }
                }

                Icon(
                    imageVector = Icons.Filled.Contactless,
                    contentDescription = "Contactless",
                    tint = MaxGold.copy(alpha = 0.7f),
                    modifier = Modifier.size(26.dp)
                )
            }

            // Bottom Row: Holder & VIP Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "TITULAR DO CARTÃO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = memberName.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = memberId,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaxGold
                    )
                }

                MaxPill(text = "● VIP ATIVO", color = AccentSuccess, filled = false)
            }
        }
    }
}

@Composable
private fun VipCardBack(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF101010))
            .border(1.5.dp, BorderDark, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(14.dp))
            // Magnetic Stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color.Black)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QrCodeView(size = 90.dp)

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Validação no comércio de Apiaí",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaxGold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Apresente este QR Code nas farmácias, postos e mercados parceiros.",
                        fontSize = 9.sp,
                        color = TextMuted,
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Central 24h: (15) 153",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSuccess
                    )
                }
            }
        }
    }
}
