import { MaterialIcons } from "@expo/vector-icons";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useRouter } from "expo-router";
import * as Haptics from "expo-haptics";
import * as Location from "expo-location";
import Constants from "expo-constants";
import * as Notifications from "expo-notifications";
import { useEffect, useMemo, useRef, useState } from "react";
import {
  Animated,
  Alert,
  FlatList,
  Linking,
  Modal,
  Platform,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from "react-native";

import { ScreenContainer } from "@/components/screen-container";
import { CENTRAL_MAX_PROFILE, filterByCategory, formatSosCoordinates, getMaxAiReply, getSosPriority, getSosRetryDelayMs, SOS_MAX_ATTEMPTS, type EmergencyContact, type EmergencyType, type UserProfileInput, validateEmergencyContact, validateUserProfile } from "@/shared/max-seg";
import { trpc } from "@/lib/trpc";
import { useAuth } from "@/hooks/use-auth";

const C = {
  bg: "#080808",
  carbon: "#121212",
  panel: "#1E1E1E",
  panel2: "#171717",
  border: "#2A2A2A",
  bordo: "#800020",
  bordoLight: "#A31236",
  gold: "#D4AF37",
  goldLight: "#F3E5AB",
  white: "#F8FAFC",
  muted: "#A1A1AA",
  success: "#34D399",
  blue: "#60A5FA",
  red: "#F87171",
};

type Tab = "home" | "carteira" | "afiliado" | "clube" | "pins" | "telemedicina";
type SosLocation = { latitude: number; longitude: number; accuracy: number | null };
const CENTRAL_STATUS_COPY = {
  idle: { title: "", body: "", icon: "notifications-none" as const },
  received: { title: "Central Max recebeu o alerta", body: "Protocolo registrado e equipe sendo acionada.", icon: "cloud-done" as const },
  dispatching: { title: "Pronta resposta despachada", body: "A equipe mais próxima está se preparando para sair.", icon: "directions-car" as const },
  enroute: { title: "Equipe a caminho", body: "A Central Max acompanha o deslocamento em tempo real.", icon: "near-me" as const },
  arrived: { title: "Equipe chegou ao local", body: "A pronta resposta confirmou atendimento no endereço.", icon: "check-circle" as const },
  canceled: { title: "Alerta cancelado", body: "A Central Max encerrou a pronta resposta com segurança.", icon: "cancel" as const },
};
const PROFILE_STORAGE_KEY = "max-seg.user-profile.v1";

type Merchant = {
  id: string;
  name: string;
  category: string;
  tag: string;
  discount: string;
  icon: keyof typeof MaterialIcons.glyphMap;
  color: string;
};

const merchants: Merchant[] = [
  { id: "1", name: "Drogaria Central Apiaí", category: "farmacia", tag: "Farmácias", discount: "10% a 70% de desconto", icon: "local-pharmacy", color: C.blue },
  { id: "2", name: "Posto de Serviços Apiaí", category: "posto", tag: "Combustível", discount: "R$ 0,15 de desconto por litro", icon: "local-gas-station", color: C.gold },
  { id: "3", name: "Supermercado Regional", category: "mercado", tag: "Supermercados", discount: "Ofertas e encarte semanal", icon: "shopping-cart", color: C.success },
  { id: "4", name: "Loja de Modas Apiaí", category: "vestuario", tag: "Vestuário", discount: "10% a 20% à vista", icon: "checkroom", color: "#C084FC" },
];

const patrols = [
  { title: "Patrulha noturna preventiva", time: "03:42", body: "Viatura Max #02 realizou varredura no seu perímetro cadastrado.", color: C.success },
  { title: "Check-in pronta resposta", time: "Ontem, 22:15", body: "Comércio afiliado verificado no Centro de Apiaí.", color: C.blue },
];

const qrCells = Array.from({ length: 81 }, (_, index) => {
  const row = Math.floor(index / 9);
  const col = index % 9;
  return (row * 3 + col * 5 + row * col) % 7 < 3 || ((row < 3 || row > 5) && (col < 3 || col > 5));
});

function triggerHaptic(style: Haptics.ImpactFeedbackStyle = Haptics.ImpactFeedbackStyle.Light) {
  if (Platform.OS !== "web") void Haptics.impactAsync(style);
}

async function registerForPushNotificationsAsync(): Promise<string | null> {
  if (Platform.OS === "web") return null;
  if (Platform.OS === "android") {
    await Notifications.setNotificationChannelAsync("default", { name: "default", importance: Notifications.AndroidImportance.MAX, vibrationPattern: [0, 250, 250, 250], lightColor: C.bordo });
  }
  const current = await Notifications.getPermissionsAsync();
  const permission = current.status === "granted" ? current : await Notifications.requestPermissionsAsync();
  if (permission.status !== "granted") return null;
  const projectId = Constants.expoConfig?.extra?.eas?.projectId || Constants.easConfig?.projectId;
  if (!projectId) return null;
  return (await Notifications.getExpoPushTokenAsync({ projectId })).data;
}

function Card({ children, style }: { children: React.ReactNode; style?: object }) {
  return <View style={[styles.card, style]}>{children}</View>;
}

function IconBox({ icon, color = C.gold, size = 38 }: { icon: keyof typeof MaterialIcons.glyphMap; color?: string; size?: number }) {
  return (
    <View style={[styles.iconBox, { width: size, height: size, backgroundColor: `${color}22` }]}>
      <MaterialIcons name={icon} size={Math.round(size * 0.52)} color={color} />
    </View>
  );
}

function Pill({ children, color = C.gold, filled = false }: { children: React.ReactNode; color?: string; filled?: boolean }) {
  return <View style={[styles.pill, { borderColor: `${color}66`, backgroundColor: filled ? color : `${color}1C` }]}><Text style={[styles.pillText, { color: filled ? C.bg : color }]}>{children}</Text></View>;
}

function ModalShell({ visible, onClose, children, title, subtitle }: { visible: boolean; onClose: () => void; children: React.ReactNode; title: string; subtitle?: string }) {
  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <View style={styles.modalBackdrop}>
        <View style={styles.modalCard}>
          <View style={styles.modalHeader}>
            <View style={{ flex: 1 }}>
              <Text style={styles.modalTitle}>{title}</Text>
              {subtitle ? <Text style={styles.modalSubtitle}>{subtitle}</Text> : null}
            </View>
            <Pressable onPress={onClose} style={styles.closeButton} accessibilityLabel="Fechar">
              <MaterialIcons name="close" size={20} color={C.muted} />
            </Pressable>
          </View>
          {children}
        </View>
      </View>
    </Modal>
  );
}

function Header({ onAi, onProfile, onAdmin, profile, isAdmin }: { onAi: () => void; onProfile: () => void; onAdmin: () => void; profile: UserProfileInput | null; isAdmin: boolean }) {
  return (
    <View style={styles.header}>
      <View style={styles.brandRow}>
        <View style={styles.logoMark}><MaterialIcons name="security" size={22} color={C.gold} /></View>
        <View>
          <View style={styles.brandLine}><Text style={styles.brandName}>MAX SEG</Text><Text style={styles.brandCity}>● APIAHY</Text></View>
          <View style={styles.statusLine}><View style={styles.onlineDot} /><Text style={styles.statusText}>Proteção ativa 24h em Apiaí</Text></View>
        </View>
      </View>
      <View style={styles.headerRight}>
        <Pressable onPress={onProfile} style={({ pressed }) => [styles.profileButton, pressed && styles.pressed]} accessibilityLabel="Abrir cadastro do usuário">
          <MaterialIcons name="account-circle" size={20} color={C.gold} />
          {profile ? <Text style={styles.profileInitial}>{profile.name.trim().charAt(0).toUpperCase()}</Text> : null}
        </Pressable>
        <Pressable onPress={onAi} style={({ pressed }) => [styles.aiButton, pressed && styles.pressed]} accessibilityLabel="Abrir Max IA">
          <MaterialIcons name="smart-toy" size={18} color={C.gold} />
          <View style={styles.notificationDot} />
        </Pressable>
        {isAdmin ? <Pressable onPress={onAdmin} style={({ pressed }) => [styles.aiButton, pressed && styles.pressed]} accessibilityLabel="Abrir painel administrativo"><MaterialIcons name="admin-panel-settings" size={18} color={C.gold} /></Pressable> : null}
        <Pill color={C.gold}>PRATA</Pill>
      </View>
    </View>
  );
}

function SosTypePicker({ value, onChange, onConfirm }: { value: EmergencyType; onChange: (value: EmergencyType) => void; onConfirm: () => void }) {
  const options: { value: EmergencyType; label: string; icon: keyof typeof MaterialIcons.glyphMap }[] = [
    { value: "security", label: "Segurança", icon: "security" },
    { value: "medical", label: "Saúde", icon: "medical-services" },
    { value: "fire", label: "Incêndio", icon: "local-fire-department" },
    { value: "accident", label: "Acidente", icon: "car-crash" },
    { value: "other", label: "Outra", icon: "help-outline" },
  ];
  const priority = getSosPriority(value);
  const priorityColor = priority.priority === "critical" ? C.red : priority.priority === "high" ? C.gold : C.blue;
  return <View style={styles.sosPicker}><View style={styles.modalIconRed}><MaterialIcons name="warning-amber" size={28} color={C.gold} /></View><Text style={styles.modalHeadline}>Qual é o tipo de emergência?</Text><Text style={styles.modalBody}>A classificação ajuda a Central Max a priorizar o despacho correto.</Text><View style={styles.sosTypeGrid}>{options.map((option) => <Pressable key={option.value} onPress={() => { triggerHaptic(); onChange(option.value); }} style={[styles.sosTypeOption, value === option.value && styles.sosTypeOptionActive]}><MaterialIcons name={option.icon} size={20} color={value === option.value ? C.gold : C.muted} /><Text style={[styles.sosTypeText, value === option.value && styles.sosTypeTextActive]}>{option.label}</Text></Pressable>)}</View><View style={styles.priorityPreview}><MaterialIcons name="priority-high" size={18} color={priorityColor} /><View><Text style={styles.responseTitle}>Prioridade {priority.label}</Text><Text style={styles.responseBody}>{priority.explanation}</Text></View></View><Pressable onPress={onConfirm} style={({ pressed }) => [styles.primaryRedButton, pressed && styles.pressed]}><MaterialIcons name="send" size={18} color={C.white} /><Text style={styles.buttonText}>Enviar alerta SOS</Text></Pressable><Text style={styles.sosPickerHint}>O GPS será capturado após a confirmação.</Text></View>;
}

function BottomNav({ tab, onChange }: { tab: Tab; onChange: (tab: Tab) => void }) {
  const items: { id: Tab; label: string; icon: keyof typeof MaterialIcons.glyphMap }[] = [
    { id: "home", label: "Início", icon: "home" },
    { id: "carteira", label: "Carteira", icon: "account-balance-wallet" },
    { id: "afiliado", label: "Afiliado", icon: "groups" },
    { id: "clube", label: "Clube", icon: "storefront" },
    { id: "pins", label: "Pins", icon: "military-tech" },
  ];
  return (
    <View style={styles.bottomNav}>
      {items.map((item) => {
        const active = item.id === tab;
        return (
          <Pressable key={item.id} onPress={() => { triggerHaptic(); onChange(item.id); }} style={({ pressed }) => [styles.navItem, pressed && styles.pressed]}>
            <MaterialIcons name={item.icon} size={21} color={active ? C.gold : C.muted} />
            <Text style={[styles.navLabel, active && { color: C.gold }]}>{item.label}</Text>
            {active ? <View style={styles.navActiveLine} /> : null}
          </Pressable>
        );
      })}
    </View>
  );
}

