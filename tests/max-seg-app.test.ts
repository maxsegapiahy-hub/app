import { describe, expect, it } from "vitest";

import { filterByCategory, formatSosCoordinates, getMaxAiReply, isSosHoldComplete } from "../shared/max-seg";

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

  it("responde temas conhecidos com orientação contextual", () => {
    expect(getMaxAiReply("Quais descontos eu tenho?")).toContain("Clube Apiaí");
    expect(getMaxAiReply("Preciso falar com um médico")).toContain("Telemedicina 24h");
    expect(getMaxAiReply("Como aciono o SOS?")).toContain("2 segundos");
  });
});
