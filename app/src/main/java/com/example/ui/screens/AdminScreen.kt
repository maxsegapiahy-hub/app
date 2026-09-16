package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SosPriority
import com.example.data.model.SosStatus
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*

@Composable
fun AdminDialog(
    viewModel: MaxSegViewModel,
    onDismiss: () -> Unit
) {
    val allAlerts by viewModel.repository.allAlerts.collectAsState()
    val central by viewModel.repository.centralProfile.collectAsState()

    val name by viewModel.adminName.collectAsState()
    val city by viewModel.adminCity.collectAsState()
    val phone by viewModel.adminPhone.collectAsState()
    val service by viewModel.adminService.collectAsState()
    val availability by viewModel.adminAvailability.collectAsState()
    val target by viewModel.adminTarget.collectAsState()
    val status by viewModel.adminStatus.collectAsState()
    val channels by viewModel.adminChannels.collectAsState()

    var selectedNeighborhood by remember { mutableStateOf("Todos") }
    val neighborhoods = listOf("Todos", "Centro", "Pinheiros", "Santa Bárbara", "Vila Nova", "Jardim Paraíso")

    val filteredAlerts = remember(allAlerts, selectedNeighborhood) {
        if (selectedNeighborhood == "Todos") allAlerts
        else allAlerts.filter { it.neighborhood.equals(selectedNeighborhood, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .systemBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PanelDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                    .testTag("admin_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MaxIconBox(Icons.Filled.AdminPanelSettings, MaxGold, size = 36.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Central Max Apiahy", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextWhite)
                                Text("Painel Operacional & Despacho", fontSize = 11.sp, color = AccentSuccess)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Summary metrics
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminMetricCard(
                                    label = "Protocolos",
                                    value = allAlerts.size.toString(),
                                    color = MaxGold,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    label = "Críticos",
                                    value = allAlerts.count { it.priority == SosPriority.CRITICAL }.toString(),
                                    color = AccentRed,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminMetricCard(
                                    label = "Status Central",
                                    value = status.uppercase(),
                                    color = if (status == "online") AccentSuccess else AccentRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Alerts management section
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FILTRO POR BAIRRO EM APIAÍ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaxGold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${filteredAlerts.size} alerta(s)",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Horizontal neighborhood selector chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(neighborhoods) { bairro ->
                                        val count = if (bairro == "Todos") allAlerts.size else allAlerts.count { it.neighborhood.equals(bairro, ignoreCase = true) }
                                        val isSelected = selectedNeighborhood == bairro
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) MaxGold else PanelDark)
                                                .border(1.dp, if (isSelected) MaxGold else BorderDark, RoundedCornerShape(20.dp))
                                                .clickable { selectedNeighborhood = bairro }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (bairro != "Todos") {
                                                    Icon(
                                                        Icons.Filled.Place,
                                                        contentDescription = null,
                                                        tint = if (isSelected) BgDark else MaxGold,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = if (bairro == "Todos") "Todos ($count)" else "$bairro ($count)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) BgDark else TextWhite
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Sector patrol information card
                                val sectorVehicle = when (selectedNeighborhood) {
                                    "Centro" -> "Viatura Tática #01 (Pronta Resposta Centro · 2 a 3 min)"
                                    "Pinheiros" -> "Viatura Rápida #02 (Setor Norte · 3 a 4 min)"
                                    "Santa Bárbara" -> "Viatura Tática #03 (Setor Sul · 4 min)"
                                    "Vila Nova" -> "Viatura Apoio #04 (Setor Leste · 5 min)"
                                    "Jardim Paraíso" -> "Viatura Preventiva #02 (Em patrulha · 4 min)"
                                    else -> "4 Viaturas Táticas Ativas em Ronda Contínua"
                                }

                                MaxCard(
                                    backgroundColor = if (LocalMaxSegColors.current.isDark) Color(0xFF181510) else MaxGoldLight.copy(alpha = 0.2f),
                                    borderColor = MaxGold.copy(alpha = 0.35f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.DirectionsCar, null, tint = MaxGold, modifier = Modifier.size(15.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (selectedNeighborhood == "Todos") "COBERTURA TOTAL APIAÍ" else "SETOR $selectedNeighborhood".uppercase(),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = MaxGold,
                                                    letterSpacing = 0.5.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = sectorVehicle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextWhite
                                            )
                                        }
                                        if (selectedNeighborhood != "Todos") {
                                            Button(
                                                onClick = {
                                                    allAlerts.filter { it.neighborhood.equals(selectedNeighborhood, ignoreCase = true) && it.status == SosStatus.RECEIVED }
                                                        .forEach { viewModel.updateAlertStatus(it.alertId, SosStatus.DISPATCHING) }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("Despachar $selectedNeighborhood", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BgDark)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredAlerts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(CarbonDark)
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (selectedNeighborhood == "Todos") "Nenhum alerta ativo no momento."
                                        else "Nenhuma ocorrência registrada no bairro $selectedNeighborhood.",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        } else {
                            items(filteredAlerts) { alert ->
                                MaxCard(
                                    backgroundColor = PanelDark,
                                    borderColor = if (alert.priority == SosPriority.CRITICAL) AccentRed.copy(alpha = 0.5f) else BorderDark
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(alert.alertId, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaxGold)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                // Neighborhood badge
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaxGold.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Filled.Place, null, tint = MaxGold, modifier = Modifier.size(10.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(alert.neighborhood, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaxGold)
                                                }
                                            }
                                            Text("${alert.userName} · ${alert.emergencyType.label}", fontSize = 11.sp, color = TextWhite)
                                        }
                                        MaxPill(
                                            text = alert.status.name,
                                            color = when (alert.status) {
                                                SosStatus.RECEIVED -> AccentRed
                                                SosStatus.DISPATCHING -> MaxGold
                                                SosStatus.ENROUTE -> AccentBlue
                                                SosStatus.ARRIVED -> AccentSuccess
                                                SosStatus.CLOSED -> TextMuted
                                                SosStatus.CANCELED -> AccentRed
                                                else -> TextMuted
                                            }
                                        )
                                    }

                                    if (alert.latitude != null && alert.longitude != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Local: %.5f, %.5f (±%.0fm)".format(alert.latitude, alert.longitude, alert.accuracy ?: 0f),
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Status Transition Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (alert.status == SosStatus.RECEIVED) {
                                            SmallActionButton("Despachar", MaxGold) {
                                                viewModel.updateAlertStatus(alert.alertId, SosStatus.DISPATCHING)
                                            }
                                        }
                                        if (alert.status == SosStatus.DISPATCHING) {
                                            SmallActionButton("A caminho", AccentBlue) {
                                                viewModel.updateAlertStatus(alert.alertId, SosStatus.ENROUTE)
                                            }
                                        }
                                        if (alert.status == SosStatus.ENROUTE) {
                                            SmallActionButton("No local", AccentSuccess) {
                                                viewModel.updateAlertStatus(alert.alertId, SosStatus.ARRIVED)
                                            }
                                        }
                                        if (alert.status != SosStatus.CLOSED && alert.status != SosStatus.CANCELED) {
                                            SmallActionButton("Encerrar", Color(0xFF9E9E9E)) {
                                                viewModel.updateAlertStatus(alert.alertId, SosStatus.CLOSED)
                                            }
                                            SmallActionButton("Cancelar", AccentRed) {
                                                viewModel.updateAlertStatus(alert.alertId, SosStatus.CANCELED)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Central Profile Settings Form
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "CONFIGURAÇÕES DA CENTRAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaxGold,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { viewModel.adminName.value = it },
                                label = { Text("Nome da Central") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaxGold,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { viewModel.adminPhone.value = it },
                                label = { Text("Telefone / Ramal de Emergência") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaxGold,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = target,
                                onValueChange = { viewModel.adminTarget.value = it },
                                label = { Text("Meta de Resposta") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaxGold,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                )
                            )
                        }

                        item {
                            Text("Status Operacional:", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("online" to "Online", "degraded" to "Atenção", "offline" to "Offline").forEach { (st, lbl) ->
                                    val isSelected = status == st
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaxGold else PanelDark)
                                            .border(1.dp, if (isSelected) MaxGold else BorderDark, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.adminStatus.value = st }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(lbl, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) BgDark else TextWhite)
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { viewModel.saveAdminCentral() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text("Salvar Configurações", color = BgDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.SmallActionButton(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun AdminMetricCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = CarbonDark)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 8.sp, color = TextMuted)
        }
    }
}