function HomeView({ onNavigate, onSos, onAi, centralProfile }: { onNavigate: (tab: Tab) => void; onSos: () => void; onAi: () => void; centralProfile?: { name: string; city: string; responseTarget: string; status: "online" | "degraded" | "offline" } }) {
  const holdTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const [holding, setHolding] = useState(false);

  const startHold = () => {
    triggerHaptic(Haptics.ImpactFeedbackStyle.Medium);
    setHolding(true);
    holdTimer.current = setTimeout(() => {
      setHolding(false);
      onSos();
      if (holdTimer.current) clearTimeout(holdTimer.current);
    }, 2000);
  };

  const cancelHold = () => {
    if (holdTimer.current) clearTimeout(holdTimer.current);
    holdTimer.current = null;
    setHolding(false);
  };

  return (
    <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
      <Card style={styles.statusCard}>
        <View style={styles.statusCardGlow}><MaterialIcons name="shield" size={76} color={C.success} /></View>
        <View style={styles.rowBetween}>
          <Text style={[styles.eyebrowSuccess, centralProfile?.status === "degraded" && { color: C.gold }, centralProfile?.status === "offline" && { color: C.red }]}>● {centralProfile?.status === "online" ? "SISTEMA 100% OPERACIONAL" : centralProfile?.status === "degraded" ? "CENTRAL EM ATENÇÃO" : "CENTRAL OFFLINE"}</Text>
          <Text style={styles.tinyMuted}>{centralProfile?.city ?? "Apiaí · Centro"}</Text>
        </View>
        <Text style={styles.cardTitle}>Sua residência & família protegidas</Text>
        <Text style={styles.bodyText}>{centralProfile?.name ?? "Central Max Apiahy"} · Viatura Max Ronda em patrulha na sua região. {centralProfile?.responseTarget ?? "Pronta resposta a 3,2 min de você."}</Text>
      </Card>

      <Card style={styles.sosCard}>
        <Pill color={C.bordoLight}>⚠  EMERGÊNCIA 24H</Pill>
        <View style={styles.sosWrap}>
          <View style={[styles.sosRing, holding && styles.sosRingActive]}>
            <Pressable onPressIn={startHold} onPressOut={cancelHold} style={({ pressed }) => [styles.sosButton, (pressed || holding) && styles.sosPressed]} accessibilityLabel="Segure por dois segundos para acionar o SOS">
              <MaterialIcons name="notifications-active" size={31} color={C.gold} />
              <Text style={styles.sosLabel}>SOS 1-TAP</Text>
              <Text style={styles.sosHint}>{holding ? "Acionando..." : "Mantenha pressionado"}</Text>
            </Pressable>
          </View>
        </View>
        <Text style={styles.sosHelper}>{holding ? "Continue pressionando para chamar a central Max." : "Pressione por 2 segundos para acionar a central Max de Apiaí com seu GPS."}</Text>
      </Card>

      <View style={styles.quickGrid}>
        <Pressable onPress={() => { triggerHaptic(); onNavigate("telemedicina"); }} style={({ pressed }) => [styles.quickCard, { borderLeftColor: C.blue }, pressed && styles.pressed]}>
          <IconBox icon="medical-services" color={C.blue} size={34} />
          <Text style={styles.quickTitle}>Telemedicina 24h</Text>
          <Text style={styles.quickBody}>Consulta online sem fila</Text>
        </Pressable>
        <Pressable onPress={() => { triggerHaptic(); onNavigate("clube"); }} style={({ pressed }) => [styles.quickCard, { borderLeftColor: C.gold }, pressed && styles.pressed]}>
          <IconBox icon="storefront" color={C.gold} size={34} />
          <Text style={styles.quickTitle}>Clube Apiaí</Text>
          <Text style={styles.quickBody}>Até 70% de desconto</Text>
        </Pressable>
        <Pressable onPress={onAi} style={({ pressed }) => [styles.quickCard, { borderLeftColor: C.bordoLight }, pressed && styles.pressed]}>
          <IconBox icon="smart-toy" color={C.bordoLight} size={34} />
          <Text style={styles.quickTitle}>Max IA</Text>
          <Text style={styles.quickBody}>Tire dúvidas em segundos</Text>
        </Pressable>
        <Pressable onPress={() => onNavigate("carteira")} style={({ pressed }) => [styles.quickCard, { borderLeftColor: C.success }, pressed && styles.pressed]}>
          <IconBox icon="qr-code-2" color={C.success} size={34} />
          <Text style={styles.quickTitle}>Meu QR Code</Text>
          <Text style={styles.quickBody}>Valide seus benefícios</Text>
        </Pressable>
      </View>

      <Card>
        <View style={styles.rowBetween}><Text style={styles.sectionTitle}>HISTÓRICO DE RONDAS RECENTES</Text><Pressable onPress={() => Alert.alert("Histórico de segurança", "O histórico completo está salvo no log de segurança de Apiaí.")}><Text style={styles.goldLink}>Ver tudo</Text></Pressable></View>
        {patrols.map((patrol) => (
          <View key={patrol.title} style={styles.activityRow}>
            <View style={[styles.activityDot, { backgroundColor: patrol.color }]} />
            <View style={{ flex: 1 }}><View style={styles.rowBetween}><Text style={styles.activityTitle}>{patrol.title}</Text><Text style={styles.tinyMuted}>{patrol.time}</Text></View><Text style={styles.activityBody}>{patrol.body}</Text></View>
          </View>
        ))}
      </Card>
    </ScrollView>
  );
}

function WalletView({ onQr }: { onQr: () => void }) {
  const [flipped, setFlipped] = useState(false);
  const flipProgress = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.timing(flipProgress, {
      toValue: flipped ? 1 : 0,
      duration: 520,
      useNativeDriver: true,
    }).start();
  }, [flipProgress, flipped]);

  const frontRotate = flipProgress.interpolate({ inputRange: [0, 1], outputRange: ["0deg", "180deg"] });
  const backRotate = flipProgress.interpolate({ inputRange: [0, 1], outputRange: ["180deg", "360deg"] });

  return (
    <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
      <View style={styles.centerBlock}><Text style={styles.pageTitle}>Carteira Virtual Max Club</Text><Text style={styles.pageSubtitle}>Toque no cartão para girar e ver o verso com QR Code</Text></View>
      <Pressable onPress={() => { triggerHaptic(Haptics.ImpactFeedbackStyle.Medium); setFlipped((value) => !value); }} style={({ pressed }) => [styles.vipCard, pressed && styles.pressed]} accessibilityLabel="Girar cartão virtual">
        <Animated.View style={[styles.cardLayer, { transform: [{ rotateY: frontRotate }] }]}>
          <View style={styles.cardFace}>
            <View style={styles.goldLine} />
            <View style={styles.rowBetween}><View style={styles.brandRow}><View style={styles.miniLogo}><MaterialIcons name="shield" size={17} color={C.gold} /></View><View><Text style={styles.vipBrand}>MAX CLUB</Text><Text style={styles.vipCaption}>APIAHY VIP MEMBER</Text></View></View><View style={styles.hologram}><Text style={styles.hologramText}>MAX{`\n`}TOTAL</Text></View></View>
            <View style={styles.rowBetween}><View style={styles.chip}><View style={styles.chipLine} /><View style={styles.chipLine} /></View><MaterialIcons name="contactless" size={26} color={`${C.gold}BB`} /></View>
            <View style={styles.rowBetween}><View><Text style={styles.vipCaption}>TITULAR DO CARTÃO</Text><Text style={styles.memberName}>CARLOS ED. SILVA</Text><Text style={styles.memberId}>ID: MAX-8842-AP</Text></View><Pill color={C.success}>● VIP ATIVO</Pill></View>
            <View style={styles.goldLineBottom} />
          </View>
        </Animated.View>
        <Animated.View style={[styles.cardLayer, styles.vipCardBack, { transform: [{ rotateY: backRotate }] }]}>
          <View style={styles.backFace}>
            <View style={styles.magStripe} />
            <View style={styles.qrRow}><QrCode size={112} /><View style={{ flex: 1, marginLeft: 14 }}><Text style={styles.qrTitle}>Validação no comércio de Apiaí</Text><Text style={styles.backText}>Apresente este QR Code nas farmácias, postos e mercados parceiros para obter descontos.</Text><Text style={styles.backPhone}>Central 24h: (15) 99888-7766</Text></View></View>
            <View style={styles.backFooter}><Text style={styles.backFooterText}>Max Seg & Max Saúde · Apiahy</Text><Text style={styles.backFooterText}>Uso pessoal</Text></View>
          </View>
        </Animated.View>
      </Pressable>
      <Card style={styles.validatorRow}><View style={{ flex: 1 }}><Text style={styles.quickTitle}>Validador de desconto em Apiaí</Text><Text style={styles.quickBody}>Mostre o QR Code no caixa do parceiro</Text></View><Pressable onPress={onQr} style={({ pressed }) => [styles.bordoButton, pressed && styles.pressed]}><MaterialIcons name="qr-code-2" size={16} color={C.white} /><Text style={styles.buttonText}>Ampliar QR</Text></Pressable></Card>
      <Text style={styles.sectionTitle}>BENEFÍCIOS DO SEU PLANO MAX TOTAL</Text>
      <View style={styles.benefitGrid}>{[
        ["shield", "Ronda noturna 24h", C.bordoLight], ["medical-services", "Telemedicina familiar", C.blue], ["local-offer", "Clube de descontos", C.gold], ["emergency", "Pronta resposta SOS", C.red],
      ].map(([icon, label, color]) => <View key={label} style={styles.benefitItem}><MaterialIcons name={icon as keyof typeof MaterialIcons.glyphMap} size={17} color={color as string} /><Text style={styles.benefitText}>{label}</Text></View>)}</View>
    </ScrollView>
  );
}

function QrCode({ size = 130 }: { size?: number }) {
  return <View style={[styles.qrCode, { width: size, height: size, padding: size * 0.06 }]}>{qrCells.map((filled, index) => <View key={index} style={{ width: `${100 / 9}%`, height: `${100 / 9}%`, backgroundColor: filled ? C.bg : "#FFF" }} />)}</View>;
}

function AffiliateView({ onPix }: { onPix: () => void }) {
  const [expanded, setExpanded] = useState("n1");
  const levels = [
    { id: "n1", title: "1º Nível · Vendas diretas (15%)", people: "15 clientes diretos ativos", value: "R$ 449,70/mês", color: C.bordoLight, members: ["Drogaria Central Apiaí · +R$ 29,98", "Posto de Serviços Apiaí · +R$ 29,98", "Roberto M. Santos · +R$ 17,98"] },
    { id: "n2", title: "2º Nível · Indicações (7%)", people: "9 clientes na rede", value: "R$ 219,90/mês", color: C.blue, members: ["Ana P. Rodrigues · +R$ 24,90", "Mercado Regional · +R$ 19,98"] },
    { id: "n3", title: "3º Nível · Expansão (3%)", people: "4 clientes na rede", value: "R$ 99,90/mês", color: C.gold, members: ["Equipe Max Apiaí · +R$ 19,98"] },
  ];
  return (
    <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}>
      <Card style={styles.balanceCard}><View style={styles.rowBetween}><View><Text style={styles.eyebrow}>SALDO DE COMISSÕES RECORRENTES</Text><Text style={styles.balance}>R$ 1.169,70</Text></View><Pressable onPress={onPix} style={({ pressed }) => [styles.goldButton, pressed && styles.pressed]}><MaterialIcons name="account-balance-wallet" size={15} color={C.bg} /><Text style={styles.darkButtonText}>Saque PIX</Text></Pressable></View><View style={styles.statsRow}><View><Text style={styles.statLabel}>Ganhos de adesão · mês</Text><Text style={styles.statValueGreen}>R$ 700,00</Text></View><View><Text style={styles.statLabel}>Contratos ativos na rede</Text><Text style={styles.statValue}>28 pessoas</Text></View></View></Card>
      <Card><View style={styles.rowBetween}><View style={styles.brandRow}><View style={styles.rankIcon}><MaterialIcons name="military-tech" size={17} color={C.white} /></View><View><Text style={styles.quickTitle}>Graduação atual: <Text style={{ color: C.gold }}>LÍDER PRATA</Text></Text><Text style={styles.quickBody}>Próxima meta: Supervisor Ouro</Text></View></View><Text style={styles.percent}>46%</Text></View><View style={styles.progressTrack}><View style={[styles.progressBar, { width: "46%" }]} /></View><Text style={styles.goalText}>ⓘ Faltam <Text style={styles.goalStrong}>32 contratos na rede</Text> + <Text style={styles.goalStrong}>1 Líder Prata</Text> para qualificar a <Text style={{ color: C.gold }}>Supervisor Ouro</Text>.</Text></Card>
      <Card><View style={styles.rowBetween}><Text style={styles.sectionTitle}>REDE UNILEVEL · 3 NÍVEIS</Text><Text style={styles.tinyMuted}>Teto Max: 25%</Text></View>{levels.map((level) => { const open = expanded === level.id; return <View key={level.id} style={styles.levelBox}><Pressable onPress={() => setExpanded(open ? "" : level.id)} style={({ pressed }) => [styles.rowBetween, pressed && styles.pressed]}><View style={styles.brandRow}><View style={[styles.levelBadge, { backgroundColor: level.color }]}><Text style={styles.levelBadgeText}>{level.id.toUpperCase()}</Text></View><View><Text style={styles.levelTitle}>{level.title}</Text><Text style={styles.quickBody}>{level.people}</Text></View></View><View style={{ alignItems: "flex-end" }}><Text style={styles.levelValue}>{level.value}</Text><MaterialIcons name={open ? "expand-less" : "expand-more"} size={18} color={C.muted} /></View></Pressable>{open ? <View style={styles.memberList}>{level.members.map((member) => <Text key={member} style={styles.memberRow}>• {member}</Text>)}</View> : null}</View>; })}</Card>
      <View style={styles.infoStrip}><MaterialIcons name="info-outline" size={17} color={C.gold} /><Text style={styles.infoText}>Seu painel de afiliado mostra comissões recorrentes de forma transparente. Saques ficam disponíveis após validação cadastral.</Text></View>
    </ScrollView>
  );
}

