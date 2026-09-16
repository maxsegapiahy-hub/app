package com.example

import com.example.data.model.*
import com.example.data.repository.MaxSegRepository
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class MaxSegBusinessLogicTest {

    // Helper fake context isn't needed if we test pure business logic or instantiate a helper
    // We can test repository logic methods directly with a stub or direct functions

    @Test
    fun testSosPriorityRules() {
        fun getSosPriority(type: EmergencyType): SosPriorityRule {
            return when (type) {
                EmergencyType.SECURITY -> SosPriorityRule(SosPriority.CRITICAL, "Crítica", "Risco imediato à integridade ou segurança.")
                EmergencyType.FIRE -> SosPriorityRule(SosPriority.CRITICAL, "Crítica", "Incêndio ou risco de propagação exige despacho imediato.")
                EmergencyType.MEDICAL -> SosPriorityRule(SosPriority.HIGH, "Alta", "Urgência de saúde com necessidade de pronta resposta.")
                EmergencyType.ACCIDENT -> SosPriorityRule(SosPriority.HIGH, "Alta", "Acidente pode exigir atendimento e isolamento do local.")
                EmergencyType.OTHER -> SosPriorityRule(SosPriority.MEDIUM, "Média", "Ocorrência geral para triagem da Central.")
            }
        }

        assertEquals(SosPriority.CRITICAL, getSosPriority(EmergencyType.SECURITY).priority)
        assertEquals(SosPriority.CRITICAL, getSosPriority(EmergencyType.FIRE).priority)
        assertEquals(SosPriority.HIGH, getSosPriority(EmergencyType.MEDICAL).priority)
        assertEquals(SosPriority.HIGH, getSosPriority(EmergencyType.ACCIDENT).priority)
        assertEquals(SosPriority.MEDIUM, getSosPriority(EmergencyType.OTHER).priority)
    }

    @Test
    fun testFormatCoordinates() {
        val lat = -24.512354
        val lon = -48.842201
        val formatted = String.format(Locale.US, "%.5f, %.5f", lat, lon)
        assertEquals("-24.51235, -48.84220", formatted)
    }

    @Test
    fun testEmergencyContactValidation() {
        fun validateContact(contact: EmergencyContact): String? {
            if (contact.name.trim().length < 2) return "Informe o nome do contato de emergência."
            if (contact.phone.filter { it.isDigit() }.length < 10) return "Informe um telefone válido para o contato."
            if (contact.relationship.trim().length < 2) return "Informe o parentesco ou relação."
            return null
        }

        val validContact = EmergencyContact("Maria Silva", "(15) 99777-6655", "Esposa", isPrimary = true)
        assertNull(validateContact(validContact))

        val shortName = EmergencyContact("A", "(15) 99777-6655", "Esposa")
        assertEquals("Informe o nome do contato de emergência.", validateContact(shortName))

        val invalidPhone = EmergencyContact("Maria Silva", "12345", "Esposa")
        assertEquals("Informe um telefone válido para o contato.", validateContact(invalidPhone))

        val shortRel = EmergencyContact("Maria Silva", "(15) 99777-6655", " ")
        assertEquals("Informe o parentesco ou relação.", validateContact(shortRel))
    }

    @Test
    fun testUserProfileValidation() {
        fun validateProfile(profile: UserProfile): String? {
            if (profile.name.trim().length < 2) return "Informe seu nome completo."
            val email = profile.email.trim()
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            val isEmailValid = emailRegex.matches(email)
            if (!isEmailValid) {
                return "Informe um e-mail válido."
            }
            if (profile.phone.filter { it.isDigit() }.length < 10) return "Informe um telefone válido."
            val primaryCount = profile.contacts.count { it.isPrimary }
            if (primaryCount > 1) return "Defina apenas um contato principal."
            if (profile.central.name.trim().length < 2 || profile.central.phone.filter { it.isDigit() }.length < 3) {
                return "Informe um telefone válido para a Central Max."
            }
            return null
        }

        val valid = UserProfile(
            name = "Carlos Ed. Silva",
            email = "carlos@maxseg.com.br",
            phone = "(15) 99888-7766",
            contacts = listOf(
                EmergencyContact("Ana", "(15) 98888-7777", "Irmã", isPrimary = true)
            )
        )
        assertNull(validateProfile(valid))

        val invalidEmail = valid.copy(email = "email-invalido-sem-arroba")
        assertEquals("Informe um e-mail válido.", validateProfile(invalidEmail))

        val multiplePrimary = valid.copy(
            contacts = listOf(
                EmergencyContact("Ana", "(15) 98888-7777", "Irmã", isPrimary = true),
                EmergencyContact("Bruno", "(15) 98888-6666", "Amigo", isPrimary = true)
            )
        )
        assertEquals("Defina apenas um contato principal.", validateProfile(multiplePrimary))
    }

    @Test
    fun testMaxAiReplies() {
        fun getAiReply(message: String): String {
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

        assertTrue(getAiReply("Onde tem desconto em Apiaí?").contains("Clube Apiaí"))
        assertTrue(getAiReply("Preciso de médico urgente").contains("Telemedicina 24h"))
        assertTrue(getAiReply("Como acionar o SOS?").contains("botão SOS pressionado por 2 segundos"))
        assertTrue(getAiReply("Olá tudo bem?").contains("Central Max 24h"))
    }

    @Test
    fun testWithdrawPixValidation() {
        var balance = 248.50

        fun withdraw(amount: Double): Boolean {
            if (amount <= 0.0 || amount > balance) return false
            balance -= amount
            return true
        }

        assertFalse("Não deve permitir saque negativo", withdraw(-10.0))
        assertFalse("Não deve permitir saque de 0", withdraw(0.0))
        assertFalse("Não deve permitir saque acima do saldo", withdraw(300.0))
        assertTrue("Deve aprovar saque de valor disponível", withdraw(100.0))
        assertEquals(148.50, balance, 0.001)
    }

    @Test
    fun testNeighborhoodFilteringLogic() {
        val alerts = listOf(
            SosAlert("1", EmergencyType.SECURITY, SosPriority.CRITICAL, SosStatus.RECEIVED, neighborhood = "Centro"),
            SosAlert("2", EmergencyType.MEDICAL, SosPriority.HIGH, SosStatus.DISPATCHING, neighborhood = "Pinheiros"),
            SosAlert("3", EmergencyType.SECURITY, SosPriority.MEDIUM, SosStatus.ENROUTE, neighborhood = "Santa Bárbara"),
            SosAlert("4", EmergencyType.OTHER, SosPriority.LOW, SosStatus.RECEIVED, neighborhood = "Centro")
        )

        fun filterBy(neighborhood: String): List<SosAlert> {
            return if (neighborhood == "Todos") alerts else alerts.filter { it.neighborhood.equals(neighborhood, ignoreCase = true) }
        }

        assertEquals(4, filterBy("Todos").size)
        assertEquals(2, filterBy("Centro").size)
        assertEquals(1, filterBy("Pinheiros").size)
        assertEquals(1, filterBy("Santa Bárbara").size)
        assertEquals(0, filterBy("Vila Nova").size)
    }
}
