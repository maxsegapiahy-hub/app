package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*

@Composable
fun PinsScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    val badges = viewModel.repository.badges

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column {
                Text(
                    text = "INSÍGNIAS & GAMIFICAÇÃO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaxGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Conquistas de Membro VIP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
        }

        // XP Banner Card
        item {
            MaxCard(
                backgroundColor = if (LocalMaxSegColors.current.isDark) Color(0xFF161206) else MaxGoldLight.copy(alpha = 0.2f),
                borderColor = MaxGold.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PONTUAÇÃO TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                        Text("1.840 XP", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaxGold)
                        Text("Membro Engajado Apiahy", fontSize = 11.sp, color = AccentSuccess)
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaxGold.copy(alpha = 0.15f))
                            .border(1.dp, MaxGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MilitaryTech, null, tint = MaxGold, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }

        item {
            Text(
                text = "SUAS CONQUISTAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        items(badges) { badge ->
            MaxCard(
                backgroundColor = PanelDark,
                borderColor = if (badge.isUnlocked) Color(badge.colorHex).copy(alpha = 0.4f) else BorderDark
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        MaxIconBox(
                            icon = if (badge.isUnlocked) Icons.Filled.Stars else Icons.Filled.Lock,
                            color = if (badge.isUnlocked) Color(badge.colorHex) else TextMuted,
                            size = 40.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(badge.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(badge.body, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                        }
                    }

                    MaxPill(
                        text = if (badge.isUnlocked) "CONCLUÍDO" else "BLOQUEADO",
                        color = if (badge.isUnlocked) AccentSuccess else TextMuted
                    )
                }
            }
        }
    }
}