function ClubView({ onCoupon }: { onCoupon: (merchant: Merchant) => void }) {
  const [category, setCategory] = useState("todos");
  const filtered = useMemo(() => filterByCategory(merchants, category), [category]);
  const filters = [["todos", "Todos"], ["farmacia", "Farmácias"], ["posto", "Combustível"], ["mercado", "Mercados"], ["vestuario", "Vestuário"]];
  return (
    <FlatList data={filtered} keyExtractor={(item) => item.id} contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false} ListHeaderComponent={<><View style={styles.clubHero}><View style={styles.clubGlow} /><Text style={styles.clubKicker}>MAX CLUB · APIAHY</Text><Text style={styles.clubTitle}>Vantagens que cuidam de você.</Text><Text style={styles.clubBody}>Descontos exclusivos em parceiros locais para quem tem proteção Max.</Text><View style={styles.clubStats}><View><Text style={styles.clubStatValue}>70%</Text><Text style={styles.clubStatLabel}>desconto máx.</Text></View><View><Text style={styles.clubStatValue}>24h</Text><Text style={styles.clubStatLabel}>benefícios ativos</Text></View><View><Text style={styles.clubStatValue}>15+</Text><Text style={styles.clubStatLabel}>parceiros</Text></View></View></View><Text style={styles.sectionTitle}>PARCEIROS EM DESTAQUE</Text><ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterRow}>{filters.map(([id, label]) => <Pressable key={id} onPress={() => setCategory(id)} style={({ pressed }) => [styles.filterChip, category === id && styles.filterChipActive, pressed && styles.pressed]}><Text style={[styles.filterText, category === id && styles.filterTextActive]}>{label}</Text></Pressable>)}</ScrollView></>} renderItem={({ item }) => <Card style={styles.merchantCard}><View style={[styles.merchantIcon, { backgroundColor: `${item.color}22` }]}><MaterialIcons name={item.icon} size={22} color={item.color} /></View><View style={{ flex: 1 }}><Text style={styles.merchantName}>{item.name}</Text><Text style={styles.merchantTag}>{item.tag}</Text><Text style={styles.merchantDiscount}>{item.discount}</Text></View><Pressable onPress={() => onCoupon(item)} style={({ pressed }) => [styles.couponButton, pressed && styles.pressed]}><Text style={styles.darkButtonText}>Cupom</Text></Pressable></Card>} ListFooterComponent={<View style={styles.partnerFooter}><MaterialIcons name="location-on" size={16} color={C.gold} /><Text style={styles.partnerFooterText}>Novos parceiros são verificados diariamente no Centro de Apiaí.</Text></View>} />
  );
}

function TelemedicineView({ onBack }: { onBack: () => void }) {
  const [calling, setCalling] = useState(false);
  return <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}><Pressable onPress={onBack} style={styles.backLink}><MaterialIcons name="arrow-back" size={18} color={C.gold} /><Text style={styles.goldLink}>Voltar ao início</Text></Pressable><View style={styles.teleHero}><View style={styles.doctorAvatar}><MaterialIcons name="medical-services" size={42} color={C.blue} /></View><Pill color={C.success}>● 3 médicos online</Pill><Text style={styles.pageTitle}>Telemedicina 24h</Text><Text style={styles.pageSubtitle}>Atendimento médico familiar, sem fila e de onde você estiver.</Text></View><Card style={styles.callCard}><View style={styles.rowBetween}><View><Text style={styles.eyebrowBlue}>PRONTA RESPOSTA SAÚDE</Text><Text style={styles.cardTitle}>Fale com um médico agora</Text><Text style={styles.bodyText}>Tempo médio de espera: menos de 3 minutos.</Text></View><IconBox icon="videocam" color={C.blue} size={48} /></View><Pressable onPress={() => { triggerHaptic(Haptics.ImpactFeedbackStyle.Medium); setCalling(true); }} style={({ pressed }) => [styles.primaryBlueButton, pressed && styles.pressed]}><MaterialIcons name={calling ? "hourglass-top" : "videocam"} size={18} color={C.white} /><Text style={styles.buttonText}>{calling ? "Conectando com a central..." : "Iniciar consulta online"}</Text></Pressable></Card><Text style={styles.sectionTitle}>COMO FUNCIONA</Text>{[["1", "Solicite sua consulta", "Toque no botão e confirme seus dados."], ["2", "Aguarde a conexão", "Um médico Max estará com você em instantes."], ["3", "Receba orientação", "Sua família fica cuidada em qualquer horário."]].map(([number, title, body]) => <View key={number} style={styles.stepRow}><View style={styles.stepNumber}><Text style={styles.stepNumberText}>{number}</Text></View><View><Text style={styles.quickTitle}>{title}</Text><Text style={styles.quickBody}>{body}</Text></View></View>)}<View style={styles.infoStrip}><MaterialIcons name="lock" size={16} color={C.blue} /><Text style={styles.infoText}>Atendimento protegido e sigiloso pelo benefício Max Saúde.</Text></View></ScrollView>;
}

function PinsView() {
  const pins = [{ icon: "shield", title: "Casa protegida", body: "Primeira ronda cadastrada", unlocked: true, color: C.success }, { icon: "local-police", title: "Olho vivo", body: "7 rondas acompanhadas", unlocked: true, color: C.blue }, { icon: "medical-services", title: "Cuidado Max", body: "Primeira consulta realizada", unlocked: false, color: C.bordoLight }, { icon: "storefront", title: "Apiaí parceiro", body: "3 descontos utilizados", unlocked: false, color: C.gold }, { icon: "groups", title: "Rede que cresce", body: "Indique um novo afiliado", unlocked: false, color: "#C084FC" }, { icon: "military-tech", title: "Supervisor Ouro", body: "Meta em andamento", unlocked: false, color: C.gold }];
  return <ScrollView contentContainerStyle={styles.scrollContent} showsVerticalScrollIndicator={false}><View style={styles.centerBlock}><Text style={styles.pageTitle}>Insígnias Max</Text><Text style={styles.pageSubtitle}>Sua jornada de proteção, cuidado e comunidade em Apiaí.</Text></View><Card style={styles.pointsCard}><View><Text style={styles.eyebrow}>PONTUAÇÃO DE SEGURANÇA</Text><Text style={styles.points}>1.840 <Text style={styles.pointsUnit}>XP</Text></Text></View><View style={styles.pointsBadge}><MaterialIcons name="military-tech" size={25} color={C.gold} /><Text style={styles.pointsBadgeText}>LÍDER PRATA</Text></View></Card><Text style={styles.sectionTitle}>COLEÇÃO · 2 DE 6 DESBLOQUEADAS</Text><View style={styles.pinGrid}>{pins.map((pin) => <View key={pin.title} style={[styles.pinCard, !pin.unlocked && styles.pinLocked]}><View style={[styles.pinIcon, { backgroundColor: `${pin.color}22` }]}><MaterialIcons name={pin.icon as keyof typeof MaterialIcons.glyphMap} size={28} color={pin.unlocked ? pin.color : C.muted} /></View><Text style={[styles.pinTitle, !pin.unlocked && { color: C.muted }]}>{pin.title}</Text><Text style={styles.pinBody}>{pin.unlocked ? pin.body : "Bloqueada · continue sua jornada"}</Text>{pin.unlocked ? <Pill color={pin.color}>DESBLOQUEADA</Pill> : <MaterialIcons name="lock" size={14} color={C.muted} style={styles.pinLock} />}</View>)}</View><View style={styles.infoStrip}><MaterialIcons name="tips-and-updates" size={16} color={C.gold} /><Text style={styles.infoText}>Acompanhe as rondas, use seus benefícios e convide a comunidade para desbloquear novas insígnias.</Text></View></ScrollView>;
}

