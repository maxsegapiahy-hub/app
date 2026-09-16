package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.model.*
import com.example.ui.MaxSegViewModel
import com.example.ui.theme.*

@Composable
fun SosPickerDialog(
    selectedType: EmergencyType,
    onTypeSelected: (EmergencyType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    viewModel: MaxSegViewModel
) {
    val priorityRule = viewModel.repository.getSosPriority(selectedType)
    val priorityColor = when (priorityRule.priority) {
        SosPriority.CRITICAL -> AccentRed
        SosPriority.HIGH -> MaxGold
        SosPriority.MEDIUM -> AccentBlue
        SosPriority.LOW -> AccentSuccess
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("sos_picker_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaxBordo)
                        .border(1.dp, MaxGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaxGold,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Qual é o tipo de emergência?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Text(
                    text = "A classificação ajuda a Central Max a priorizar o despacho correto.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                    lineHeight = 16.sp
                )

                // Emergency Options Grid
                val options = listOf(
                    Triple(EmergencyType.SECURITY, "Segurança", Icons.Filled.Security),
                    Triple(EmergencyType.MEDICAL, "Saúde", Icons.Filled.MedicalServices),
                    Triple(EmergencyType.FIRE, "Incêndio", Icons.Filled.LocalFireDepartment),
                    Triple(EmergencyType.ACCIDENT, "Acidente", Icons.Filled.CarCrash),
                    Triple(EmergencyType.OTHER, "Outra", Icons.Filled.HelpOutline)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.take(3).forEach { (type, label, icon) ->
                        val active = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) MaxGold.copy(alpha = 0.15f) else Color(0xFF0F0F0F))
                                .border(1.dp, if (active) MaxGold else BorderDark, RoundedCornerShape(10.dp))
                                .clickable { onTypeSelected(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = if (active) MaxGold else TextMuted, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) MaxGold else TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.drop(3).forEach { (type, label, icon) ->
                        val active = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) MaxGold.copy(alpha = 0.15f) else Color(0xFF0F0F0F))
                                .border(1.dp, if (active) MaxGold else BorderDark, RoundedCornerShape(10.dp))
                                .clickable { onTypeSelected(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icon, null, tint = if (active) MaxGold else TextMuted, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(label, fontSize = 11.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) MaxGold else TextMuted)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0D0D0D))
                        .border(1.dp, priorityColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PriorityHigh, null, tint = priorityColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Prioridade ${priorityRule.label}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = priorityColor)
                            Text(priorityRule.explanation, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Send Button
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_sos_button")
                ) {
                    Icon(Icons.Filled.Send, null, tint = TextWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar alerta SOS agora", fontWeight = FontWeight.Bold, color = TextWhite)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SosCancelDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Cancel, null, tint = AccentRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancelar alerta SOS?", color = TextWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Text(
                "A Central Max será notificada e a equipe de pronta resposta encerrará o deslocamento com segurança.",
                color = TextMuted,
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sim, cancelar SOS", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Manter atendimento", color = TextMuted)
            }
        }
    )
}

@Composable
fun ProfileDialog(
    viewModel: MaxSegViewModel,
    onDismiss: () -> Unit
) {
    val name by viewModel.editName.collectAsState()
    val email by viewModel.editEmail.collectAsState()
    val phone by viewModel.editPhone.collectAsState()
    val contacts by viewModel.editContacts.collectAsState()
    val centralName by viewModel.editCentralName.collectAsState()
    val centralPhone by viewModel.editCentralPhone.collectAsState()
    val error by viewModel.profileError.collectAsState()
    val success by viewModel.profileSuccess.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("profile_dialog")
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
                    Column {
                        Text("Cadastro do Titular", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        Text("Seus dados e contatos de emergência", fontSize = 11.sp, color = TextMuted)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.editName.value = it },
                            label = { Text("Nome Completo") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaxGold,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = MaxGold,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.editEmail.value = it },
                            label = { Text("E-mail") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaxGold,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = MaxGold,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.editPhone.value = it },
                            label = { Text("Telefone / WhatsApp") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaxGold,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = MaxGold,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Contatos de Emergência (${contacts.size}/3)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaxGold)
                            if (contacts.size < 3) {
                                TextButton(onClick = { viewModel.addEmergencyContact() }) {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp), tint = MaxGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adicionar", color = MaxGold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    items(contacts.size) { index ->
                        val contact = contacts[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderDark, RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Contato #${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(onClick = {
                                            viewModel.updateEmergencyContact(index, contact.name, contact.phone, contact.relationship, !contact.isPrimary)
                                        }) {
                                            Text(if (contact.isPrimary) "★ Principal" else "Definir Principal", fontSize = 10.sp, color = if (contact.isPrimary) AccentSuccess else TextMuted)
                                        }
                                        IconButton(onClick = { viewModel.removeEmergencyContact(index) }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Filled.Delete, null, tint = AccentRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = contact.name,
                                    onValueChange = { viewModel.updateEmergencyContact(index, it, contact.phone, contact.relationship, contact.isPrimary) },
                                    label = { Text("Nome do contato") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaxGold,
                                        unfocusedBorderColor = BorderDark,
                                        focusedTextColor = TextWhite,
                                        unfocusedTextColor = TextWhite,
                                        focusedLabelColor = MaxGold,
                                        unfocusedLabelColor = TextMuted
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = contact.phone,
                                        onValueChange = { viewModel.updateEmergencyContact(index, contact.name, it, contact.relationship, contact.isPrimary) },
                                        label = { Text("Telefone") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaxGold,
                                            unfocusedBorderColor = BorderDark,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedLabelColor = MaxGold,
                                            unfocusedLabelColor = TextMuted
                                        )
                                    )
                                    OutlinedTextField(
                                        value = contact.relationship,
                                        onValueChange = { viewModel.updateEmergencyContact(index, contact.name, contact.phone, it, contact.isPrimary) },
                                        label = { Text("Parentesco") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaxGold,
                                            unfocusedBorderColor = BorderDark,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedLabelColor = MaxGold,
                                            unfocusedLabelColor = TextMuted
                                        )
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Central de Atendimento", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaxGold)
                        OutlinedTextField(
                            value = centralName,
                            onValueChange = { viewModel.editCentralName.value = it },
                            label = { Text("Nome da Central") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaxGold,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = MaxGold,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = centralPhone,
                            onValueChange = { viewModel.editCentralPhone.value = it },
                            label = { Text("Telefone da Central") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaxGold,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedLabelColor = MaxGold,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                    }
                }

                if (error != null) {
                    Text(
                        text = error ?: "",
                        color = AccentRed,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                if (success) {
                    Text(
                        text = "Perfil salvo com sucesso!",
                        color = AccentSuccess,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.saveProfile() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.Save, null, tint = BgDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Perfil", fontWeight = FontWeight.Bold, color = BgDark)
                }
            }
        }
    }
}

@Composable
fun MaxAiDialog(
    viewModel: MaxSegViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.repository.chatMessages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("max_ai_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaxBordo)
                                .border(1.dp, MaxGold, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.SmartToy, null, tint = MaxGold, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Max IA Apiahy", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Assistente 24h de segurança e saúde", fontSize = 10.sp, color = AccentSuccess)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Fechar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Messages list
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 14.dp,
                                            topEnd = 14.dp,
                                            bottomStart = if (msg.fromUser) 14.dp else 2.dp,
                                            bottomEnd = if (msg.fromUser) 2.dp else 14.dp
                                        )
                                    )
                                    .background(if (msg.fromUser) MaxGold else Color(0xFF222222))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = msg.text,
                                    fontSize = 12.sp,
                                    color = if (msg.fromUser) BgDark else TextWhite,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Descontos", "Telemedicina", "Como acionar SOS").forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.dp, BorderDark, RoundedCornerShape(99.dp))
                                .clickable {
                                    viewModel.sendAiMessage(prompt)
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(prompt, fontSize = 10.sp, color = MaxGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Digite sua dúvida...", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaxGold,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendAiMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaxGold)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = BgDark)
                    }
                }
            }
        }
    }
}

@Composable
fun PixWithdrawalDialog(
    balance: Double,
    onWithdraw: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var pixKey by remember { mutableStateOf("carlos.silva@email.com") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("pix_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaxGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = MaxGold, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Saque de Comissões PIX", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text("Saldo disponível: R$ %.2f".format(balance), fontSize = 13.sp, color = AccentSuccess, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pixKey,
                    onValueChange = { pixKey = it },
                    label = { Text("Chave PIX (CPF/E-mail/Telefone)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaxGold,
                        unfocusedBorderColor = BorderDark,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = MaxGold,
                        unfocusedLabelColor = TextMuted
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onWithdraw(balance) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Transferir R$ %.2f via PIX".format(balance), fontWeight = FontWeight.Bold, color = BgDark)
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun QrEnlargeDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("qr_enlarge_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QR Code Max Club Apiahy", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text("Mostre na tela do caixa para validar descontos", fontSize = 11.sp, color = TextMuted)

                Spacer(modifier = Modifier.height(20.dp))

                QrCodeView(size = 200.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("CARLOS ED. SILVA · ID: MAX-8842-AP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaxGold)
                Text("Plano Max Total · Válido em toda a rede de Apiaí", fontSize = 10.sp, color = TextMuted)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite)
                }
            }
        }
    }
}

@Composable
fun CouponDialog(
    merchant: Merchant,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PanelDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .testTag("coupon_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MaxIconBox(Icons.Filled.LocalOffer, Color(merchant.colorHex), size = 48.dp)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Cupom Exclusivo", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(merchant.name, fontSize = 13.sp, color = MaxGold, fontWeight = FontWeight.Medium)
                Text(merchant.discount, fontSize = 12.sp, color = AccentSuccess, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0D0D))
                        .border(1.dp, MaxGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CÓDIGO DE DESCONTO", fontSize = 9.sp, color = TextMuted, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("MAX-${merchant.category.uppercase()}-AP", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaxGold, letterSpacing = 2.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaxGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Utilizar no Caixa", color = BgDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
