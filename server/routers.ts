import { COOKIE_NAME } from "../shared/const.js";
import { getSessionCookieOptions } from "./_core/cookies";
import { systemRouter } from "./_core/systemRouter";
import { publicProcedure, router } from "./_core/trpc";
import { z } from "zod";

export const appRouter = router({
  // if you need to use socket.io, read and register route in server/_core/index.ts, all api should start with '/api/' so that the gateway can route correctly
  system: systemRouter,
  auth: router({
    me: publicProcedure.query((opts) => opts.ctx.user),
    logout: publicProcedure.mutation(({ ctx }) => {
      const cookieOptions = getSessionCookieOptions(ctx.req);
      ctx.res.clearCookie(COOKIE_NAME, { ...cookieOptions, maxAge: -1 });
      return {
        success: true,
      } as const;
    }),
  }),

  sos: router({
    send: publicProcedure
      .input(z.object({
        latitude: z.number().finite().nullable(),
        longitude: z.number().finite().nullable(),
        accuracy: z.number().finite().nonnegative().nullable(),
        platform: z.string().min(1).max(32),
        contacts: z.array(z.object({ name: z.string().min(2).max(120), phone: z.string().min(10).max(24), relationship: z.string().min(2).max(60) })).max(3).default([]),
      }))
      .mutation(async ({ input }) => {
        await new Promise((resolve) => setTimeout(resolve, 250));
        const receivedAt = new Date();
        return {
          alertId: `MAX-SOS-${receivedAt.getTime()}`,
          status: "received" as const,
          receivedAt,
          locationAttached: input.latitude !== null && input.longitude !== null,
          location: input.latitude !== null && input.longitude !== null
            ? { latitude: input.latitude, longitude: input.longitude, accuracy: input.accuracy }
            : null,
          emergencyContactsNotified: input.contacts.length,
          message: "Alerta recebido pela Central Max Apiahy.",
        };
      }),
    cancel: publicProcedure
      .input(z.object({ alertId: z.string().min(1).max(64) }))
      .mutation(async ({ input }) => {
        await new Promise((resolve) => setTimeout(resolve, 180));
        return {
          alertId: input.alertId,
          status: "canceled" as const,
          canceledAt: new Date(),
          message: "Alerta cancelado pela pessoa titular.",
        };
      }),
  }),

  profile: router({
    sync: publicProcedure
      .input(z.object({
        name: z.string().min(2).max(120),
        email: z.string().email(),
        phone: z.string().min(10).max(24),
        contacts: z.array(z.object({
          name: z.string().min(2).max(120),
          phone: z.string().min(10).max(24),
          relationship: z.string().min(2).max(60),
        })).max(3),
      }))
      .mutation(async ({ input }) => {
        await new Promise((resolve) => setTimeout(resolve, 220));
        return {
          profileId: `MAX-PERFIL-${Date.now()}`,
          status: "synced" as const,
          contactCount: input.contacts.length,
          notificationsReady: input.contacts.length > 0,
          message: "Perfil sincronizado com a Central Max Apiahy.",
        };
      }),
  }),

  // TODO: add feature routers here, e.g.
  // todo: router({
  //   list: protectedProcedure.query(({ ctx }) =>
  //     db.getUserTodos(ctx.user.id)
  //   ),
  // }),
});

export type AppRouter = typeof appRouter;