export default function HomeScreen() {
  const router = useRouter();
  const { user, isAuthenticated } = useAuth();
  const centralProfileQuery = trpc.central.profile.useQuery(undefined, { refetchInterval: 5000 });
  const [tab, setTab] = useState<Tab>("home");
  const [sosVisible, setSosVisible] = useState(false);
  const [sosSending, setSosSending] = useState(false);
  const [sosLocation, setSosLocation] = useState<SosLocation | null>(null);
  const [sosLocationError, setSosLocationError] = useState(false);
  const [sosNoticeVisible, setSosNoticeVisible] = useState(false);
  const [sosApiStatus, setSosApiStatus] = useState<"idle" | "sending" | "sent" | "error">("idle");
  const [sosApiId, setSosApiId] = useState<string | null>(null);
  const [sosContactsNotified, setSosContactsNotified] = useState(0);
  const [sosAttempt, setSosAttempt] = useState(0);
  const [emergencyType, setEmergencyType] = useState<EmergencyType>("security");
  const [centralStatus, setCentralStatus] = useState<"idle" | "received" | "dispatching" | "enroute" | "arrived" | "canceled">("idle");
  const [cancelVisible, setCancelVisible] = useState(false);
  const [cancelSending, setCancelSending] = useState(false);
  const [cancelStatus, setCancelStatus] = useState<"idle" | "sent" | "error">("idle");
  const [profile, setProfile] = useState<UserProfileInput | null>(null);
  const [profileVisible, setProfileVisible] = useState(false);
  const [profileName, setProfileName] = useState("");
  const [profileEmail, setProfileEmail] = useState("");
  const [profilePhone, setProfilePhone] = useState("");
  const [emergencyContacts, setEmergencyContacts] = useState<EmergencyContact[]>([]);
  const [centralName, setCentralName] = useState(CENTRAL_MAX_PROFILE.name);
  const [centralPhone, setCentralPhone] = useState(CENTRAL_MAX_PROFILE.phone);
  const [profileError, setProfileError] = useState("");
  const [profileSaved, setProfileSaved] = useState(false);
  const [profileSyncStatus, setProfileSyncStatus] = useState<"idle" | "syncing" | "synced" | "error">("idle");
  const [profileSyncId, setProfileSyncId] = useState<string | null>(null);
  const [pushStatus, setPushStatus] = useState<"idle" | "registered" | "unavailable" | "error">("idle");
  const [aiVisible, setAiVisible] = useState(false);
  const [pixVisible, setPixVisible] = useState(false);
  const [qrVisible, setQrVisible] = useState(false);
  const [coupon, setCoupon] = useState<Merchant | null>(null);
  const [aiText, setAiText] = useState("");
  const [aiMessages, setAiMessages] = useState<{ from: "user" | "ai"; text: string }[]>([{ from: "ai", text: "Olá, Carlos. Sou o Max IA. Posso ajudar com proteção, saúde ou benefícios em Apiaí." }]);
  const sosMutation = trpc.sos.send.useMutation();
  const cancelSosMutation = trpc.sos.cancel.useMutation();
  const profileSyncMutation = trpc.profile.sync.useMutation();
  const profileQuery = trpc.profile.get.useQuery(undefined, { enabled: isAuthenticated, retry: false });
  const { mutateAsync: registerPushAsync } = trpc.push.register.useMutation();

  useEffect(() => {
    if (!isAuthenticated || Platform.OS === "web") return;
    let active = true;
    void registerForPushNotificationsAsync().then((token) => {
      if (!active) return;
      if (!token) { setPushStatus("unavailable"); return; }
      return registerPushAsync({ token, platform: Platform.OS as "ios" | "android" }).then(() => setPushStatus("registered")).catch(() => setPushStatus("error"));
    }).catch(() => { if (active) setPushStatus("error"); });
    return () => { active = false; };
  }, [isAuthenticated, registerPushAsync]);

  useEffect(() => {
    const remoteProfile = profileQuery.data;
    if (!remoteProfile) return;
    const nextProfile: UserProfileInput = { name: remoteProfile.name, email: remoteProfile.email, phone: remoteProfile.phone, contacts: remoteProfile.contacts as EmergencyContact[], central: remoteProfile.central };
    setProfile(nextProfile);
    setProfileName(nextProfile.name);
    setProfileEmail(nextProfile.email);
    setProfilePhone(nextProfile.phone);
    setEmergencyContacts(nextProfile.contacts ?? []);
    setCentralName(nextProfile.central?.name ?? CENTRAL_MAX_PROFILE.name);
    setCentralPhone(nextProfile.central?.phone ?? CENTRAL_MAX_PROFILE.phone);
  }, [profileQuery.data]);

  useEffect(() => {
    if (sosApiStatus !== "sent") return;
    setCentralStatus("received");
    const timers = [
      setTimeout(() => setCentralStatus("dispatching"), 2200),
      setTimeout(() => setCentralStatus("enroute"), 5200),
      setTimeout(() => setCentralStatus("arrived"), 8200),
    ];
    return () => timers.forEach(clearTimeout);
  }, [sosApiStatus]);

  useEffect(() => {
    void AsyncStorage.getItem(PROFILE_STORAGE_KEY).then((stored) => {
      if (!stored) return;
      try {
        const saved = JSON.parse(stored) as UserProfileInput;
        if (!validateUserProfile(saved)) {
          setProfile(saved);
          setProfileName(saved.name);
          setProfileEmail(saved.email);
          setProfilePhone(saved.phone);
          setEmergencyContacts(saved.contacts ?? []);
          setCentralName(saved.central?.name ?? CENTRAL_MAX_PROFILE.name);
          setCentralPhone(saved.central?.phone ?? CENTRAL_MAX_PROFILE.phone);
        }
      } catch {
        // Keep the form available when local data is malformed.
      }
    });
  }, []);

  const sendAi = () => {
    const trimmed = aiText.trim();
    if (!trimmed) return;
    triggerHaptic();
    setAiMessages((current) => [...current, { from: "user", text: trimmed }, { from: "ai", text: getMaxAiReply(trimmed) }]);
    setAiText("");
  };

  const openProfile = () => {
    setProfileName(profile?.name ?? "");
    setProfileEmail(profile?.email ?? "");
    setProfilePhone(profile?.phone ?? "");
    setEmergencyContacts(profile?.contacts ?? []);
    setCentralName(profile?.central?.name ?? CENTRAL_MAX_PROFILE.name);
    setCentralPhone(profile?.central?.phone ?? CENTRAL_MAX_PROFILE.phone);
    setProfileError("");
    setProfileSaved(false);
    setProfileSyncStatus("idle");
    setProfileSyncId(null);
    setProfileVisible(true);
  };

  const saveProfile = async () => {
    const nextProfile = { name: profileName, email: profileEmail, phone: profilePhone, contacts: emergencyContacts, central: { name: centralName, phone: centralPhone } };
    const validationError = validateUserProfile(nextProfile);
    if (validationError) {
      setProfileError(validationError);
      return;
    }
    const invalidContact = emergencyContacts.find((contact) => validateEmergencyContact(contact));
    if (invalidContact) {
      setProfileError(validateEmergencyContact(invalidContact) ?? "Revise o contato de emergência.");
      return;
    }
    await AsyncStorage.setItem(PROFILE_STORAGE_KEY, JSON.stringify(nextProfile));
    setProfile(nextProfile);
    setProfileError("");
    setProfileSaved(true);
    if (!isAuthenticated) {
      setProfileSyncStatus("error");
      setProfileError("Entre com sua conta Max para sincronizar este perfil com segurança.");
      triggerHaptic(Haptics.ImpactFeedbackStyle.Medium);
      return;
    }
    setProfileSyncStatus("syncing");
    try {
      const response = await profileSyncMutation.mutateAsync(nextProfile);
      setProfileSyncId(response.profileId);
      setProfileSyncStatus("synced");
    } catch {
      setProfileSyncStatus("error");
    }
    triggerHaptic(Haptics.ImpactFeedbackStyle.Medium);
  };

  const updateEmergencyContact = (index: number, field: keyof EmergencyContact, value: string) => {
    setEmergencyContacts((current) => current.map((contact, contactIndex) => contactIndex === index ? { ...contact, [field]: value } : contact));
    setProfileError("");
  };

  const addEmergencyContact = () => {
    if (emergencyContacts.length >= 3) return;
    setEmergencyContacts((current) => [...current, { name: "", phone: "", relationship: "" }]);
  };

  const setPrimaryContact = (index: number) => {
    setEmergencyContacts((current) => current.map((contact, contactIndex) => ({ ...contact, isPrimary: contactIndex === index })));
  };

  const openSos = () => {
    if (!isAuthenticated) {
      Alert.alert("Autenticação necessária", "Entre com sua conta Max para enviar um alerta SOS autenticado.");
      return;
    }
    setSosVisible(true);
    setSosSending(false);
    setSosApiStatus("idle");
    setSosApiId(null);
    setSosLocation(null);
    setSosLocationError(false);
    setCentralStatus("idle");
  };

  const handleSos = async () => {
      if (!isAuthenticated) {
      Alert.alert("Autenticação necessária", "Entre com sua conta Max para enviar um alerta SOS autenticado.");
      return;
    }
    triggerHaptic(Haptics.ImpactFeedbackStyle.Heavy);
    setSosVisible(true);
    setSosSending(true);
    setSosLocation(null);
    setSosLocationError(false);
    setSosApiStatus("idle");
    setSosApiId(null);
    setSosContactsNotified(0);
    setSosAttempt(0);
    setCentralStatus("idle");
    setCancelStatus("idle");
    let capturedLocation: SosLocation | null = null;
    try {
      const servicesEnabled = await Location.hasServicesEnabledAsync();
      if (!servicesEnabled) throw new Error("GPS desativado");
      const permission = await Location.requestForegroundPermissionsAsync();
      if (permission.status !== "granted") throw new Error("Permissão de localização negada");
      const current = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.High });
      capturedLocation = { latitude: current.coords.latitude, longitude: current.coords.longitude, accuracy: current.coords.accuracy };
      setSosLocation(capturedLocation);
    } catch {
      setSosLocationError(true);
    }
    let delivered = false;
    for (let attempt = 1; attempt <= SOS_MAX_ATTEMPTS && !delivered; attempt += 1) {
      setSosAttempt(attempt);
      setSosApiStatus("sending");
      try {
        const response = await sosMutation.mutateAsync({
          latitude: capturedLocation?.latitude ?? null,
          longitude: capturedLocation?.longitude ?? null,
          accuracy: capturedLocation?.accuracy ?? null,
          platform: Platform.OS,
          emergencyType,
          contacts: emergencyContacts,
        });
        setSosApiId(response.alertId);
        setSosContactsNotified(response.pushSent);
        setSosApiStatus("sent");
        delivered = true;
        if (Platform.OS !== "web") void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      } catch {
        if (attempt < SOS_MAX_ATTEMPTS) {
          await new Promise((resolve) => setTimeout(resolve, getSosRetryDelayMs(attempt)));
        }
      }
    }
    if (!delivered) {
      setSosApiStatus("error");
      if (Platform.OS !== "web") void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Error);
    }
    setSosSending(false);
    setSosNoticeVisible(true);
    setTimeout(() => setSosNoticeVisible(false), 4200);
  };

  const confirmCancelSos = async () => {
    if (!sosApiId) return;
    setCancelSending(true);
    try {
      await cancelSosMutation.mutateAsync({ alertId: sosApiId });
      setCancelStatus("sent");
      setCancelVisible(false);
      setCentralStatus("canceled");
      if (Platform.OS !== "web") void Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    } catch {
      setCancelStatus("error");
    } finally {
      setCancelSending(false);
    }
  };

  const content = tab === "home" ? <HomeView onNavigate={setTab} onSos={openSos} onAi={() => setAiVisible(true)} /> : tab === "carteira" ? <WalletView onQr={() => setQrVisible(true)} /> : tab === "afiliado" ? <AffiliateView onPix={() => setPixVisible(true)} /> : tab === "clube" ? <ClubView onCoupon={setCoupon} /> : tab === "pins" ? <PinsView /> : <TelemedicineView onBack={() => setTab("home")} />;

  return (
    <ScreenContainer edges={["top", "left", "right", "bottom"]} containerClassName="bg-background" safeAreaClassName="bg-background">
      <View style={styles.appShell}><Header onAi={() => setAiVisible(true)} onProfile={openProfile} onAdmin={() => router.push("/admin")} isAdmin={user?.role === "admin"} profile={profile} /><View style={styles.main}>{tab === "home" ? <HomeView onNavigate={setTab} onSos={openSos} onAi={() => setAiVisible(true)} centralProfile={centralProfileQuery.data} /> : content}</View>{sosNoticeVisible ? <View style={[styles.sosToast, centralStatus !== "idle" && styles.sosToastRaised, sosApiStatus === "error" && styles.sosToastError]}><MaterialIcons name={sosApiStatus === "sent" ? "check-circle" : "error-outline"} size={19} color={sosApiStatus === "sent" ? C.success : C.red} /><View style={{ flex: 1 }}><Text style={styles.sosToastTitle}>{sosApiStatus === "sent" ? "SOS recebido pela API Max" : "Falha ao enviar SOS"}</Text><Text style={styles.sosToastBody}>{sosApiStatus === "sent" ? `${sosLocation ? "GPS anexado" : "Sem GPS"} · protocolo confirmado.` : "Tente novamente ou fale com a Central."}</Text></View></View> : null}{centralStatus !== "idle" ? <View style={styles.centralStatusCard}><View style={styles.centralStatusIcon}><MaterialIcons name={CENTRAL_STATUS_COPY[centralStatus].icon} size={20} color={C.success} /></View><View style={{ flex: 1 }}><Text style={styles.centralStatusTitle}>{CENTRAL_STATUS_COPY[centralStatus].title}</Text><Text style={styles.centralStatusBody}>{CENTRAL_STATUS_COPY[centralStatus].body}</Text><View style={styles.centralProgress}><View style={styles.centralProgressFill} /></View></View><View style={styles.centralStatusActions}><Text style={styles.centralStatusProtocol}>{sosApiId ?? "Protocolo ativo"}</Text>{centralStatus !== "arrived" && centralStatus !== "canceled" ? <Pressable onPress={() => setCancelVisible(true)} style={({ pressed }) => [styles.centralCancelButton, pressed && styles.pressed]}><MaterialIcons name="cancel" size={15} color={C.red} /><Text style={styles.centralCancelText}>Cancelar</Text></Pressable> : null}</View></View> : null}{tab !== "telemedicina" ? <BottomNav tab={tab} onChange={setTab} /> : null}</View>
      <ModalShell visible={sosVisible} onClose={() => setSosVisible(false)} title={sosApiStatus === "idle" ? "Classificar alerta SOS" : sosSending ? "Confirmando alerta SOS" : sosApiStatus === "error" ? "Falha no envio do SOS" : centralStatus === "canceled" ? "Alerta SOS cancelado" : "Alerta SOS enviado"} subtitle="Central Max Apiahy · protocolo prioritário"><View style={styles.modalSuccess}>{sosApiStatus === "idle" && !sosSending ? <SosTypePicker value={emergencyType} onChange={setEmergencyType} onConfirm={() => void handleSos()} /> : sosSending ? <><View style={styles.modalIconRed}><MaterialIcons name={sosApiStatus === "sending" ? "cloud-upload" : "my-location"} size={28} color={C.gold} /></View><Text style={styles.modalHeadline}>{sosApiStatus === "sending" ? "Enviando para a Central..." : "Obtendo sua localização..."}</Text><Text style={styles.modalBody}>{sosApiStatus === "sending" ? `O backend autenticado está confirmando o alerta. Tentativa ${sosAttempt} de ${SOS_MAX_ATTEMPTS}.` : "Estamos solicitando o GPS de alta precisão para anexar ao alerta da Central Max."}</Text><View style={styles.responseBox}><MaterialIcons name={sosApiStatus === "sending" ? "sync" : "gps-fixed"} size={18} color={C.gold} /><View><Text style={styles.responseTitle}>{sosApiStatus === "sending" ? "Retry automático em andamento" : "Captura GPS em andamento"}</Text><Text style={styles.responseBody}>{sosApiStatus === "sending" ? "Se a rede falhar, tentaremos novamente com backoff." : "Mantenha o app aberto por alguns segundos."}</Text></View></View></> : <><View style={styles.modalIconRed}><MaterialIcons name={sosApiStatus === "sent" ? "check-circle" : "error-outline"} size={28} color={sosApiStatus === "sent" ? C.success : C.red} /></View><Text style={styles.modalHeadline}>{centralStatus === "canceled" ? "O alerta foi cancelado com segurança." : sosApiStatus === "sent" ? "Sua família está sendo assistida." : "Não conseguimos confirmar o envio."}</Text><Text style={styles.modalBody}>{sosApiStatus === "sent" ? "O backend autenticado confirmou o alerta e a Central Max recebeu o protocolo." : "O alerta foi preparado, mas o backend não respondeu após as tentativas. Tente novamente em alguns instantes."}</Text>{sosApiStatus === "sent" ? <View style={styles.responseBox}><MaterialIcons name="cloud-done" size={18} color={C.success} /><View><Text style={styles.responseTitle}>Backend confirmou recebimento</Text><Text style={styles.responseBody}>{sosApiId ?? "Protocolo gerado"} · {sosLocation ? formatSosCoordinates(sosLocation.latitude, sosLocation.longitude) : "GPS não confirmado"} · {sosContactsNotified} push(s) entregue(s) ao titular</Text></View></View> : null}{sosLocation ? <View style={styles.responseBox}><MaterialIcons name="gps-fixed" size={18} color={C.success} /><View><Text style={styles.responseTitle}>GPS anexado ao alerta</Text><Text style={styles.responseBody}>{formatSosCoordinates(sosLocation.latitude, sosLocation.longitude)} · precisão {sosLocation.accuracy ? `${Math.round(sosLocation.accuracy)} m` : "indisponível"}</Text></View></View> : <View style={[styles.responseBox, { backgroundColor: `${C.gold}12`, borderColor: `${C.gold}44` }]}><MaterialIcons name="location-off" size={18} color={C.gold} /><View><Text style={[styles.responseTitle, { color: C.gold }]}>Alerta sem coordenadas</Text><Text style={styles.responseBody}>{sosLocationError ? "Ative a localização nas configurações para melhorar a resposta." : "A central recebeu o protocolo."}</Text></View></View>}</>}{sosApiStatus === "sent" && centralStatus !== "arrived" && centralStatus !== "canceled" ? <Pressable onPress={() => { triggerHaptic(); setCancelVisible(true); }} style={({ pressed }) => [styles.cancelButton, pressed && styles.pressed]}><MaterialIcons name="cancel" size={18} color={C.red} /><Text style={styles.cancelButtonText}>Cancelar alerta SOS</Text></Pressable> : null}<Pressable onPress={() => { setSosVisible(false); if (Platform.OS !== "web") void Linking.openURL(`tel:${centralPhone}`); else Alert.alert(centralName, `Ligação disponível pelo número ${centralPhone}.`); }} style={({ pressed }) => [styles.primaryRedButton, pressed && styles.pressed]}><MaterialIcons name="phone" size={18} color={C.white} /><Text style={styles.buttonText}>Falar com a Central Max</Text></Pressable><Pressable onPress={() => setSosVisible(false)} style={({ pressed }) => [styles.secondaryButton, pressed && styles.pressed]}><Text style={styles.buttonText}>Entendi, estou seguro</Text></Pressable></View></ModalShell>
      <ModalShell visible={profileVisible} onClose={() => setProfileVisible(false)} title="Cadastro do usuário" subtitle="Seus dados ficam salvos neste aparelho">
        <ScrollView style={styles.profileScroll} showsVerticalScrollIndicator={false}>
          <View style={styles.profileModal}>
            <View style={styles.profileAvatar}><MaterialIcons name="person" size={28} color={C.gold} /></View>
            <Text style={styles.profileIntro}>{profile ? "Atualize seus dados para manter seu atendimento Max completo." : "Cadastre seus dados para agilizar o atendimento da Central Max."}</Text>
            {!isAuthenticated ? <View style={styles.profileSyncError}><MaterialIcons name="lock" size={16} color={C.red} /><Text style={styles.profileSyncErrorText}>Conta Max não autenticada: o perfil será salvo localmente, mas não será sincronizado.</Text></View> : null}
            {isAuthenticated && pushStatus === "registered" ? <View style={styles.profileSync}><MaterialIcons name="notifications-active" size={16} color={C.success} /><Text style={styles.profileSyncText}>Notificações push ativas neste aparelho.</Text></View> : null}
            {isAuthenticated && pushStatus === "unavailable" ? <View style={styles.profileSync}><MaterialIcons name="notifications-off" size={16} color={C.gold} /><Text style={styles.profileSyncText}>Push pendente: configure o EXPO_PROJECT_ID em uma build nativa.</Text></View> : null}
            <TextInput value={profileName} onChangeText={setProfileName} placeholder="Nome completo" placeholderTextColor={C.muted} style={styles.profileInput} autoCapitalize="words" />
            <TextInput value={profileEmail} onChangeText={setProfileEmail} placeholder="E-mail" placeholderTextColor={C.muted} style={styles.profileInput} keyboardType="email-address" autoCapitalize="none" />
            <TextInput value={profilePhone} onChangeText={setProfilePhone} placeholder="Telefone" placeholderTextColor={C.muted} style={styles.profileInput} keyboardType="phone-pad" />
            <View style={styles.contactsSection}>
              <View style={styles.contactsHeader}>
                <View><Text style={styles.contactsTitle}>CONTATOS DE EMERGÊNCIA</Text><Text style={styles.contactsHint}>Serão avisados durante um alerta SOS.</Text></View>
                {emergencyContacts.length < 3 ? <Pressable onPress={addEmergencyContact} style={({ pressed }) => [styles.addContactButton, pressed && styles.pressed]}><MaterialIcons name="person-add" size={15} color={C.gold} /><Text style={styles.addContactText}>Adicionar</Text></Pressable> : null}
              </View>
              {emergencyContacts.map((contact, index) => (
                <View key={`contact-${index}`} style={styles.contactCard}>
                  <View style={styles.contactCardHeader}>
                    <Text style={styles.contactNumber}>CONTATO {index + 1}</Text>
                    <View style={styles.contactHeaderActions}>
                      <Pressable onPress={() => setPrimaryContact(index)} style={({ pressed }) => [styles.primaryContactButton, contact.isPrimary && styles.primaryContactActive, pressed && styles.pressed]}>
                        <MaterialIcons name={contact.isPrimary ? "star" : "star-border"} size={15} color={contact.isPrimary ? C.gold : C.muted} />
                        <Text style={[styles.primaryContactText, contact.isPrimary && styles.primaryContactTextActive]}>{contact.isPrimary ? "Principal" : "Definir principal"}</Text>
                      </Pressable>
                      <Pressable onPress={() => setEmergencyContacts((current) => current.filter((_, contactIndex) => contactIndex !== index))} accessibilityLabel={`Remover contato ${index + 1}`}>
                        <MaterialIcons name="delete-outline" size={18} color={C.red} />
                      </Pressable>
                    </View>
                  </View>
                  <TextInput value={contact.name} onChangeText={(value) => updateEmergencyContact(index, "name", value)} placeholder="Nome do contato" placeholderTextColor={C.muted} style={styles.profileInput} autoCapitalize="words" />
                  <View style={styles.contactInputRow}>
                    <TextInput value={contact.phone} onChangeText={(value) => updateEmergencyContact(index, "phone", value)} placeholder="Telefone" placeholderTextColor={C.muted} style={[styles.profileInput, styles.contactHalf]} keyboardType="phone-pad" />
                    <TextInput value={contact.relationship} onChangeText={(value) => updateEmergencyContact(index, "relationship", value)} placeholder="Parentesco" placeholderTextColor={C.muted} style={[styles.profileInput, styles.contactHalf]} />
                  </View>
                </View>
              ))}
            </View>
            <View style={styles.centralConfig}>
              <Text style={styles.contactsTitle}>CENTRAL MAX</Text>
              <Text style={styles.contactsHint}>{CENTRAL_MAX_PROFILE.service} · {CENTRAL_MAX_PROFILE.availability} · {CENTRAL_MAX_PROFILE.responseTarget}.</Text>
              <Text style={styles.contactsHint}>Canais: {CENTRAL_MAX_PROFILE.channels.join(" · ")}.</Text>
              <TextInput value={centralName} onChangeText={setCentralName} placeholder="Nome da Central" placeholderTextColor={C.muted} style={styles.profileInput} />
              <TextInput value={centralPhone} onChangeText={setCentralPhone} placeholder="Telefone da Central" placeholderTextColor={C.muted} style={styles.profileInput} keyboardType="phone-pad" />
            </View>
            {profileError ? <Text style={styles.profileError}>{profileError}</Text> : null}
            {profileSaved ? <View style={styles.profileSuccess}><MaterialIcons name="check-circle" size={16} color={C.success} /><Text style={styles.profileSuccessText}>Cadastro salvo com sucesso.</Text></View> : null}
            {profileSyncStatus === "syncing" ? <View style={styles.profileSync}><MaterialIcons name="sync" size={16} color={C.gold} /><Text style={styles.profileSyncText}>Sincronizando com a Central Max...</Text></View> : profileSyncStatus === "synced" ? <View style={styles.profileSync}><MaterialIcons name="cloud-done" size={16} color={C.success} /><Text style={styles.profileSyncText}>Perfil sincronizado{profileSyncId ? ` · ${profileSyncId}` : ""}</Text></View> : profileSyncStatus === "error" ? <View style={[styles.profileSync, styles.profileSyncError]}><MaterialIcons name="cloud-off" size={16} color={C.red} /><Text style={styles.profileSyncErrorText}>Salvo no aparelho; sincronização pendente.</Text></View> : null}
            <Pressable onPress={() => void saveProfile()} style={({ pressed }) => [styles.goldButtonLarge, pressed && styles.pressed]}><MaterialIcons name="save" size={18} color={C.bg} /><Text style={styles.darkButtonText}>Salvar cadastro</Text></Pressable>
          </View>
        </ScrollView>
      </ModalShell>
      <ModalShell visible={cancelVisible} onClose={() => setCancelVisible(false)} title="Cancelar alerta SOS" subtitle="Confirmação necessária"><View style={styles.modalSuccess}><View style={styles.cancelModalIcon}><MaterialIcons name={cancelStatus === "error" ? "error-outline" : "warning-amber"} size={30} color={cancelStatus === "error" ? C.red : C.gold} /></View><Text style={styles.modalHeadline}>{cancelSending ? "Cancelando atendimento..." : cancelStatus === "error" ? "Não foi possível cancelar" : "Deseja cancelar o alerta?"}</Text><Text style={styles.modalBody}>{cancelSending ? "A Central Max está encerrando a solicitação com segurança." : cancelStatus === "error" ? "A Central ainda não confirmou o cancelamento. Tente novamente." : "A equipe pode já estar em deslocamento. Confirme somente se você não precisa mais da pronta resposta."}</Text>{cancelStatus === "sent" ? <View style={styles.responseBox}><MaterialIcons name="check-circle" size={18} color={C.success} /><View><Text style={styles.responseTitle}>Alerta cancelado</Text><Text style={styles.responseBody}>A Central Max foi avisada e encerrou o atendimento.</Text></View></View> : <><Pressable disabled={cancelSending} onPress={confirmCancelSos} style={({ pressed }) => [styles.primaryRedButton, pressed && styles.pressed, cancelSending && styles.disabledButton]}><MaterialIcons name={cancelSending ? "sync" : "cancel"} size={18} color={C.white} /><Text style={styles.buttonText}>{cancelSending ? "Cancelando..." : "Sim, cancelar alerta"}</Text></Pressable><Pressable disabled={cancelSending} onPress={() => setCancelVisible(false)} style={({ pressed }) => [styles.secondaryButton, pressed && styles.pressed]}><Text style={styles.buttonText}>Não, manter atendimento</Text></Pressable></>}</View></ModalShell>
      <ModalShell visible={pixVisible} onClose={() => setPixVisible(false)} title="Solicitar saque PIX" subtitle="Saldo disponível para validação"><View style={styles.modalSuccess}><View style={styles.pixHeader}><MaterialIcons name="pix" size={30} color={C.gold} /><View><Text style={styles.quickTitle}>Saldo recorrente</Text><Text style={styles.balanceSmall}>R$ 1.169,70</Text></View></View><Text style={styles.modalBody}>Informe o valor que deseja solicitar. A transferência será processada após a validação do seu cadastro.</Text><View style={styles.amountInput}><Text style={styles.amountPrefix}>R$</Text><TextInput style={styles.amountText} placeholder="0,00" placeholderTextColor={C.muted} keyboardType="decimal-pad" defaultValue="700,00" /></View><Pressable onPress={() => { setPixVisible(false); Alert.alert("Solicitação enviada", "Seu saque PIX foi encaminhado para validação cadastral."); }} style={({ pressed }) => [styles.goldButtonLarge, pressed && styles.pressed]}><MaterialIcons name="pix" size={18} color={C.bg} /><Text style={styles.darkButtonText}>Confirmar solicitação</Text></Pressable></View></ModalShell>
      <ModalShell visible={qrVisible} onClose={() => setQrVisible(false)} title="QR Code Max Club" subtitle="Apresente no caixa do parceiro"><View style={styles.qrModalBody}><QrCode size={210} /><Text style={styles.qrModalTitle}>MAX-CLUB-APIAHY-CARLOS-8842</Text><Text style={styles.modalBody}>Válido para uso pessoal e intransferível em parceiros credenciados.</Text><Pressable onPress={() => setQrVisible(false)} style={({ pressed }) => [styles.bordoButtonLarge, pressed && styles.pressed]}><Text style={styles.buttonText}>Fechar</Text></Pressable></View></ModalShell>
      <ModalShell visible={coupon !== null} onClose={() => setCoupon(null)} title="Cupom Max Club" subtitle={coupon?.name}><View style={styles.couponModal}><View style={styles.couponSeal}><MaterialIcons name="local-offer" size={32} color={C.gold} /></View><Text style={styles.couponValue}>{coupon?.discount}</Text><Text style={styles.modalBody}>Mostre seu cartão virtual Max Club no caixa para validar este benefício.</Text><View style={styles.couponCode}><Text style={styles.couponCodeLabel}>CÓDIGO DO BENEFÍCIO</Text><Text style={styles.couponCodeValue}>MAX-APIAHY-8842</Text></View><Pressable onPress={() => { setCoupon(null); Alert.alert("Benefício salvo", "O cupom foi salvo na sua carteira virtual."); }} style={({ pressed }) => [styles.goldButtonLarge, pressed && styles.pressed]}><MaterialIcons name="bookmark" size={18} color={C.bg} /><Text style={styles.darkButtonText}>Salvar na carteira</Text></Pressable></View></ModalShell>
      <ModalShell visible={aiVisible} onClose={() => setAiVisible(false)} title="Max IA" subtitle="Assistente inteligente Max Seg & Max Saúde"><View style={styles.chatBody}><ScrollView style={styles.chatMessages} contentContainerStyle={{ gap: 10 }}><View style={styles.aiNotice}><MaterialIcons name="auto-awesome" size={14} color={C.gold} /><Text style={styles.aiNoticeText}>Respostas rápidas sobre seus benefícios</Text></View>{aiMessages.map((message, index) => <View key={`${message.from}-${index}`} style={[styles.chatBubble, message.from === "user" ? styles.userBubble : styles.aiBubble]}><Text style={styles.chatText}>{message.text}</Text></View>)}</ScrollView><View style={styles.chatComposer}><TextInput value={aiText} onChangeText={setAiText} onSubmitEditing={sendAi} returnKeyType="done" placeholder="Escreva sua dúvida..." placeholderTextColor={C.muted} style={styles.chatInput} /><Pressable onPress={sendAi} style={({ pressed }) => [styles.sendButton, pressed && styles.pressed]}><MaterialIcons name="send" size={17} color={C.white} /></Pressable></View></View></ModalShell>
    </ScreenContainer>
  );
}

