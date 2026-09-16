package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainTab
import com.example.ui.MaxSegViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val vm: MaxSegViewModel = viewModel()
            val isDarkMode by vm.isDarkMode.collectAsState()

            MaxSegTheme(darkTheme = isDarkMode) {
                val currentTab by vm.currentTab.collectAsState()
                val profile by vm.repository.userProfile.collectAsState()
                val toastMsg by vm.toastMessage.collectAsState()

                val isSosPickerOpen by vm.isSosPickerOpen.collectAsState()
                val isSosCancelOpen by vm.isSosCancelOpen.collectAsState()
                val isProfileOpen by vm.isProfileOpen.collectAsState()
                val isAiOpen by vm.isAiOpen.collectAsState()
                val isPixOpen by vm.isPixOpen.collectAsState()
                val isQrOpen by vm.isQrOpen.collectAsState()
                val selectedCoupon by vm.selectedCoupon.collectAsState()
                val isAdminOpen by vm.isAdminOpen.collectAsState()
                val selectedEmergencyType by vm.selectedEmergencyType.collectAsState()

                val profileInitial = remember(profile.name) {
                    profile.name.trim().take(1).uppercase()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                ) {
                    Scaffold(
                        containerColor = BgDark,
                        topBar = {
                            MaxHeader(
                                profileInitial = profileInitial,
                                onProfileClick = { vm.openProfile() },
                                onAiClick = { vm.openAi() },
                                onAdminClick = { vm.openAdmin() },
                                isDarkMode = isDarkMode,
                                onToggleTheme = { vm.toggleTheme() }
                            )
                        },
                        bottomBar = {
                            MaxBottomNav(
                                currentTab = currentTab,
                                onTabSelected = { vm.setTab(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                MainTab.HOME -> HomeScreen(viewModel = vm)
                                MainTab.WALLET -> WalletScreen(viewModel = vm)
                                MainTab.AFFILIATE -> AffiliateScreen(viewModel = vm)
                                MainTab.CLUB -> ClubScreen(viewModel = vm)
                                MainTab.PINS -> PinsScreen(viewModel = vm)
                                MainTab.TELEMEDICINE -> TelemedicineScreen(viewModel = vm)
                            }
                        }
                    }

                    // Toast message overlay
                    AnimatedVisibility(
                        visible = toastMsg != null,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 60.dp, start = 20.dp, end = 20.dp)
                    ) {
                        toastMsg?.let { msg ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PanelDark)
                                    .border(1.dp, MaxGold, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = msg,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Dialogs
                    if (isSosPickerOpen) {
                        SosPickerDialog(
                            selectedType = selectedEmergencyType,
                            onTypeSelected = { vm.setEmergencyType(it) },
                            onConfirm = { vm.confirmSos() },
                            onDismiss = { vm.closeSosPicker() },
                            viewModel = vm
                        )
                    }

                    if (isSosCancelOpen) {
                        SosCancelDialog(
                            onConfirm = { vm.confirmCancelSos() },
                            onDismiss = { vm.closeCancelSos() }
                        )
                    }

                    if (isProfileOpen) {
                        ProfileDialog(
                            viewModel = vm,
                            onDismiss = { vm.closeProfile() }
                        )
                    }

                    if (isAiOpen) {
                        MaxAiDialog(
                            viewModel = vm,
                            onDismiss = { vm.closeAi() }
                        )
                    }

                    if (isPixOpen) {
                        val balance by vm.repository.commissionBalance.collectAsState()
                        PixWithdrawalDialog(
                            balance = balance,
                            onWithdraw = { vm.withdrawPix(it) },
                            onDismiss = { vm.closePix() }
                        )
                    }

                    if (isQrOpen) {
                        QrEnlargeDialog(
                            onDismiss = { vm.closeQr() }
                        )
                    }

                    if (selectedCoupon != null) {
                        CouponDialog(
                            merchant = selectedCoupon!!,
                            onDismiss = { vm.closeCoupon() }
                        )
                    }

                    if (isAdminOpen) {
                        AdminDialog(
                            viewModel = vm,
                            onDismiss = { vm.closeAdmin() }
                        )
                    }
                }
            }
        }
    }
}
