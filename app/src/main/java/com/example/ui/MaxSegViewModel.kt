package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MaxSegRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, WALLET, AFFILIATE, CLUB, PINS, TELEMEDICINE
}

class MaxSegViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MaxSegRepository(application.applicationContext)

    // Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Modals
    private val _isSosPickerOpen = MutableStateFlow(false)
    val isSosPickerOpen: StateFlow<Boolean> = _isSosPickerOpen.asStateFlow()

    private val _isSosCancelOpen = MutableStateFlow(false)
    val isSosCancelOpen: StateFlow<Boolean> = _isSosCancelOpen.asStateFlow()

    private val _isProfileOpen = MutableStateFlow(false)
    val isProfileOpen: StateFlow<Boolean> = _isProfileOpen.asStateFlow()

    private val _isAiOpen = MutableStateFlow(false)
    val isAiOpen: StateFlow<Boolean> = _isAiOpen.asStateFlow()

    private val _isPixOpen = MutableStateFlow(false)
    val isPixOpen: StateFlow<Boolean> = _isPixOpen.asStateFlow()

    private val _isQrOpen = MutableStateFlow(false)
    val isQrOpen: StateFlow<Boolean> = _isQrOpen.asStateFlow()

    private val _selectedCoupon = MutableStateFlow<Merchant?>(null)
    val selectedCoupon: StateFlow<Merchant?> = _selectedCoupon.asStateFlow()

    private val _isAdminOpen = MutableStateFlow(false)
    val isAdminOpen: StateFlow<Boolean> = _isAdminOpen.asStateFlow()

    // Selected SOS Emergency Type
    private val _selectedEmergencyType = MutableStateFlow(EmergencyType.SECURITY)
    val selectedEmergencyType: StateFlow<EmergencyType> = _selectedEmergencyType.asStateFlow()

    // Selected Merchant Category
    private val _selectedCategory = MutableStateFlow("todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Status Toast
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Theme Mode (Dark/Light)
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode

    fun toggleTheme() {
        repository.toggleDarkMode()
        val nextMsg = if (repository.isDarkMode.value) {
            "Modo Escuro ativado (conforto para leitura noturna)"
        } else {
            "Modo Claro ativado (ótimo para o dia em Apiaí)"
        }
        showToast(nextMsg)
    }

    fun setDarkMode(isDark: Boolean) {
        repository.setDarkMode(isDark)
        val msg = if (isDark) {
            "Modo Escuro ativado (conforto para leitura noturna)"
        } else {
            "Modo Claro ativado (ótimo para o dia em Apiaí)"
        }
        showToast(msg)
    }

    // Profile form state
    var editName = MutableStateFlow("")
    var editEmail = MutableStateFlow("")
    var editPhone = MutableStateFlow("")
    var editContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    var editCentralName = MutableStateFlow("")
    var editCentralPhone = MutableStateFlow("")
    var profileError = MutableStateFlow<String?>(null)
    var profileSuccess = MutableStateFlow(false)

    // Admin form state
    var adminName = MutableStateFlow("")
    var adminCity = MutableStateFlow("")
    var adminPhone = MutableStateFlow("")
    var adminService = MutableStateFlow("")
    var adminAvailability = MutableStateFlow("")
    var adminTarget = MutableStateFlow("")
    var adminStatus = MutableStateFlow("online")
    var adminChannels = MutableStateFlow("")

    init {
        val prof = repository.userProfile.value
        editName.value = prof.name
        editEmail.value = prof.email
        editPhone.value = prof.phone
        editContacts.value = prof.contacts
        editCentralName.value = prof.central.name
        editCentralPhone.value = prof.central.phone

        val cent = repository.centralProfile.value
        adminName.value = cent.name
        adminCity.value = cent.city
        adminPhone.value = cent.phone
        adminService.value = cent.service
        adminAvailability.value = cent.availability
        adminTarget.value = cent.responseTarget
        adminStatus.value = cent.status
        adminChannels.value = cent.channels.joinToString("\n")
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun openSosPicker() {
        _isSosPickerOpen.value = true
    }

    fun closeSosPicker() {
        _isSosPickerOpen.value = false
    }

    fun setEmergencyType(type: EmergencyType) {
        _selectedEmergencyType.value = type
    }

    fun confirmSos() {
        _isSosPickerOpen.value = false
        val alert = repository.triggerSos(_selectedEmergencyType.value)
        showToast("Alerta ${alert.alertId} enviado para a Central Max!")
    }

    fun openCancelSos() {
        _isSosCancelOpen.value = true
    }

    fun closeCancelSos() {
        _isSosCancelOpen.value = false
    }

    fun confirmCancelSos() {
        val alertId = repository.activeAlert.value?.alertId ?: return
        repository.cancelSos(alertId)
        _isSosCancelOpen.value = false
        showToast("Alerta $alertId cancelado com segurança.")
    }

    fun openProfile() {
        val prof = repository.userProfile.value
        editName.value = prof.name
        editEmail.value = prof.email
        editPhone.value = prof.phone
        editContacts.value = prof.contacts
        editCentralName.value = prof.central.name
        editCentralPhone.value = prof.central.phone
        profileError.value = null
        profileSuccess.value = false
        _isProfileOpen.value = true
    }

    fun closeProfile() {
        _isProfileOpen.value = false
    }

    fun saveProfile() {
        val nextProfile = UserProfile(
            name = editName.value.trim(),
            email = editEmail.value.trim(),
            phone = editPhone.value.trim(),
            contacts = editContacts.value,
            central = CentralContact(editCentralName.value.trim(), editCentralPhone.value.trim())
        )
        val err = repository.saveProfile(nextProfile)
        if (err != null) {
            profileError.value = err
            profileSuccess.value = false
        } else {
            profileError.value = null
            profileSuccess.value = true
            showToast("Perfil salvo e sincronizado com a Central!")
        }
    }

    fun addEmergencyContact() {
        if (editContacts.value.size < 3) {
            editContacts.update { it + EmergencyContact("", "", "") }
        }
    }

    fun updateEmergencyContact(index: Int, name: String, phone: String, rel: String, isPrimary: Boolean) {
        editContacts.update { list ->
            list.mapIndexed { i, c ->
                if (i == index) EmergencyContact(name, phone, rel, isPrimary)
                else if (isPrimary) c.copy(isPrimary = false)
                else c
            }
        }
    }

    fun removeEmergencyContact(index: Int) {
        editContacts.update { list ->
            list.filterIndexed { i, _ -> i != index }
        }
    }

    fun openAi() {
        _isAiOpen.value = true
    }

    fun closeAi() {
        _isAiOpen.value = false
    }

    fun sendAiMessage(text: String) {
        repository.sendChatMessage(text)
    }

    fun openPix() {
        _isPixOpen.value = true
    }

    fun closePix() {
        _isPixOpen.value = false
    }

    fun withdrawPix(amount: Double) {
        val success = repository.withdrawPix(amount)
        if (success) {
            _isPixOpen.value = false
            showToast("Saque de R$ %.2f via PIX solicitado!".format(amount))
        } else {
            showToast("Saldo insuficiente para este saque.")
        }
    }

    fun openQr() {
        _isQrOpen.value = true
    }

    fun closeQr() {
        _isQrOpen.value = false
    }

    fun openCoupon(merchant: Merchant) {
        _selectedCoupon.value = merchant
    }

    fun closeCoupon() {
        _selectedCoupon.value = null
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openAdmin() {
        val cent = repository.centralProfile.value
        adminName.value = cent.name
        adminCity.value = cent.city
        adminPhone.value = cent.phone
        adminService.value = cent.service
        adminAvailability.value = cent.availability
        adminTarget.value = cent.responseTarget
        adminStatus.value = cent.status
        adminChannels.value = cent.channels.joinToString("\n")
        _isAdminOpen.value = true
    }

    fun closeAdmin() {
        _isAdminOpen.value = false
    }

    fun saveAdminCentral() {
        val channels = adminChannels.value.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val updated = CentralProfile(
            name = adminName.value.trim(),
            city = adminCity.value.trim(),
            phone = adminPhone.value.trim(),
            service = adminService.value.trim(),
            availability = adminAvailability.value.trim(),
            responseTarget = adminTarget.value.trim(),
            channels = channels,
            status = adminStatus.value
        )
        repository.updateCentralProfile(updated)
        showToast("Configurações da Central Max atualizadas!")
    }

    fun updateAlertStatus(alertId: String, status: SosStatus) {
        repository.updateAlertStatus(alertId, status)
        showToast("Protocolo $alertId atualizado para ${status.title}")
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }
}
