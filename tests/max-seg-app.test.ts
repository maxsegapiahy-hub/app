import { describe, expect, it } from "vitest";

import { CENTRAL_MAX_PROFILE, filterByCategory, formatSosCoordinates, getMaxAiReply, getSosRetryDelayMs, isSosHoldComplete, SOS_MAX_ATTEMPTS, validateEmergencyContact, validateUserProfile } from "../shared/max-seg";

describe("Max Seg & Max Saúde", () => {
  it("filtra parceiros do Clube por categoria e preserva todos", () => {
    const partners = [
      { name: "Drogaria Central", category: "farmacia" },
      { name: "Posto Apiaí", category: "posto" },
    ];

    expect(filterByCategory(partners, "farmacia")).toEqual([partners[0]]);
    expect(filterByCategory(partners, "todos")).toEqual(partners);
  });

  it("só completa o SOS depois de dois segundos de pressão", () => {
    expect(isSosHoldComplete(1000, 2999)).toBe(false);
    expect(isSosHoldComplete(1000, 3000)).toBe(true);
    expect(isSosHoldComplete(1000, 2500, 1000)).toBe(true);
  });

  it("formata as coordenadas enviadas para a central", () => {
    expect(formatSosCoordinates(-24.5123456, -48.8421987)).toBe("-24.51235, -48.84220");
  });

  it("calcula backoff progressivo para até três tentativas", () => {
    expect(SOS_MAX_ATTEMPTS).toBe(3);
    expect(getSosRetryDelayMs(1)).toBe(800);
    expect(getSosRetryDelayMs(2)).toBe(1600);
    expect(getSosRetryDelayMs(3)).toBe(3200);
  });

  it("responde temas conhecidos com orientação contextual", () => {
    expect(getMaxAiReply("Quais descontos eu tenho?")).toContain("Clube Apiaí");
    expect(getMaxAiReply("Preciso falar com um médico")).toContain("Telemedicina 24h");
    expect(getMaxAiReply("Como aciono o SOS?")).toContain("2 segundos");
  });

  it("mantém o perfil operacional da Central Max Apiahy", () => {
    expect(CENTRAL_MAX_PROFILE.name).toBe("Central Max Apiahy");
    expect(CENTRAL_MAX_PROFILE.phone).toBe("153");
    expect(CENTRAL_MAX_PROFILE.status).toBe("online");
    expect(CENTRAL_MAX_PROFILE.channels).toContain("SOS com localização GPS");
  });

  it("valida nome, e-mail e telefone do cadastro", () => {
    expect(validateUserProfile({ name: "", email: "carlos@email.com", phone: "15999999999" })).toBe("Informe seu nome completo.");
    expect(validateUserProfile({ name: "Carlos", email: "email-invalido", phone: "15999999999" })).toBe("Informe um e-mail válido.");
    expect(validateUserProfile({ name: "Carlos", email: "carlos@email.com", phone: "123" })).toBe("Informe um telefone válido.");
    expect(validateUserProfile({ name: "Carlos Silva", email: "carlos@email.com", phone: "(15) 99999-9999" })).toBeNull();
    expect(validateUserProfile({ name: "Carlos Silva", email: "carlos@email.com", phone: "(15) 99999-9999", contacts: [{ name: "Ana", phone: "123", relationship: "Irmã" }] })).toContain("telefone");
    expect(validateEmergencyContact({ name: "Ana Silva", phone: "(15) 98888-7777", relationship: "Irmã" })).toBeNull();
    expect(validateUserProfile({ name: "Carlos Silva", email: "carlos@email.com", phone: "(15) 99999-9999", contacts: [{ name: "Ana Silva", phone: "15988887777", relationship: "Irmã", isPrimary: true }, { name: "João Silva", phone: "15977776666", relationship: "Pai", isPrimary: true }] })).toContain("apenas um");
    expect(validateUserProfile({ name: "Carlos Silva", email: "carlos@email.com", phone: "(15) 99999-9999", central: { name: "Central Max Apiahy", phone: "153" } })).toBeNull();
    expect(validateUserProfile({ name: "Carlos Silva", email: "carlos@email.com", phone: "(15) 99999-9999", contacts: [{ name: "Ana Silva", phone: "15988887777", relationship: "Irmã", isPrimary: true }, { name: "João Silva", phone: "15977776666", relationship: "Pai" }], central: { name: "Central Max Apiahy", phone: "153" } })).toBeNull();
  });
});
