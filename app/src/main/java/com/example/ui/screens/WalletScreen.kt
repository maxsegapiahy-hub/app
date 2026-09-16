package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.components.MaxVipCard
import com.example.ui.theme.*

@Composable
fun WalletScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.repository.userProfile.collectAsState()

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
                    text = "CARTEIRA DE MEMBRO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaxGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Cartão VIP & Credencial Apiaí",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Text(
                    text = "Toque no cartão para girar e ver o verso com o QR Code.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 3D Flip Card
        item {
            MaxVipCard(
                memberName = userProfile.name,
                memberId = "ID: MAX-8842-AP"
            )
        }

        // Expand QR Code Action
        item {
            OutlinedButton(
                onClick = { viewModel.openQr() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaxGold),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MaxGold)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("enlarge_qr_button")
            ) {
                Icon(Icons.Filled.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Expandir QR Code para o Caixa", fontWeight = FontWeight.Bold)
            }
        }

        // Benefits Included in Plan
        item {
            Text(
                text = "BENEFÍCIOS ATIVOS NESTE PLANO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val benefits = listOf(
            Triple(Icons.Filled.Security, "Pronta Resposta 24h", "Viaturas dedicadas e despachos prioritários em Apiaí."),
            Triple(Icons.Filled.LocalHospital, "Telemedicina Familiar", "Consultas ilimitadas para você e dependentes sem coparticipação."),
            Triple(Icons.Filled.Percent, "Clube Apiahy de Descontos", "Até 70% em farmácias, postos e comércio credenciado."),
            Triple(Icons.Filled.Shield, "Ronda Preventiva Noturna", "Monitoramento e varredura periódica do seu endereço.")
        )

        items(benefits.size) { index ->
            val (icon, title, desc) = benefits[index]
            MaxCard(backgroundColor = PanelDark) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MaxIconBox(icon, MaxGold, size = 38.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Spacer(modifier = Modifier.width(6.dp))
                            MaxPill(text = "INCLUSO", color = AccentSuccess)
                        }
                        Text(desc, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}
