package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*

@Composable
fun AffiliateScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    val balance by viewModel.repository.commissionBalance.collectAsState()
    val levels = viewModel.repository.affiliateLevels
    var expandedLevel by remember { mutableStateOf<String?>("n1") }

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
                    text = "REDE DE AFILIADOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaxGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Comissões & Expansão Apiaí",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
        }

        // Commission Balance Card
        item {
            MaxCard(
                backgroundColor = if (LocalMaxSegColors.current.isDark) Color(0xFF16090D) else MaxBordoLight.copy(alpha = 0.08f),
                borderColor = MaxBordoLight.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SALDO DISPONÍVEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "R$ %.2f".format(balance),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = MaxGold
                        )
                        Text(
                            text = "+ R$ 380,00 de adesões este mês",
                            fontSize = 11.sp,
                            color = AccentSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { viewModel.openPix() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("withdraw_pix_button")
                    ) {
                        Icon(Icons.Filled.Pix, null, tint = BgDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sacar PIX", fontWeight = FontWeight.Bold, color = BgDark, fontSize = 12.sp)
                    }
                }
            }
        }

        // Leader Rank & Progress
        item {
            MaxCard(backgroundColor = PanelDark) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MaxIconBox(Icons.Filled.WorkspacePremium, MaxGold, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Líder Prata", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Próximo nível: Supervisor Ouro", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    MaxPill(text = "46% CONCLUÍDO", color = MaxGold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { 0.46f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaxGold,
                    trackColor = Color(0xFF242424)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Faltam 4 adesões diretas para atingir o bônus de Supervisor.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        // Unilevel Tree Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REDE UNILEVEL (3 NÍVEIS)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Total: 28 membros",
                    fontSize = 11.sp,
                    color = MaxGold
                )
            }
        }

        // Levels List
        items(levels.size) { index ->
            val level = levels[index]
            val isExpanded = expandedLevel == level.id

            MaxCard(
                backgroundColor = PanelDark,
                borderColor = if (isExpanded) Color(level.colorHex).copy(alpha = 0.5f) else BorderDark,
                modifier = Modifier.clickable {
                    expandedLevel = if (isExpanded) null else level.id
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(level.colorHex))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(level.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(level.people, fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(level.value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaxGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Divider(color = BorderDark, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        level.members.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(member.substringBefore(" · "), fontSize = 11.sp, color = TextWhite)
                                Text(member.substringAfter(" · "), fontSize = 11.sp, color = AccentSuccess, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Share invite link
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaxGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (LocalMaxSegColors.current.isDark) Color(0xFF131006) else MaxGoldLight.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        MaxIconBox(Icons.Filled.Share, MaxGold, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Seu link de indicação", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("maxseg.app/r/carlossilva", fontSize = 11.sp, color = MaxGold)
                        }
                    }

                    TextButton(onClick = { viewModel.showToast("Link copiado para compartilhar!") }) {
                        Text("Copiar", fontWeight = FontWeight.Bold, color = MaxGold)
                    }
                }
            }
        }
    }
}
