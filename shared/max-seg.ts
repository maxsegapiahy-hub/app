export function filterByCategory<T extends { category: string }>(items: T[], category: string): T[] {
  return category === "todos" ? items : items.filter((item) => item.category === category);
}

export function isSosHoldComplete(startedAt: number, releasedAt: number, thresholdMs = 2000): boolean {
  return releasedAt - startedAt >= thresholdMs;
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
