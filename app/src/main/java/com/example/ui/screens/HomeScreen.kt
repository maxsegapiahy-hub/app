package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SosStatus
import com.example.ui.MainTab
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    val activeAlert by viewModel.repository.activeAlert.collectAsState()
    val patrols = viewModel.repository.patrols
    val centralProfile by viewModel.repository.centralProfile.collectAsState()
    val view = LocalView.current

    // SOS Hold button state
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / 2000f).coerceIn(0f, 1f)
                if (holdProgress >= 1f) {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    viewModel.openSosPicker()
                    isHolding = false
                    holdProgress = 0f
                    break
                }
                delay(16)
            }
        } else {
            holdProgress = 0f
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Active SOS Protocol Card (if active)
        if (activeAlert != null) {
            item {
                ActiveSosBanner(
                    alert = activeAlert!!,
                    onCancelClick = { viewModel.openCancelSos() }
                )
            }
        }

        // Operational Status Card
        item {
            MaxCard(
                backgroundColor = Color(0xFF141414),
                borderColor = AccentSuccess.copy(alpha = 0.3f),
                modifier = Modifier.testTag("status_card")
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
                                .background(AccentSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SISTEMA 100% OPERACIONAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSuccess,
                            letterSpacing = 0.5.sp
                        )
                    }

                    MaxPill(text = centralProfile.status.uppercase(), color = AccentSuccess)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Equipe de Pronta Resposta ativa em Apiaí e perímetro.",
                    fontSize = 13.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${centralProfile.responseTarget} · Central 24h: ${centralProfile.phone}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 1-TAP / 2-SECOND SOS CARD
        item {
            MaxCard(
                backgroundColor = Color(0xFF16090D),
                borderColor = MaxBordoLight.copy(alpha = 0.6f),
                modifier = Modifier.testTag("sos_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BOTÃO SOS · PRONTA RESPOSTA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentRed,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Segure por 2s para acionar a viatura",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.openSosPicker() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaxGold),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(MaxGold, MaxGoldLight))),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("select_emergency_type_button")
                    ) {
                        Text("Classificar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big SOS Button Center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow / progress ring
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaxBordoLight.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = (4 * holdProgress + 2).dp,
                                color = if (holdProgress > 0f) MaxGold else MaxBordoLight,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner Button with Tap Detector
                        Box(
                            modifier = Modifier
                                .size(126.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaxBordoLight,
                                            MaxBordo,
                                            Color(0xFF4A0012)
                                        )
                                    )
                                )
                                .border(2.dp, if (holdProgress > 0f) MaxGold else MaxGold.copy(alpha = 0.4f), CircleShape)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isHolding = true
                                            tryAwaitRelease()
                                            isHolding = false
                                        }
                                    )
                                }
                                .testTag("sos_hold_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Emergency,
                                    contentDescription = "SOS",
                                    tint = TextWhite,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (holdProgress > 0f) "${(holdProgress * 100).toInt()}%" else "SOS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (holdProgress > 0f) MaxGold else TextWhite,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = if (holdProgress > 0f) "Mantenha..." else "2 SEGUNDOS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = MaxGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS: -24.51235, -48.84220 (Apiaí)",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = "Precisão ±8m",
                        fontSize = 10.sp,
                        color = AccentSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Quick Actions Grid
        item {
            Text(
                text = "SERVIÇOS RÁPIDOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Telemedicina
                QuickActionCard(
                    icon = Icons.Filled.MedicalServices,
                    iconColor = AccentRed,
                    title = "Telemedicina",
                    subtitle = "Médico 24h sem fila",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTab(MainTab.TELEMEDICINE) }
                )

                // Clube Apiaí
                QuickActionCard(
                    icon = Icons.Filled.LocalOffer,
                    iconColor = MaxGold,
                    title = "Clube Apiaí",
                    subtitle = "Até 70% desconto",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setTab(MainTab.CLUB) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Max IA
                QuickActionCard(
                    icon = Icons.Filled.SmartToy,
                    iconColor = AccentBlue,
                    title = "Max IA",
                    subtitle = "Assistente 24h",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openAi() }
                )

                // Meu QR Code
                QuickActionCard(
                    icon = Icons.Filled.QrCode2,
                    iconColor = AccentSuccess,
                    title = "Meu QR Code",
                    subtitle = "Carteira digital",
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openQr() }
                )
            }
        }

        // Recent Patrols Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTÓRICO DE RONDA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Apiaí Centro e Bairros",
                    fontSize = 10.sp,
                    color = MaxGold
                )
            }
        }

        items(patrols) { patrol ->
            MaxCard(backgroundColor = PanelDark) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        MaxIconBox(Icons.Filled.DirectionsCar, Color(patrol.colorHex), size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(patrol.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text(patrol.body, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp), lineHeight = 15.sp)
                        }
                    }
                    Text(patrol.time, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PanelDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            MaxIconBox(icon, iconColor, size = 36.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Text(subtitle, fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun ActiveSosBanner(
    alert: com.example.data.model.SosAlert,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, AccentRed, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D060A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AccentRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALERTA ATIVO: ${alert.alertId}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = AccentRed,
                        letterSpacing = 1.sp
                    )
                }

                MaxPill(text = alert.emergencyType.label.uppercase(), color = AccentRed)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.status.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Text(
                text = alert.status.body,
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step progression bar
            val steps = listOf(
                SosStatus.RECEIVED to "Recebido",
                SosStatus.DISPATCHING to "Despachado",
                SosStatus.ENROUTE to "A caminho",
                SosStatus.ARRIVED to "No local"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, (stepStatus, stepLabel) ->
                    val isPastOrCurrent = alert.status.ordinal >= stepStatus.ordinal
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isPastOrCurrent) MaxGold else BorderDark),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPastOrCurrent) {
                                Icon(Icons.Filled.Check, null, tint = BgDark, modifier = Modifier.size(10.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stepLabel,
                            fontSize = 8.sp,
                            fontWeight = if (isPastOrCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isPastOrCurrent) MaxGold else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onCancelClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(AccentRed, Color(0xFFB91C1C)))),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancelar alerta com segurança", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
