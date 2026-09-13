import { describe, expect, it } from "vitest";

import { appRouter } from "../server/routers";
import type { TrpcContext } from "../server/_core/context";

const context: TrpcContext = {
  user: null,
  req: { protocol: "https", headers: {} } as TrpcContext["req"],
  res: {} as TrpcContext["res"],
};

describe("sos.send", () => {
  it("recebe alerta e coordenadas GPS na API simulada", async () => {
    const result = await appRouter.createCaller(context).sos.send({
      latitude: -24.51235,
      longitude: -48.8422,
      accuracy: 8,
      platform: "ios",
    });

    expect(result.status).toBe("received");
    expect(result.alertId).toMatch(/^MAX-SOS-/);
    expect(result.locationAttached).toBe(true);
    expect(result.location).toEqual({ latitude: -24.51235, longitude: -48.8422, accuracy: 8 });
  });

  it("aceita alerta mesmo quando a localização não está disponível", async () => {
    const result = await appRouter.createCaller(context).sos.send({
      latitude: null,
      longitude: null,
      accuracy: null,
      platform: "web",
    });

    expect(result.status).toBe("received");
    expect(result.locationAttached).toBe(false);
    expect(result.location).toBeNull();
  });

  it("cancela um alerta antes da chegada da equipe", async () => {
    const result = await appRouter.createCaller(context).sos.cancel({ alertId: "MAX-SOS-123" });

    expect(result.status).toBe("canceled");
    expect(result.alertId).toBe("MAX-SOS-123");
  });
});