const styles = StyleSheet.create({
  appShell: { flex: 1, backgroundColor: C.bg },
  header: { minHeight: 70, paddingHorizontal: 16, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.border, backgroundColor: "#111111EE", flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  brandRow: { flexDirection: "row", alignItems: "center", gap: 10 },
  logoMark: { width: 40, height: 40, borderRadius: 12, alignItems: "center", justifyContent: "center", backgroundColor: C.bordo, borderWidth: 1, borderColor: `${C.gold}55` },
  brandLine: { flexDirection: "row", alignItems: "center", gap: 5 },
  brandName: { color: C.white, fontSize: 14, fontWeight: "900", letterSpacing: 1.4 },
  brandCity: { color: C.bordoLight, fontSize: 10, fontWeight: "800" },
  statusLine: { flexDirection: "row", alignItems: "center", gap: 5, marginTop: 3 },
  onlineDot: { width: 7, height: 7, borderRadius: 4, backgroundColor: C.success },
  statusText: { color: C.muted, fontSize: 9.5 },
  headerRight: { flexDirection: "row", alignItems: "center", gap: 8 },
  aiButton: { width: 36, height: 36, borderRadius: 18, backgroundColor: C.panel, borderWidth: 1, borderColor: `${C.gold}55`, alignItems: "center", justifyContent: "center", position: "relative" },
  notificationDot: { position: "absolute", top: 0, right: 0, width: 9, height: 9, borderRadius: 5, backgroundColor: C.bordoLight, borderWidth: 1, borderColor: C.bg },
  main: { flex: 1 },
  scrollContent: { padding: 16, paddingBottom: 28, gap: 14 },
  card: { backgroundColor: `${C.panel}CC`, borderWidth: 1, borderColor: `${C.white}12`, borderRadius: 16, padding: 15, overflow: "hidden" },
  statusCard: { borderLeftWidth: 4, borderLeftColor: C.success, position: "relative" },
  statusCardGlow: { position: "absolute", right: -12, bottom: -12, opacity: 0.06 },
  rowBetween: { flexDirection: "row", justifyContent: "space-between", alignItems: "center" },
  eyebrow: { color: C.muted, fontSize: 9.5, fontWeight: "800", letterSpacing: 1.2 },
  eyebrowSuccess: { color: C.success, fontSize: 9.5, fontWeight: "800", letterSpacing: 1.1 },
  eyebrowBlue: { color: C.blue, fontSize: 9.5, fontWeight: "800", letterSpacing: 1.1 },
  tinyMuted: { color: C.muted, fontSize: 9 },
  cardTitle: { color: C.white, fontSize: 17, fontWeight: "800", marginTop: 8, textTransform: "capitalize" },
  bodyText: { color: "#D4D4D8", fontSize: 11.5, lineHeight: 17, marginTop: 5 },
  pill: { borderRadius: 999, borderWidth: 1, paddingHorizontal: 9, paddingVertical: 5, alignSelf: "flex-start" },
  pillText: { fontSize: 9.5, fontWeight: "800", letterSpacing: 0.35 },
  sosCard: { alignItems: "center", borderColor: `${C.bordo}88`, paddingVertical: 20 },
  sosWrap: { marginVertical: 10 },
  sosRing: { width: 190, height: 190, borderRadius: 95, borderWidth: 8, borderColor: `${C.bordo}66`, alignItems: "center", justifyContent: "center", padding: 7 },
  sosRingActive: { borderColor: C.red, shadowColor: C.red, shadowOpacity: 0.8, shadowRadius: 18, elevation: 12 },
  sosButton: { width: 164, height: 164, borderRadius: 82, backgroundColor: C.bordo, borderWidth: 2, borderColor: `${C.red}AA`, alignItems: "center", justifyContent: "center", shadowColor: C.bordo, shadowOpacity: 0.8, shadowRadius: 18, elevation: 10 },
  sosPressed: { transform: [{ scale: 0.96 }], backgroundColor: C.bordoLight },
  sosLabel: { color: C.white, fontSize: 22, fontWeight: "900", marginTop: 4 },
  sosHint: { color: "#E5E7EB", fontSize: 9.5, marginTop: 3 },
  sosHelper: { color: C.muted, fontSize: 10.5, textAlign: "center", lineHeight: 16, maxWidth: 265 },
  quickGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
  quickCard: { width: "48.5%", minHeight: 112, backgroundColor: `${C.panel}CC`, borderWidth: 1, borderColor: `${C.white}10`, borderLeftWidth: 3, borderRadius: 12, padding: 12 },
  quickTitle: { color: C.white, fontSize: 11.5, fontWeight: "800", marginTop: 7 },
  quickBody: { color: C.muted, fontSize: 10, lineHeight: 14, marginTop: 3 },
  iconBox: { borderRadius: 10, alignItems: "center", justifyContent: "center" },
  sectionTitle: { color: "#D4D4D8", fontSize: 10, fontWeight: "800", letterSpacing: 1.1 },
  goldLink: { color: C.gold, fontSize: 10, fontWeight: "700" },
  activityRow: { flexDirection: "row", gap: 10, padding: 10, marginTop: 9, backgroundColor: `${C.carbon}AA`, borderRadius: 9, borderWidth: 1, borderColor: C.border },
  activityDot: { width: 8, height: 8, borderRadius: 4, marginTop: 4 },
  activityTitle: { color: "#E4E4E7", fontSize: 10.5, fontWeight: "600", flex: 1 },
  activityBody: { color: C.muted, fontSize: 10, lineHeight: 14, marginTop: 3 },
  bottomNav: { height: 68, flexDirection: "row", backgroundColor: "#101010F5", borderTopWidth: 1, borderTopColor: C.border, paddingTop: 8, paddingBottom: Platform.OS === "web" ? 8 : 12 },
  navItem: { flex: 1, alignItems: "center", justifyContent: "center", gap: 4, position: "relative" },
  navLabel: { color: C.muted, fontSize: 9, fontWeight: "700" },
  navActiveLine: { position: "absolute", top: -8, width: 26, height: 2, backgroundColor: C.gold, borderRadius: 1 },
  centerBlock: { alignItems: "center", gap: 4, paddingVertical: 4 },
  pageTitle: { color: C.white, fontSize: 19, fontWeight: "900", textAlign: "center" },
  pageSubtitle: { color: C.muted, fontSize: 11, textAlign: "center", lineHeight: 16 },
  vipCard: { height: 220, backgroundColor: C.carbon, borderRadius: 18, borderWidth: 2, borderColor: `${C.gold}66`, overflow: "hidden", shadowColor: C.gold, shadowOpacity: 0.15, shadowRadius: 18, elevation: 8 },
  cardLayer: { ...StyleSheet.absoluteFillObject, backfaceVisibility: "hidden" as const },
  vipCardBack: { backgroundColor: "#F7F7F7", borderColor: `${C.bordo}77` },
  cardFace: { flex: 1, padding: 16, justifyContent: "space-between" },
  goldLine: { position: "absolute", top: 0, left: 0, right: 0, height: 4, backgroundColor: C.gold },
  goldLineBottom: { position: "absolute", bottom: 0, left: 0, right: 0, height: 4, backgroundColor: C.bordo },
  miniLogo: { width: 31, height: 31, borderRadius: 8, backgroundColor: C.bordo, alignItems: "center", justifyContent: "center", borderWidth: 1, borderColor: `${C.gold}66` },
  vipBrand: { color: C.white, fontSize: 11, fontWeight: "900", letterSpacing: 1.3 },
  vipCaption: { color: C.muted, fontSize: 8, fontWeight: "700", letterSpacing: 1 },
  hologram: { width: 42, height: 42, borderRadius: 21, backgroundColor: C.gold, alignItems: "center", justifyContent: "center", opacity: 0.9 },
  hologramText: { color: C.bg, fontSize: 7, lineHeight: 9, fontWeight: "900", textAlign: "center" },
  chip: { width: 42, height: 30, borderRadius: 6, backgroundColor: "#D9A928", borderWidth: 1, borderColor: "#8E650E", padding: 5, justifyContent: "space-around" },
  chipLine: { height: 2, backgroundColor: "#996F15", borderRadius: 2 },
  memberName: { color: C.white, fontSize: 13, fontWeight: "800", marginTop: 3 },
  memberId: { color: C.gold, fontSize: 9, letterSpacing: 1.2, marginTop: 3 },
  backFace: { flex: 1, padding: 15, justifyContent: "space-between" },
  magStripe: { position: "absolute", top: 20, left: 0, right: 0, height: 30, backgroundColor: C.bg },
  qrRow: { flexDirection: "row", alignItems: "center", marginTop: 38 },
  qrCode: { backgroundColor: "#FFF", flexDirection: "row", flexWrap: "wrap", alignContent: "flex-start", borderRadius: 5, overflow: "hidden" },
  qrTitle: { color: C.bordo, fontSize: 11, fontWeight: "900", lineHeight: 14 },
  backText: { color: "#52525B", fontSize: 9.5, lineHeight: 13, marginTop: 4 },
  backPhone: { color: "#71717A", fontSize: 8.5, marginTop: 4 },
  backFooter: { flexDirection: "row", justifyContent: "space-between", borderTopWidth: 1, borderTopColor: "#D4D4D8", paddingTop: 5 },
  backFooterText: { color: "#71717A", fontSize: 8, fontWeight: "700" },
  validatorRow: { flexDirection: "row", alignItems: "center" },
  bordoButton: { backgroundColor: C.bordo, paddingHorizontal: 11, paddingVertical: 9, borderRadius: 9, flexDirection: "row", alignItems: "center", gap: 5 },
  buttonText: { color: C.white, fontSize: 11, fontWeight: "800" },
  benefitGrid: { flexDirection: "row", flexWrap: "wrap", gap: 8 },
  benefitItem: { width: "48.5%", backgroundColor: C.carbon, borderRadius: 9, borderWidth: 1, borderColor: C.border, padding: 10, flexDirection: "row", alignItems: "center", gap: 7 },
  benefitText: { color: "#E4E4E7", fontSize: 9.5, flex: 1 },
  balanceCard: { borderLeftWidth: 4, borderLeftColor: C.gold },
  balance: { color: C.gold, fontSize: 26, fontWeight: "900", marginTop: 4 },
  goldButton: { backgroundColor: C.gold, paddingHorizontal: 11, paddingVertical: 9, borderRadius: 9, flexDirection: "row", alignItems: "center", gap: 5 },
  darkButtonText: { color: C.bg, fontSize: 10.5, fontWeight: "900" },
  statsRow: { flexDirection: "row", gap: 30, marginTop: 14, paddingTop: 11, borderTopWidth: 1, borderTopColor: C.border },
  statLabel: { color: C.muted, fontSize: 9.5 },
  statValue: { color: C.white, fontSize: 12, fontWeight: "800", marginTop: 3 },
  statValueGreen: { color: C.success, fontSize: 12, fontWeight: "800", marginTop: 3 },
  rankIcon: { width: 34, height: 34, borderRadius: 17, backgroundColor: "#475569", alignItems: "center", justifyContent: "center" },
  percent: { color: C.gold, fontSize: 12, fontWeight: "900" },
  progressTrack: { height: 10, backgroundColor: C.carbon, borderRadius: 6, overflow: "hidden", marginTop: 13, borderWidth: 1, borderColor: C.border, padding: 1 },
  progressBar: { height: "100%", borderRadius: 6, backgroundColor: C.gold },
  goalText: { color: "#D4D4D8", backgroundColor: C.carbon, borderRadius: 8, padding: 9, fontSize: 9.5, lineHeight: 14, textAlign: "center", marginTop: 10 },
  goalStrong: { color: C.white, fontWeight: "800" },
  levelBox: { backgroundColor: C.carbon, borderRadius: 11, borderWidth: 1, borderColor: C.border, padding: 11, marginTop: 9 },
  levelBadge: { width: 29, height: 29, borderRadius: 15, alignItems: "center", justifyContent: "center" },
  levelBadgeText: { color: C.white, fontSize: 8, fontWeight: "900" },
  levelTitle: { color: C.white, fontSize: 10.5, fontWeight: "800" },
  levelValue: { color: C.success, fontSize: 10, fontWeight: "800" },
  memberList: { borderTopWidth: 1, borderTopColor: C.border, marginTop: 9, paddingTop: 7, gap: 6 },
  memberRow: { color: "#D4D4D8", fontSize: 9.5 },
  infoStrip: { flexDirection: "row", gap: 8, alignItems: "flex-start", backgroundColor: `${C.gold}0F`, borderWidth: 1, borderColor: `${C.gold}2E`, borderRadius: 10, padding: 11 },
  infoText: { color: C.muted, fontSize: 9.5, lineHeight: 14, flex: 1 },
  clubHero: { backgroundColor: C.bordo, borderRadius: 18, padding: 18, overflow: "hidden", position: "relative" },
  clubGlow: { position: "absolute", width: 160, height: 160, borderRadius: 80, right: -60, top: -60, backgroundColor: C.gold, opacity: 0.15 },
  clubKicker: { color: C.goldLight, fontSize: 9.5, letterSpacing: 1.4, fontWeight: "900" },
  clubTitle: { color: C.white, fontSize: 24, lineHeight: 29, fontWeight: "900", marginTop: 8, maxWidth: 250 },
  clubBody: { color: "#F3D7DE", fontSize: 11, lineHeight: 16, marginTop: 5, maxWidth: 270 },
  clubStats: { flexDirection: "row", gap: 25, marginTop: 17 },
  clubStatValue: { color: C.gold, fontSize: 20, fontWeight: "900" },
  clubStatLabel: { color: "#F3D7DE", fontSize: 8.5, marginTop: 1 },
  filterRow: { gap: 7, paddingVertical: 2 },
  filterChip: { paddingHorizontal: 11, paddingVertical: 8, backgroundColor: C.carbon, borderRadius: 8, borderWidth: 1, borderColor: C.border },
  filterChipActive: { backgroundColor: C.gold, borderColor: C.gold },
  filterText: { color: C.muted, fontSize: 9.5, fontWeight: "700" },
  filterTextActive: { color: C.bg },
  merchantCard: { flexDirection: "row", alignItems: "center", gap: 10, padding: 12 },
  merchantIcon: { width: 42, height: 42, borderRadius: 12, alignItems: "center", justifyContent: "center" },
  merchantName: { color: C.white, fontSize: 11.5, fontWeight: "800" },
  merchantTag: { color: C.muted, fontSize: 9, marginTop: 2 },
  merchantDiscount: { color: C.gold, fontSize: 9.5, fontWeight: "700", marginTop: 4 },
  couponButton: { backgroundColor: C.gold, paddingHorizontal: 9, paddingVertical: 8, borderRadius: 8 },
  partnerFooter: { flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 5, paddingVertical: 8 },
  partnerFooterText: { color: C.muted, fontSize: 9, textAlign: "center" },
  backLink: { flexDirection: "row", alignItems: "center", gap: 5, alignSelf: "flex-start" },
  teleHero: { alignItems: "center", gap: 10, paddingVertical: 10 },
  doctorAvatar: { width: 90, height: 90, borderRadius: 45, backgroundColor: `${C.blue}18`, borderWidth: 1, borderColor: `${C.blue}55`, alignItems: "center", justifyContent: "center" },
  callCard: { borderColor: `${C.blue}44` },
  primaryBlueButton: { backgroundColor: "#2563EB", paddingVertical: 12, borderRadius: 10, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 7, marginTop: 15 },
  stepRow: { flexDirection: "row", alignItems: "center", gap: 11, paddingVertical: 5 },
  stepNumber: { width: 30, height: 30, borderRadius: 15, backgroundColor: `${C.blue}22`, borderWidth: 1, borderColor: `${C.blue}55`, alignItems: "center", justifyContent: "center" },
  stepNumberText: { color: C.blue, fontWeight: "900", fontSize: 12 },
  pointsCard: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", borderColor: `${C.gold}44` },
  points: { color: C.gold, fontSize: 30, fontWeight: "900", marginTop: 3 },
  pointsUnit: { fontSize: 12, color: C.goldLight },
  pointsBadge: { alignItems: "center", gap: 4 },
  pointsBadgeText: { color: C.gold, fontSize: 8.5, fontWeight: "900" },
  pinGrid: { flexDirection: "row", flexWrap: "wrap", gap: 10 },
  pinCard: { width: "48.5%", backgroundColor: C.panel, borderRadius: 14, borderWidth: 1, borderColor: `${C.gold}33`, padding: 13, minHeight: 154, alignItems: "center" },
  pinLocked: { opacity: 0.65, borderColor: C.border },
  pinIcon: { width: 58, height: 58, borderRadius: 29, alignItems: "center", justifyContent: "center", marginBottom: 8 },
  pinTitle: { color: C.white, fontSize: 11, fontWeight: "800", textAlign: "center" },
  pinBody: { color: C.muted, fontSize: 9, lineHeight: 13, textAlign: "center", marginVertical: 5, minHeight: 26 },
  pinLock: { marginTop: 5 },
  modalBackdrop: { flex: 1, backgroundColor: "#000000B8", justifyContent: "center", padding: 18 },
  modalCard: { backgroundColor: C.panel, borderWidth: 1, borderColor: `${C.gold}44`, borderRadius: 18, padding: 16, maxHeight: "88%" },
  modalHeader: { flexDirection: "row", alignItems: "flex-start", marginBottom: 14 },
  modalTitle: { color: C.white, fontSize: 18, fontWeight: "900" },
  modalSubtitle: { color: C.muted, fontSize: 10, marginTop: 3 },
  closeButton: { width: 30, height: 30, borderRadius: 15, backgroundColor: C.carbon, alignItems: "center", justifyContent: "center" },
  modalSuccess: { alignItems: "center", gap: 10 },
  modalIconRed: { width: 62, height: 62, borderRadius: 31, backgroundColor: `${C.red}1D`, borderWidth: 1, borderColor: `${C.red}66`, alignItems: "center", justifyContent: "center" },
  modalHeadline: { color: C.white, fontSize: 17, fontWeight: "900", textAlign: "center" },
  modalBody: { color: C.muted, fontSize: 11, lineHeight: 16, textAlign: "center" },
  responseBox: { flexDirection: "row", gap: 9, width: "100%", backgroundColor: `${C.success}12`, borderWidth: 1, borderColor: `${C.success}44`, borderRadius: 10, padding: 11, alignItems: "center" },
  responseTitle: { color: C.success, fontSize: 11, fontWeight: "800" },
  responseBody: { color: C.muted, fontSize: 9.5, marginTop: 2 },
  primaryRedButton: { width: "100%", backgroundColor: C.bordo, paddingVertical: 12, borderRadius: 10, flexDirection: "row", justifyContent: "center", alignItems: "center", gap: 7 },
  secondaryButton: { width: "100%", backgroundColor: C.carbon, borderWidth: 1, borderColor: C.border, paddingVertical: 11, borderRadius: 10, alignItems: "center" },
  pixHeader: { width: "100%", flexDirection: "row", alignItems: "center", gap: 10, backgroundColor: `${C.gold}12`, borderRadius: 10, padding: 11 },
  balanceSmall: { color: C.gold, fontSize: 20, fontWeight: "900", marginTop: 2 },
  amountInput: { width: "100%", flexDirection: "row", alignItems: "center", backgroundColor: C.carbon, borderWidth: 1, borderColor: C.border, borderRadius: 10, paddingHorizontal: 12 },
  amountPrefix: { color: C.gold, fontSize: 16, fontWeight: "800" },
  amountText: { flex: 1, color: C.white, fontSize: 18, fontWeight: "800", paddingVertical: 11, paddingLeft: 7 },
  goldButtonLarge: { width: "100%", backgroundColor: C.gold, paddingVertical: 12, borderRadius: 10, flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 7 },
  bordoButtonLarge: { width: "100%", backgroundColor: C.bordo, paddingVertical: 12, borderRadius: 10, alignItems: "center" },
  qrModalBody: { alignItems: "center", gap: 11 },
  qrModalTitle: { color: C.gold, fontSize: 10, fontWeight: "800", letterSpacing: 1 },
  couponModal: { alignItems: "center", gap: 10 },
  couponSeal: { width: 68, height: 68, borderRadius: 34, backgroundColor: `${C.gold}18`, borderWidth: 1, borderColor: `${C.gold}66`, alignItems: "center", justifyContent: "center" },
  couponValue: { color: C.gold, fontSize: 18, fontWeight: "900", textAlign: "center" },
  couponCode: { width: "100%", backgroundColor: C.carbon, borderRadius: 10, padding: 11, alignItems: "center", borderWidth: 1, borderColor: C.border },
  couponCodeLabel: { color: C.muted, fontSize: 8.5, letterSpacing: 1 },
  couponCodeValue: { color: C.white, fontSize: 14, fontWeight: "900", letterSpacing: 1, marginTop: 4 },
  chatBody: { minHeight: 330 },
  chatMessages: { maxHeight: 280 },
  aiNotice: { flexDirection: "row", alignItems: "center", gap: 5, alignSelf: "center", backgroundColor: `${C.gold}12`, borderRadius: 999, paddingHorizontal: 9, paddingVertical: 5 },
  aiNoticeText: { color: C.gold, fontSize: 9 },
  chatBubble: { maxWidth: "88%", padding: 10, borderRadius: 12 },
  aiBubble: { alignSelf: "flex-start", backgroundColor: C.carbon, borderBottomLeftRadius: 3 },
  userBubble: { alignSelf: "flex-end", backgroundColor: C.bordo, borderBottomRightRadius: 3 },
  chatText: { color: C.white, fontSize: 10.5, lineHeight: 15 },
  chatComposer: { flexDirection: "row", gap: 7, alignItems: "center", marginTop: 12 },
  chatInput: { flex: 1, backgroundColor: C.carbon, borderWidth: 1, borderColor: C.border, borderRadius: 10, color: C.white, fontSize: 11, paddingHorizontal: 11, paddingVertical: 10 },
  sendButton: { width: 38, height: 38, borderRadius: 10, backgroundColor: C.bordo, alignItems: "center", justifyContent: "center" },
  sosToast: { position: "absolute", left: 16, right: 16, bottom: Platform.OS === "web" ? 76 : 84, zIndex: 20, flexDirection: "row", alignItems: "center", gap: 9, backgroundColor: "#10251CEE", borderWidth: 1, borderColor: `${C.success}88`, borderRadius: 12, padding: 12, shadowColor: C.success, shadowOpacity: 0.22, shadowRadius: 12, elevation: 8 },
  sosToastRaised: { bottom: Platform.OS === "web" ? 164 : 172 },
  sosToastError: { backgroundColor: "#32151CEE", borderColor: `${C.red}88`, shadowColor: C.red },
  sosToastTitle: { color: C.white, fontSize: 11, fontWeight: "900" },
  sosToastBody: { color: C.muted, fontSize: 9.5, marginTop: 2 },
  centralStatusCard: { position: "absolute", left: 16, right: 16, bottom: Platform.OS === "web" ? 76 : 84, zIndex: 18, flexDirection: "row", alignItems: "center", gap: 10, backgroundColor: "#10251CF5", borderWidth: 1, borderColor: `${C.success}66`, borderRadius: 14, padding: 12, shadowColor: C.success, shadowOpacity: 0.18, shadowRadius: 14, elevation: 7 },
  centralStatusIcon: { width: 34, height: 34, borderRadius: 17, backgroundColor: `${C.success}18`, alignItems: "center", justifyContent: "center" },
  centralStatusTitle: { color: C.white, fontSize: 11, fontWeight: "900" },
  centralStatusBody: { color: C.muted, fontSize: 9.5, marginTop: 2 },
  centralProgress: { height: 4, borderRadius: 2, backgroundColor: `${C.white}18`, overflow: "hidden", marginTop: 8 },
  centralProgressFill: { height: 4, borderRadius: 2, backgroundColor: C.success },
  cancelButton: { flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8, borderWidth: 1, borderColor: "#F8717166", backgroundColor: "#F8717112", borderRadius: 10, paddingVertical: 12, marginTop: 10 },
  cancelButtonText: { color: C.red, fontSize: 12, fontWeight: "900" },
  cancelModalIcon: { width: 58, height: 58, borderRadius: 29, backgroundColor: "#D4AF3718", alignItems: "center", justifyContent: "center", marginBottom: 12 },
  centralStatusActions: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginTop: 8 },
  centralStatusProtocol: { color: C.muted, fontSize: 8.5 },
  centralCancelButton: { flexDirection: "row", alignItems: "center", gap: 4, paddingHorizontal: 8, paddingVertical: 5, borderRadius: 7, borderWidth: 1, borderColor: "#F8717155" },
  centralCancelText: { color: C.red, fontSize: 9, fontWeight: "900" },
  disabledButton: { opacity: 0.6 },
  profileButton: { width: 36, height: 36, borderRadius: 18, backgroundColor: C.panel, borderWidth: 1, borderColor: "#D4AF3755", alignItems: "center", justifyContent: "center", position: "relative" },
  profileInitial: { position: "absolute", right: 2, bottom: 1, color: C.white, fontSize: 8, fontWeight: "900", backgroundColor: C.bordo, borderRadius: 6, paddingHorizontal: 3 },
  profileScroll: { maxHeight: 560 },
  profileModal: { alignItems: "stretch", paddingTop: 4 },
  profileAvatar: { width: 62, height: 62, borderRadius: 31, backgroundColor: "#D4AF3718", alignItems: "center", justifyContent: "center", alignSelf: "center", marginBottom: 10 },
  profileIntro: { color: C.muted, fontSize: 10.5, lineHeight: 15, textAlign: "center", marginBottom: 12 },
  profileInput: { height: 46, borderWidth: 1, borderColor: C.border, backgroundColor: C.carbon, borderRadius: 10, color: C.white, paddingHorizontal: 12, marginBottom: 9, fontSize: 12 },
  profileError: { color: C.red, fontSize: 10, marginBottom: 9 },
  profileSuccess: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: "#34D39918", borderRadius: 8, padding: 9, marginBottom: 10 },
  profileSuccessText: { color: C.success, fontSize: 10, fontWeight: "800" },
  centralConfig: { marginTop: 4, marginBottom: 8, paddingTop: 10, borderTopWidth: 1, borderTopColor: C.border },
  contactHeaderActions: { flexDirection: "row", alignItems: "center", gap: 8 },
  primaryContactButton: { flexDirection: "row", alignItems: "center", gap: 3, borderWidth: 1, borderColor: C.border, borderRadius: 7, paddingHorizontal: 6, paddingVertical: 4 },
  primaryContactActive: { borderColor: "#D4AF3766", backgroundColor: "#D4AF3712" },
  primaryContactText: { color: C.muted, fontSize: 8, fontWeight: "800" },
  primaryContactTextActive: { color: C.gold },
  contactsSection: { marginTop: 6, marginBottom: 8 },
  contactsHeader: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: 8 },
  contactsTitle: { color: C.gold, fontSize: 9.5, fontWeight: "900", letterSpacing: 0.8 },
  contactsHint: { color: C.muted, fontSize: 9, marginTop: 2 },
  addContactButton: { flexDirection: "row", alignItems: "center", gap: 4, borderWidth: 1, borderColor: "#D4AF3755", borderRadius: 8, paddingHorizontal: 8, paddingVertical: 6 },
  addContactText: { color: C.gold, fontSize: 9, fontWeight: "900" },
  contactCard: { backgroundColor: C.carbon, borderWidth: 1, borderColor: C.border, borderRadius: 10, padding: 9, marginBottom: 8 },
  contactCardHeader: { flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 7 },
  contactNumber: { color: C.muted, fontSize: 8.5, fontWeight: "900", letterSpacing: 0.8 },
  contactInputRow: { flexDirection: "row", gap: 7 },
  contactHalf: { flex: 1 },
  profileSync: { flexDirection: "row", alignItems: "center", gap: 6, backgroundColor: "#34D39918", borderRadius: 8, padding: 9, marginBottom: 10 },
  profileSyncText: { color: C.success, fontSize: 10, fontWeight: "800", flex: 1 },
  profileSyncError: { backgroundColor: "#F8717112" },
  profileSyncErrorText: { color: C.red, fontSize: 10, fontWeight: "800", flex: 1 },
  sosPicker: { alignItems: "stretch", gap: 10 },
  sosTypeGrid: { flexDirection: "row", flexWrap: "wrap", gap: 8, justifyContent: "center", marginTop: 4 },
  sosTypeOption: { width: "30%", minHeight: 66, alignItems: "center", justifyContent: "center", gap: 5, backgroundColor: C.panel2, borderColor: C.border, borderWidth: 1, borderRadius: 10, padding: 7 },
  sosTypeOptionActive: { borderColor: C.gold, backgroundColor: `${C.gold}18` },
  sosTypeText: { color: C.muted, fontSize: 10, fontWeight: "700", textAlign: "center" },
  sosTypeTextActive: { color: C.gold },
  priorityPreview: { flexDirection: "row", alignItems: "center", gap: 9, padding: 10, backgroundColor: `${C.gold}10`, borderColor: `${C.gold}44`, borderWidth: 1, borderRadius: 10 },
  sosPickerHint: { color: C.muted, fontSize: 10, textAlign: "center" },
  pressed: { opacity: 0.78, transform: [{ scale: 0.98 }] },
});
