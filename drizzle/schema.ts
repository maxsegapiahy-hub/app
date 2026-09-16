import { boolean, double, int, mysqlEnum, mysqlTable, text, timestamp, unique, varchar } from "drizzle-orm/mysql-core";

export const users = mysqlTable("users", {
  id: int("id").autoincrement().primaryKey(),
  openId: varchar("openId", { length: 64 }).notNull().unique(),
  name: text("name"),
  email: varchar("email", { length: 320 }),
  loginMethod: varchar("loginMethod", { length: 64 }),
  role: mysqlEnum("role", ["user", "admin"]).default("user").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
  lastSignedIn: timestamp("lastSignedIn").defaultNow().notNull(),
});

export const userProfiles = mysqlTable("user_profiles", {
  id: int("id").autoincrement().primaryKey(),
  userId: int("userId").notNull().unique(),
  name: varchar("name", { length: 120 }).notNull(),
  email: varchar("email", { length: 320 }).notNull(),
  phone: varchar("phone", { length: 24 }).notNull(),
  centralName: varchar("centralName", { length: 120 }).notNull(),
  centralPhone: varchar("centralPhone", { length: 24 }).notNull(),
  contactsJson: text("contactsJson").notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export const pushTokens = mysqlTable("push_tokens", {
  id: int("id").autoincrement().primaryKey(),
  userId: int("userId").notNull(),
  token: varchar("token", { length: 512 }).notNull().unique(),
  platform: varchar("platform", { length: 16 }).notNull(),
  enabled: boolean("enabled").default(true).notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
}, (table) => ({ userPlatform: unique("push_tokens_user_platform").on(table.userId, table.platform) }));

export const centralSettings = mysqlTable("central_settings", {
  id: int("id").autoincrement().primaryKey(),
  centralId: varchar("centralId", { length: 80 }).notNull().unique(),
  name: varchar("name", { length: 120 }).notNull(),
  city: varchar("city", { length: 120 }).notNull(),
  phone: varchar("phone", { length: 24 }).notNull(),
  service: varchar("service", { length: 180 }).notNull(),
  availability: varchar("availability", { length: 120 }).notNull(),
  responseTarget: varchar("responseTarget", { length: 180 }).notNull(),
  status: mysqlEnum("status", ["online", "degraded", "offline"]).default("online").notNull(),
  channelsJson: text("channelsJson").notNull(),
  updatedBy: int("updatedBy").notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export const sosAlerts = mysqlTable("sos_alerts", {
  id: int("id").autoincrement().primaryKey(),
  alertId: varchar("alertId", { length: 64 }).notNull().unique(),
  userId: int("userId").notNull(),
  emergencyType: mysqlEnum("emergencyType", ["security", "medical", "fire", "accident", "other"]).notNull(),
  priority: mysqlEnum("priority", ["low", "medium", "high", "critical"]).notNull(),
  status: mysqlEnum("status", ["received", "dispatching", "enroute", "arrived", "canceled", "closed"]).default("received").notNull(),
  latitude: double("latitude"),
  longitude: double("longitude"),
  accuracy: double("accuracy"),
  contactsQueued: int("contactsQueued").default(0).notNull(),
  pushSent: int("pushSent").default(0).notNull(),
  createdAt: timestamp("createdAt").defaultNow().notNull(),
  updatedAt: timestamp("updatedAt").defaultNow().onUpdateNow().notNull(),
});

export type User = typeof users.$inferSelect;
export type InsertUser = typeof users.$inferInsert;
export type UserProfile = typeof userProfiles.$inferSelect;
export type PushToken = typeof pushTokens.$inferSelect;
export type CentralSettings = typeof centralSettings.$inferSelect;
export type SosAlert = typeof sosAlerts.$inferSelect;
