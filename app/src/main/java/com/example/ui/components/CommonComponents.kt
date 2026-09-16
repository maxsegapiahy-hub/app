package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.theme.*

@Composable
fun MaxHeader(
    profileInitial: String,
    onProfileClick: () -> Unit,
    onAiClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BgDark,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaxBordo)
                        .border(1.dp, MaxGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "Max Seg Logo",
                        tint = MaxGold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MAX SEG",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = TextWhite,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "● APIAHY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaxGold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(AccentSuccess)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Proteção ativa 24h em Apiaí",
                            fontSize = 10.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable(onClick = onProfileClick)
                        .testTag("header_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Perfil",
                            tint = MaxGold,
                            modifier = Modifier.size(18.dp)
                        )
                        if (profileInitial.isNotEmpty()) {
                            Text(
                                text = profileInitial,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }

                // AI Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable(onClick = onAiClick)
                        .testTag("header_ai_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "Max IA",
                        tint = MaxGold,
                        modifier = Modifier.size(18.dp)
                    )
                    // Notification dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AccentSuccess)
                    )
                }

                // Admin Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PanelDark)
                        .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                        .clickable(onClick = onAdminClick)
                        .testTag("header_admin_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Painel Central",
                        tint = MaxGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tier Pill
                MaxPill(text = "PRATA", color = MaxGold)
            }
        }
    }
}

@Composable
fun MaxBottomNav(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(MainTab.HOME, "Início", Icons.Filled.Home),
        Triple(MainTab.WALLET, "Carteira", Icons.Filled.AccountBalanceWallet),
        Triple(MainTab.AFFILIATE, "Afiliado", Icons.Filled.Groups),
        Triple(MainTab.CLUB, "Clube", Icons.Filled.Storefront),
        Triple(MainTab.PINS, "Pins", Icons.Filled.MilitaryTech)
    )

    Surface(
        color = BgDark,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = BorderDark)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (tab, label, icon) ->
                val active = currentTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("nav_tab_${tab.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (active) MaxGold else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = if (active) MaxGold else TextMuted
                    )
                    if (active) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(MaxGold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaxCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = PanelDark,
    borderColor: Color = BorderDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun MaxPill(
    text: String,
    color: Color = MaxGold,
    filled: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(99.dp))
            .background(if (filled) color else color.copy(alpha = 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (filled) BgDark else color,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun MaxIconBox(
    icon: ImageVector,
    color: Color = MaxGold,
    size: Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun QrCodeView(
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    // 9x9 QR pattern identical to original
    val qrCells = List(81) { index ->
        val row = index / 9
        val col = index % 9
        (row * 3 + col * 5 + row * col) % 7 < 3 || ((row < 3 || row > 5) && (col < 3 || col > 5))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(size * 0.06f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (r in 0 until 9) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (c in 0 until 9) {
                        val filled = qrCells[r * 9 + c]
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (filled) BgDark else Color.White)
                        )
                    }
                }
            }
        }
    }
}
