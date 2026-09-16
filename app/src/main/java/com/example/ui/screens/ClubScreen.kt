package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.Merchant
import com.example.ui.MaxSegViewModel
import com.example.ui.components.MaxCard
import com.example.ui.components.MaxIconBox
import com.example.ui.components.MaxPill
import com.example.ui.theme.*

@Composable
fun ClubScreen(
    viewModel: MaxSegViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val merchants = viewModel.repository.filterMerchants(selectedCategory)

    val categories = listOf(
        "todos" to "Todos",
        "farmacia" to "Farmácias",
        "posto" to "Combustível",
        "mercado" to "Mercados",
        "vestuario" to "Vestuário"
    )

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
                    text = "CLUBE MAX APIAHY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaxGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Descontos & Parceiros Locais",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
            }
        }

        // Hero Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stat 1
                ClubStatCard(
                    number = "70%",
                    label = "Max Desconto",
                    color = MaxGold,
                    modifier = Modifier.weight(1f)
                )
                // Stat 2
                ClubStatCard(
                    number = "24h",
                    label = "Proteção Ativa",
                    color = AccentSuccess,
                    modifier = Modifier.weight(1f)
                )
                // Stat 3
                ClubStatCard(
                    number = "15+",
                    label = "Parceiros Apiaí",
                    color = AccentBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filter chips horizontal scroll
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (catId, catLabel) ->
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (isSelected) MaxGold else PanelDark)
                            .border(1.dp, if (isSelected) MaxGold else BorderDark, RoundedCornerShape(99.dp))
                            .clickable { viewModel.setCategory(catId) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = catLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) BgDark else TextWhite
                        )
                    }
                }
            }
        }

        // Merchant list
        items(merchants) { merchant ->
            MerchantCard(
                merchant = merchant,
                onVerCupom = { viewModel.openCoupon(merchant) }
            )
        }
    }
}

@Composable
fun ClubStatCard(
    number: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = PanelDark)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(number, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }
    }
}

@Composable
fun MerchantCard(
    merchant: Merchant,
    onVerCupom: () -> Unit
) {
    MaxCard(backgroundColor = PanelDark) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                val icon = when (merchant.category) {
                    "farmacia" -> Icons.Filled.LocalPharmacy
                    "posto" -> Icons.Filled.LocalGasStation
                    "mercado" -> Icons.Filled.ShoppingCart
                    else -> Icons.Filled.Checkroom
                }
                MaxIconBox(icon, Color(merchant.colorHex), size = 42.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    MaxPill(text = merchant.tag.uppercase(), color = Color(merchant.colorHex))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(merchant.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(merchant.discount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentSuccess, modifier = Modifier.padding(top = 2.dp))
                }
            }

            Button(
                onClick = onVerCupom,
                colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Cupom", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BgDark)
            }
        }
    }
}
