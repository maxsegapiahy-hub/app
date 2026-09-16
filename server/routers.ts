import { TRPCError } from "@trpc/server";
import { z } from "zod";
import { COOKIE_NAME } from "../shared/const.js";
import { CENTRAL_MAX_PROFILE, getSosPriority, type EmergencyType } from "../shared/max-seg";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { protectedProcedure, publicProcedure, router } from "./_core/trpc";
import { cancelSosAlertForUser, createSosAlert, getActivePushTokens, getCentralProfile, getUserProfile, listActiveSosAlerts, updateSosAlertStatus, upsertCentralProfile, upsertPushToken, upsertUserProfile } from "./db";
import { sendExpoPushMessages } from "./push";

const emergencyTypeSchema = z.enum(["security", "medical", "fire", "accident", "other"]);
const alertStatusSchema = z.enum(["received", "dispatching", "enroute", "arrived", "canceled", "closed"]);

function requireAdmin(user: { role: string }) {
  if (user.role !== "admin") throw new TRPCError({ code: "FORBIDDEN", message: "Acesso restrito aos operadores da Central Max." });
}

const profileInput = z.object({
  name: z.string().min(2).max(120),
  email: z.string().email(),
  phone: z.string().min(10).max(24),
  contacts: z.array(z.object({ name: z.string().min(2).max(120), phone: z.string().min(10).max(24), relationship: z.string().min(2).max(60), isPrimary: z.boolean().optional() })).max(3),
  central: z.object({ name: z.string().min(2).max(120), phone: z.string().min(3).max(24) }),
});

const centralInput = z.object({
  name: z.string().min(2).max(120),
  city: z.string().min(2).max(120),
  phone: z.string().min(3).max(24),
  service: z.string().min(2).max(180),
  availability: z.string().min(2).max(120),
  responseTarget: z.string().min(2).max(180),
  status: z.enum(["online", "degraded", "offline"]),
  channels: z.array(z.string().min(2).max(120)).min(1).max(8),
});

export const appRouter = router({
  system: systemRouter,
  central: router({
    profile: publicProcedure.query(() => getCentralProfile()),
    admin: router({
      get: protectedProcedure.query(async ({ ctx }) => { requireAdmin(ctx.user); return getCentralProfile(); }),
      update: protectedProcedure.input(centralInput).mutation(async ({ ctx, input }) => { requireAdmin(ctx.user); return upsertCentralProfile(ctx.user.id, input); }),
      alerts: router({
        list: protectedProcedure.query(async ({ ctx }) => { requireAdmin(ctx.user); return listActiveSosAlerts(); }),
        updateStatus: protectedProcedure.input(z.object({ alertId: z.string().min(1).max(64), status: alertStatusSchema })).mutation(async ({ ctx, input }) => { requireAdmin(ctx.user); return updateSosAlertStatus(input.alertId, input.status); }),
      }),
    }),
  }),
  auth: router({
    me: publicProcedure.query((opts) => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => { const cookieOptions = getSessionCookieOptions(ctx.req); ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 }); return { success: true } as const; }),
  }),
  push: router({
    register: protectedProcedure.input(z.object({ token: z.string().min(20).max(512), platform: z.enum(["ios", "android"]) })).mutation(async ({ ctx, input }) => { await upsertPushToken(ctx.user.id, input.token, input.platform); return { status: "registered" as const }; }),
  }),
  sos: router({
    send: protectedProcedure.input(z.object({
      latitude: z.number().finite().nullable(), longitude: z.number().finite().nullable(), accuracy: z.number().finite().nonnegative().nullable(), platform: z.string().min(1).max(32),
      emergencyType: emergencyTypeSchema.default("other"),
      contacts: z.array(z.object({ name: z.string().min(2).max(120), phone: z.string().min(10).max(24), relationship: z.string().min(2).max(60), isPrimary: z.boolean().optional() })).max(3).default([]),
    })).mutation(async ({ ctx, input }) => {
      const receivedAt = new Date();
      const alertId = `MAX-SOS-${receivedAt.getTime()}`;
      const priority = getSosPriority(input.emergencyType as EmergencyType);
      const tokens = await getActivePushTokens(ctx.user.id);
      let pushSent = 0;
      let pushFailed = 0;
      try {
        const result = await sendExpoPushMessages(tokens.map(({ token }) => ({ to: token, title: `${priority.label} · SOS Max recebido`, body: `${ctx.user.name ?? "Seu contato"} acionou a Central Max. ${priority.explanation}`, data: { type: "sos", priority: priority.priority, emergencyType: input.emergencyType, latitude: input.latitude, longitude: input.longitude, alertId } })));
        pushSent = result.sent;
        pushFailed = result.failed;
      } catch (error) { console.warn("[SOS] Push delivery failed:", error); pushFailed = tokens.length; }
      await createSosAlert({ alertId, userId: ctx.user.id, emergencyType: input.emergencyType, priority: priority.priority, latitude: input.latitude, longitude: input.longitude, accuracy: input.accuracy, contactsQueued: input.contacts.length, pushSent });
      const central = await getCentralProfile();
      return { alertId, status: "received" as const, receivedAt, emergencyType: input.emergencyType, priority: priority.priority, priorityLabel: priority.label, priorityExplanation: priority.explanation, locationAttached: input.latitude !== null && input.longitude !== null, location: input.latitude !== null && input.longitude !== null ? { latitude: input.latitude, longitude: input.longitude, accuracy: input.accuracy } : null, emergencyContactsNotified: 0, emergencyContactsQueued: input.contacts.length, pushSent, pushFailed, central, message: "Alerta autenticado e recebido pela Central Max Apiahy." };
    }),
    cancel: protectedProcedure.input(z.object({ alertId: z.string().min(1).max(64) })).mutation(async ({ ctx, input }) => { await cancelSosAlertForUser(input.alertId, ctx.user.id); return { alertId: input.alertId, status: "canceled" as const, canceledAt: new Date(), message: "Alerta cancelado pela pessoa titular." }; }),
  }),
  profile: router({
    get: protectedProcedure.query(({ ctx }) => getUserProfile(ctx.user.id)),
    sync: protectedProcedure.input(profileInput).mutation(async ({ ctx, input }) => { const profileId = await upsertUserProfile(ctx.user.id, input); return { profileId: `MAX-PERFIL-${profileId}`, status: "synced" as const, contactCount: input.contacts.length, primaryContact: input.contacts.find((contact) => contact.isPrimary)?.name ?? null, notificationsReady: input.contacts.length > 0, message: "Perfil autenticado e sincronizado com a Central Max Apiahy." }; }),
  }),
});

export type AppRouter = typeof appRouter;
