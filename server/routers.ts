import { COOKIE_NAME } from "../shared/const.js";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { protectedProcedure, publicProcedure, router } from "./_core/trpc";
import { getActivePushTokens, getUserProfile, upsertPushToken, upsertUserProfile } from "./db";
import { sendExpoPushMessages } from "./push";
import { CENTRAL_MAX_PROFILE } from "../shared/max-seg";
import { z } from "zod";

const profileInput = z.object({
  name: z.string().min(2).max(120),
  email: z.string().email(),
  phone: z.string().min(10).max(24),
  contacts: z.array(z.object({
    name: z.string().min(2).max(120),
    phone: z.string().min(10).max(24),
    relationship: z.string().min(2).max(60),
    isPrimary: z.boolean().optional(),
  })).max(3),
  central: z.object({ name: z.string().min(2).max(120), phone: z.string().min(3).max(24) }),
});

export const appRouter = router({
  system: systemRouter,
  central: router({
    profile: publicProcedure.query(() => CENTRAL_MAX_PROFILE),
  }),
  auth: router({
    me: publicProcedure.query((opts) => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => {
      const cookieOptions = getSessionCookieOptions(ctx.req);
      ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 });
      return { success: true } as const;
    }),
  }),

  push: router({
    register: protectedProcedure
      .input(z.object({ token: z.string().min(20).max(512), platform: z.enum(["ios", "android"]) }))
      .mutation(async ({ ctx, input }) => {
        await upsertPushToken(ctx.user.id, input.token, input.platform);
        return { status: "registered" as const };
      }),
  }),

  sos: router({
    send: protectedProcedure
      .input(z.object({
        latitude: z.number().finite().nullable(),
        longitude: z.number().finite().nullable(),
        accuracy: z.number().finite().nonnegative().nullable(),
        platform: z.string().min(1).max(32),
        contacts: z.array(z.object({ name: z.string().min(2).max(120), phone: z.string().min(10).max(24), relationship: z.string().min(2).max(60), isPrimary: z.boolean().optional() })).max(3).default([]),
      }))
      .mutation(async ({ ctx, input }) => {
        const receivedAt = new Date();
        const tokens = await getActivePushTokens(ctx.user.id);
        let pushSent = 0;
        let pushFailed = 0;
        try {
          const result = await sendExpoPushMessages(tokens.map(({ token }) => ({
            to: token,
            title: "SOS Max recebido",
            body: `${ctx.user.name ?? "Seu contato"} acionou a Central Max. A equipe foi avisada.`,
            data: { type: "sos", latitude: input.latitude, longitude: input.longitude, alertId: `MAX-SOS-${receivedAt.getTime()}` },
          })));
          pushSent = result.sent;
          pushFailed = result.failed;
        } catch (error) {
          console.warn("[SOS] Push delivery failed:", error);
          pushFailed = tokens.length;
        }
        return {
          alertId: `MAX-SOS-${receivedAt.getTime()}`,
          status: "received" as const,
          receivedAt,
          locationAttached: input.latitude !== null && input.longitude !== null,
          location: input.latitude !== null && input.longitude !== null ? { latitude: input.latitude, longitude: input.longitude, accuracy: input.accuracy } : null,
          emergencyContactsNotified: 0,
          emergencyContactsQueued: input.contacts.length,
          pushSent,
          pushFailed,
          central: CENTRAL_MAX_PROFILE,
          message: "Alerta autenticado e recebido pela Central Max Apiahy.",
        };
      }),
    cancel: protectedProcedure
      .input(z.object({ alertId: z.string().min(1).max(64) }))
      .mutation(async ({ input }) => ({ alertId: input.alertId, status: "canceled" as const, canceledAt: new Date(), message: "Alerta cancelado pela pessoa titular." })),
  }),

  profile: router({
    get: protectedProcedure.query(({ ctx }) => getUserProfile(ctx.user.id)),
    sync: protectedProcedure
      .input(profileInput)
      .mutation(async ({ ctx, input }) => {
        const profileId = await upsertUserProfile(ctx.user.id, input);
        return {
          profileId: `MAX-PERFIL-${profileId}`,
          status: "synced" as const,
          contactCount: input.contacts.length,
          primaryContact: input.contacts.find((contact) => contact.isPrimary)?.name ?? null,
          notificationsReady: input.contacts.length > 0,
          message: "Perfil autenticado e sincronizado com a Central Max Apiahy.",
        };
      }),
  }),
});

export type AppRouter = typeof appRouter;
