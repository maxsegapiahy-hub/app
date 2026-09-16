package com.example.data.model

enum class EmergencyType(val id: String, val label: String) {
    SECURITY("security", "Segurança"),
    MEDICAL("medical", "Saúde"),
    FIRE("fire", "Incêndio"),
    ACCIDENT("accident", "Acidente"),
    OTHER("other", "Outra")
}

enum class SosPriority(val label: String) {
    CRITICAL("Crítica"),
    HIGH("Alta"),
    MEDIUM("Média"),
    LOW("Baixa")
}

data class SosPriorityRule(
    val priority: SosPriority,
    val label: String,
    val explanation: String
)

data class EmergencyContact(
    val name: String,
    val phone: String,
    val relationship: String,
    val isPrimary: Boolean = false
)

data class CentralContact(
    val name: String,
    val phone: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val contacts: List<EmergencyContact> = emptyList(),
    val central: CentralContact = CentralContact("Central Max Apiahy", "153")
)

data class CentralProfile(
    val id: String = "central-max-apiahy",
    val name: String = "Central Max Apiahy",
    val city: String = "Apiaí · SP",
    val phone: String = "153",
    val service: String = "Proteção, saúde e pronta resposta",
    val availability: String = "24 horas, todos os dias",
    val responseTarget: String = "Pronta resposta estimada em até 3 minutos",
    val channels: List<String> = listOf("SOS com localização GPS", "Telefone 153", "Acompanhamento no app"),
    val status: String = "online" // "online", "degraded", "offline"
)

enum class SosStatus(val title: String, val body: String) {
    IDLE("", ""),
    RECEIVED("Central Max recebeu o alerta", "Protocolo registrado e equipe sendo acionada."),
    DISPATCHING("Pronta resposta despachada", "A equipe mais próxima está se preparando para sair."),
    ENROUTE("Equipe a caminho", "A Central Max acompanha o deslocamento em tempo real."),
    ARRIVED("Equipe chegou ao local", "A pronta resposta confirmou atendimento no endereço."),
    CLOSED("Protocolo encerrado", "O atendimento foi concluído com sucesso."),
    CANCELED("Alerta cancelado", "A Central Max encerrou a pronta resposta com segurança.")
}

data class SosAlert(
    val alertId: String,
    val emergencyType: EmergencyType,
    val priority: SosPriority,
    val status: SosStatus,
    val latitude: Double?,
    val longitude: Double?,
    val accuracy: Float?,
    val createdAt: Long = System.currentTimeMillis(),
    val contactsNotified: Int = 0,
    val userName: String = "Carlos Ed. Silva",
    val userEmail: String = "carlos.silva@email.com"
)

data class Merchant(
    val id: String,
    val name: String,
    val category: String, // "farmacia", "posto", "mercado", "vestuario"
    val tag: String,
    val discount: String,
    val colorHex: Long
)

data class PatrolLog(
    val title: String,
    val time: String,
    val body: String,
    val colorHex: Long
)

data class BadgeItem(
    val id: String,
    val title: String,
    val body: String,
    val isUnlocked: Boolean,
    val colorHex: Long
)

data class AffiliateLevel(
    val id: String,
    val title: String,
    val people: String,
    val value: String,
    val colorHex: Long,
    val members: List<String>
)

data class ChatMessage(
    val id: String,
    val fromUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
