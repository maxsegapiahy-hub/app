package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainTab
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*

@Composable
fun TelemedicineScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    var isCallActive by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MAX SAÚDE 24H",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Telemedicina Familiar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextWhite
                    )
                }

                IconButton(onClick = { viewModel.setTab(MainTab.HOME) }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = TextMuted)
                }
            }
        }

        // Live Medical Status Card
        item {
            MaxCard(
                backgroundColor = Color(0xFF16090D),
                borderColor = MaxBordoLight.copy(alpha = 0.6f)
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
                        Text("PLANTÃO CLÍNICO ATIVO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentSuccess)
                    }

                    MaxPill(text = "SEM FILA", color = AccentSuccess)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Médicos disponíveis para atendimento imediato",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "Tempo estimado de espera: menos de 2 minutos. Receituário válido em qualquer farmácia de Apiaí.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isCallActive) {
                    Button(
                        onClick = {
                            isCallActive = true
                            viewModel.showToast("Conectando à sala médica segura Max...")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("start_telemedicine_button")
                    ) {
                        Icon(Icons.Filled.VideoCall, null, tint = TextWhite, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Consulta Médica Online", fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AccentSuccess, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1A12))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SALA DE TELECONSULTA CONECTADA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AccentSuccess)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dra. Mariana Costa · CRM/SP 192841", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Pronto Atendimento Clínico Geral", fontSize = 10.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { isCallActive = false },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(AccentRed)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Encerrar Atendimento", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 3-step guide
        item {
            Text(
                text = "COMO FUNCIONA O ATENDIMENTO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        val steps = listOf(
            Triple(Icons.Filled.TouchApp, "1. Toque e Conecte", "Clique no botão acima para abrir a sala virtual segura sem precisar agendar."),
            Triple(Icons.Filled.VideoCameraFront, "2. Vídeo Consulta HD", "Converse diretamente com o médico pelo celular ou tablet em áudio e vídeo."),
            Triple(Icons.Filled.ReceiptLong, "3. Receita e Atestado Digital", "Receba prescrições e atestados oficiais com QR Code ICP-Brasil diretamente por WhatsApp.")
        )

        items(steps.size) { index ->
            val (icon, title, desc) = steps[index]
            MaxCard(backgroundColor = PanelDark) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MaxIconBox(icon, AccentBlue, size = 38.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(desc, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp), lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}
