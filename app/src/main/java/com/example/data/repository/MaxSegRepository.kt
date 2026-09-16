package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class MaxSegRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("max_seg_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default)
    private var alertProgressionJob: Job? = null

    // User Profile
    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Central Profile
    private val _centralProfile = MutableStateFlow(loadInitialCentral())
    val centralProfile: StateFlow<CentralProfile> = _centralProfile.asStateFlow()

    // Theme Mode: Dark (recommended for night patrols/reading) or Light (crisp daylight reading)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode.value)
    }

    // Current active alert (null if idle)
    private val _activeAlert = MutableStateFlow<SosAlert?>(null)
    val activeAlert: StateFlow<SosAlert?> = _activeAlert.asStateFlow()

    // All alerts for Admin panel
    private val _allAlerts = MutableStateFlow<List<SosAlert>>(
        listOf(
            SosAlert(
                alertId = "SOS-8842-AP",
                emergencyType = EmergencyType.SECURITY,
                priority = SosPriority.CRITICAL,
                status = SosStatus.RECEIVED,
                latitude = -24.51235,
                longitude = -48.84220,
                accuracy = 8.5f,
                contactsNotified = 2,
                userName = "Carlos Ed. Silva",
                userEmail = "carlos.silva@email.com",
                neighborhood = "Centro"
            ),
            SosAlert(
                alertId = "SOS-7120-AP",
                emergencyType = EmergencyType.MEDICAL,
                priority = SosPriority.HIGH,
                status = SosStatus.DISPATCHING,
                latitude = -24.50890,
                longitude = -48.83540,
                accuracy = 12.0f,
                contactsNotified = 1,
                userName = "Marcos Vinicius",
                userEmail = "marcos.v@email.com",
                neighborhood = "Pinheiros"
            ),
            SosAlert(
                alertId = "SOS-5541-AP",
                emergencyType = EmergencyType.SECURITY,
                priority = SosPriority.MEDIUM,
                status = SosStatus.ENROUTE,
                latitude = -24.52110,
                longitude = -48.85100,
                accuracy = 9.0f,
                contactsNotified = 1,
                userName = "Drogaria Central (Comércio)",
                userEmail = "contato@drogaria.com",
                neighborhood = "Santa Bárbara"
            ),
            SosAlert(
                alertId = "SOS-3912-AP",
                emergencyType = EmergencyType.OTHER,
                priority = SosPriority.LOW,
                status = SosStatus.ARRIVED,
                latitude = -24.50520,
                longitude = -48.82900,
                accuracy = 15.0f,
                contactsNotified = 0,
                userName = "Juliana Alencar",
                userEmail = "juliana.a@email.com",
                neighborhood = "Vila Nova"
            )
        )
    )
    val allAlerts: StateFlow<List<SosAlert>> = _allAlerts.asStateFlow()

    // AI Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "1",
                fromUser = false,
                text = "Olá, Carlos. Sou o Max IA. Posso ajudar com proteção, saúde ou benefícios em Apiaí."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Commission Balance
    private val _commissionBalance = MutableStateFlow(1169.70)
    val commissionBalance: StateFlow<Double> = _commissionBalance.asStateFlow()

    // Merchants
    val merchants: List<Merchant> = listOf(
        Merchant(
            id = "1",
            name = "Drogaria Central Apiaí",
            category = "farmacia",
            tag = "Farmácias",
            discount = "10% a 70% de desconto",
            colorHex = 0xFF60A5FA
        ),
        Merchant(
            id = "2",
            name = "Posto de Serviços Apiaí",
            category = "posto",
            tag = "Combustível",
            discount = "R$ 0,15 de desconto por litro",
            colorHex = 0xFFD4AF37
        ),
        Merchant(
            id = "3",
            name = "Supermercado Regional",
            category = "mercado",
            tag = "Supermercados",
            discount = "Ofertas e encarte semanal",
            colorHex = 0xFF34D399
        ),
        Merchant(
            id = "4",
            name = "Loja de Modas Apiaí",
            category = "vestuario",
            tag = "Vestuário",
            discount = "10% a 20% à vista",
            colorHex = 0xFFC084FC
        )
    )

    // Patrols
    val patrols: List<PatrolLog> = listOf(
        PatrolLog(
            title = "Patrulha noturna preventiva",
            time = "03:42",
            body = "Viatura Max #02 realizou varredura no seu perímetro cadastrado.",
            colorHex = 0xFF34D399
        ),
        PatrolLog(
            title = "Check-in pronta resposta",
            time = "Ontem, 22:15",
            body = "Comércio afiliado verificado no Centro de Apiaí.",
            colorHex = 0xFF60A5FA
        )
    )

    // Badges / Insígnias
    val badges: List<BadgeItem> = listOf(
        BadgeItem("1", "Casa protegida", "Primeira ronda cadastrada", true, 0xFF34D399),
        BadgeItem("2", "Olho vivo", "7 rondas acompanhadas", true, 0xFF60A5FA),
        BadgeItem("3", "Cuidado Max", "Primeira consulta realizada", false, 0xFFA31236),
        BadgeItem("4", "Apiaí parceiro", "3 descontos utilizados", false, 0xFFD4AF37),
        BadgeItem("5", "Rede que cresce", "Indique um novo afiliado", false, 0xFFC084FC),
        BadgeItem("6", "Supervisor Ouro", "Meta em andamento", false, 0xFFD4AF37)
    )

    // Affiliate Levels
    val affiliateLevels: List<AffiliateLevel> = listOf(
        AffiliateLevel(
            id = "n1",
            title = "1º Nível · Vendas diretas (15%)",
            people = "15 clientes diretos ativos",
            value = "R$ 449,70/mês",
            colorHex = 0xFFA31236,
            members = listOf(
                "Drogaria Central Apiaí · +R$ 29,98",
                "Posto de Serviços Apiaí · +R$ 29,98",
                "Roberto M. Santos · +R$ 17,98"
            )
        ),
        AffiliateLevel(
            id = "n2",
            title = "2º Nível · Indicações (7%)",
            people = "9 clientes na rede",
            value = "R$ 219,90/mês",
            colorHex = 0xFF60A5FA,
            members = listOf(
                "Ana P. Rodrigues · +R$ 24,90",
                "Mercado Regional · +R$ 19,98"
            )
        ),
        AffiliateLevel(
            id = "n3",
            title = "3º Nível · Expansão (3%)",
            people = "4 clientes na rede",
            value = "R$ 99,90/mês",
            colorHex = 0xFFD4AF37,
            members = listOf(
                "Equipe Max Apiaí · +R$ 19,98"
            )
        )
    )

    private fun loadInitialProfile(): UserProfile {
        val name = prefs.getString("user_name", "Carlos Ed. Silva") ?: "Carlos Ed. Silva"
        val email = prefs.getString("user_email", "carlos.silva@email.com") ?: "carlos.silva@email.com"
        val phone = prefs.getString("user_phone", "(15) 99888-7766") ?: "(15) 99888-7766"
        val contact1Name = prefs.getString("c1_name", "Ana Silva") ?: "Ana Silva"
        val contact1Phone = prefs.getString("c1_phone", "(15) 98888-7777") ?: "(15) 98888-7777"
        val contact1Rel = prefs.getString("c1_rel", "Irmã") ?: "Irmã"

        return UserProfile(
            name = name,
            email = email,
            phone = phone,
            contacts = listOf(
                EmergencyContact(contact1Name, contact1Phone, contact1Rel, isPrimary = true)
            )
        )
    }

    private fun loadInitialCentral(): CentralProfile {
        return CentralProfile(
            name = prefs.getString("central_name", "Central Max Apiahy") ?: "Central Max Apiahy",
            city = prefs.getString("central_city", "Apiaí · SP") ?: "Apiaí · SP",
            phone = prefs.getString("central_phone", "153") ?: "153",
            service = prefs.getString("central_service", "Proteção, saúde e pronta resposta") ?: "Proteção, saúde e pronta resposta",
            availability = prefs.getString("central_availability", "24 horas, todos os dias") ?: "24 horas, todos os dias",
            responseTarget = prefs.getString("central_target", "Pronta resposta estimada em até 3 minutos") ?: "Pronta resposta estimada em até 3 minutos",
            status = prefs.getString("central_status", "online") ?: "online"
        )
    }

    fun saveProfile(profile: UserProfile): String? {
        val err = validateUserProfile(profile)
        if (err != null) return err

        prefs.edit()
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("user_phone", profile.phone)
            .apply()

        _userProfile.value = profile
        return null
    }

    fun updateCentralProfile(central: CentralProfile) {
        prefs.edit()
            .putString("central_name", central.name)
            .putString("central_city", central.city)
            .putString("central_phone", central.phone)
            .putString("central_service", central.service)
            .putString("central_availability", central.availability)
            .putString("central_target", central.responseTarget)
            .putString("central_status", central.status)
            .apply()

        _centralProfile.value = central
    }

    fun validateEmergencyContact(contact: EmergencyContact): String? {
        if (contact.name.trim().length < 2) return "Informe o nome do contato de emergência."
        if (contact.phone.filter { it.isDigit() }.length < 10) return "Informe um telefone válido para o contato."
        if (contact.relationship.trim().length < 2) return "Informe o parentesco ou relação."
        return null
    }

    fun validateUserProfile(profile: UserProfile): String? {
        if (profile.name.trim().length < 2) return "Informe seu nome completo."
        val email = profile.email.trim()
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val isEmailValid = try {
            android.util.Patterns.EMAIL_ADDRESS?.matcher(email)?.matches() ?: emailRegex.matches(email)
        } catch (_: Exception) {
            emailRegex.matches(email)
        }
        if (!isEmailValid) {
            return "Informe um e-mail válido."
        }
        if (profile.phone.filter { it.isDigit() }.length < 10) return "Informe um telefone válido."
        for (contact in profile.contacts) {
            val contactError = validateEmergencyContact(contact)
            if (contactError != null) return contactError
        }
        val primaryCount = profile.contacts.count { it.isPrimary }
        if (primaryCount > 1) return "Defina apenas um contato principal."
        if (profile.central.name.trim().length < 2 || profile.central.phone.filter { it.isDigit() }.length < 3) {
            return "Informe um telefone válido para a Central Max."
        }
        return null
    }

    fun getSosPriority(type: EmergencyType): SosPriorityRule {
        return when (type) {
            EmergencyType.SECURITY -> SosPriorityRule(SosPriority.CRITICAL, "Crítica", "Risco imediato à integridade ou segurança.")
            EmergencyType.FIRE -> SosPriorityRule(SosPriority.CRITICAL, "Crítica", "Incêndio ou risco de propagação exige despacho imediato.")
            EmergencyType.MEDICAL -> SosPriorityRule(SosPriority.HIGH, "Alta", "Urgência de saúde com necessidade de pronta resposta.")
            EmergencyType.ACCIDENT -> SosPriorityRule(SosPriority.HIGH, "Alta", "Acidente pode exigir atendimento e isolamento do local.")
            EmergencyType.OTHER -> SosPriorityRule(SosPriority.MEDIUM, "Média", "Ocorrência geral para triagem da Central.")
        }
    }

    fun formatSosCoordinates(latitude: Double, longitude: Double): String {
        return String.format(java.util.Locale.US, "%.5f, %.5f", latitude, longitude)
    }

    fun triggerSos(
        type: EmergencyType,
        latitude: Double? = -24.51235,
        longitude: Double? = -48.84220,
        accuracy: Float? = 8.5f,
        neighborhood: String = "Centro"
    ): SosAlert {
        alertProgressionJob?.cancel()
        val priorityRule = getSosPriority(type)
        val shortId = "SOS-${(1000..9999).random()}-AP"
        val alert = SosAlert(
            alertId = shortId,
            emergencyType = type,
            priority = priorityRule.priority,
            status = SosStatus.RECEIVED,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            contactsNotified = _userProfile.value.contacts.size,
            userName = _userProfile.value.name,
            userEmail = _userProfile.value.email,
            neighborhood = neighborhood
        )

        _activeAlert.value = alert
        _allAlerts.update { listOf(alert) + it }

        // Start automatic dispatch progression
        alertProgressionJob = scope.launch {
            delay(2500)
            if (_activeAlert.value?.alertId == alert.alertId && _activeAlert.value?.status == SosStatus.RECEIVED) {
                updateAlertStatus(alert.alertId, SosStatus.DISPATCHING)
            }
            delay(3500)
            if (_activeAlert.value?.alertId == alert.alertId && _activeAlert.value?.status == SosStatus.DISPATCHING) {
                updateAlertStatus(alert.alertId, SosStatus.ENROUTE)
            }
            delay(4000)
            if (_activeAlert.value?.alertId == alert.alertId && _activeAlert.value?.status == SosStatus.ENROUTE) {
                updateAlertStatus(alert.alertId, SosStatus.ARRIVED)
            }
        }

        return alert
    }

    fun cancelSos(alertId: String) {
        alertProgressionJob?.cancel()
        updateAlertStatus(alertId, SosStatus.CANCELED)
        if (_activeAlert.value?.alertId == alertId) {
            _activeAlert.value = null
        }
    }

    fun updateAlertStatus(alertId: String, newStatus: SosStatus) {
        _allAlerts.update { alerts ->
            alerts.map { if (it.alertId == alertId) it.copy(status = newStatus) else it }
        }
        if (_activeAlert.value?.alertId == alertId) {
            if (newStatus == SosStatus.CLOSED || newStatus == SosStatus.CANCELED) {
                _activeAlert.value = null
            } else {
                _activeAlert.value = _activeAlert.value?.copy(status = newStatus)
            }
        }
    }

    fun filterMerchants(category: String): List<Merchant> {
        return if (category == "todos") merchants else merchants.filter { it.category == category }
    }

    fun getMaxAiReply(message: String): String {
        val normalized = message.lowercase()
        return when {
            normalized.contains("desconto") || normalized.contains("clube") || normalized.contains("parceiro") -> {
                "Posso mostrar o Clube Apiaí com os parceiros e descontos ativos. Também é possível validar o benefício com o QR Code da sua carteira."
            }
            normalized.contains("médico") || normalized.contains("saúde") || normalized.contains("consulta") -> {
                "O benefício Telemedicina 24h está disponível. Posso encaminhar você para um médico Max agora, sem fila."
            }
            normalized.contains("sos") || normalized.contains("emergência") -> {
                "Em uma emergência, mantenha o botão SOS pressionado por 2 segundos. A Central Max recebe sua localização e aciona a pronta resposta."
            }
            else -> {
                "Entendi. Para uma orientação personalizada, posso encaminhar você para a Central Max 24h. Também posso mostrar seus benefícios ativos ou a rede de parceiros em Apiaí."
            }
        }
    }

    fun sendChatMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return
        val userMsg = ChatMessage(id = UUID.randomUUID().toString(), fromUser = true, text = trimmed)
        val aiReplyText = getMaxAiReply(trimmed)
        val aiMsg = ChatMessage(id = UUID.randomUUID().toString(), fromUser = false, text = aiReplyText)
        _chatMessages.update { it + userMsg + aiMsg }
    }

    fun withdrawPix(amount: Double): Boolean {
        if (amount <= 0 || amount > _commissionBalance.value) return false
        _commissionBalance.update { it - amount }
        return true
    }
}
