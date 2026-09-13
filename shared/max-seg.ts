export function filterByCategory<T extends { category: string }>(items: T[], category: string): T[] {
  return category === "todos" ? items : items.filter((item) => item.category === category);
}

export function isSosHoldComplete(startedAt: number, releasedAt: number, thresholdMs = 2000): boolean {
  return releasedAt - startedAt >= thresholdMs;
}

export function formatSosCoordinates(latitude: number, longitude: number): string {
  return `${latitude.toFixed(5)}, ${longitude.toFixed(5)}`;
}

export const SOS_MAX_ATTEMPTS = 3;

export function getSosRetryDelayMs(attempt: number): number {
  return Math.min(800 * 2 ** Math.max(attempt - 1, 0), 3200);
}

export type EmergencyContact = { name: string; phone: string; relationship: string };
export type UserProfileInput = { name: string; email: string; phone: string; contacts?: EmergencyContact[] };

export function validateEmergencyContact(contact: EmergencyContact): string | null {
  if (contact.name.trim().length < 2) return "Informe o nome do contato de emergência.";
  if (contact.phone.replace(/\D/g, "").length < 10) return "Informe um telefone válido para o contato.";
  if (contact.relationship.trim().length < 2) return "Informe o parentesco ou relação.";
  return null;
}

export function validateUserProfile(profile: UserProfileInput): string | null {
  if (profile.name.trim().length < 2) return "Informe seu nome completo.";
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profile.email.trim())) return "Informe um e-mail válido.";
  if (profile.phone.replace(/\D/g, "").length < 10) return "Informe um telefone válido.";
  for (const contact of profile.contacts ?? []) {
    const contactError = validateEmergencyContact(contact);
    if (contactError) return contactError;
  }
  return null;
}

export function getMaxAiReply(message: string): string {
  const normalized = message.toLowerCase();
  if (normalized.includes("desconto") || normalized.includes("clube") || normalized.includes("parceiro")) {
    return "Posso mostrar o Clube Apiaí com os parceiros e descontos ativos. Também é possível validar o benefício com o QR Code da sua carteira.";
  }
  if (normalized.includes("médico") || normalized.includes("saúde") || normalized.includes("consulta")) {
    return "O benefício Telemedicina 24h está disponível. Posso encaminhar você para um médico Max agora, sem fila.";
  }
  if (normalized.includes("sos") || normalized.includes("emergência")) {
    return "Em uma emergência, mantenha o botão SOS pressionado por 2 segundos. A Central Max recebe sua localização e aciona a pronta resposta.";
  }
  return "Entendi. Para uma orientação personalizada, posso encaminhar você para a Central Max 24h. Também posso mostrar seus benefícios ativos ou a rede de parceiros em Apiaí.";
}
