import { and, desc, eq, ne } from "drizzle-orm";
import { drizzle } from "drizzle-orm/mysql2";
import { centralSettings, InsertUser, pushTokens, sosAlerts, userProfiles, users } from "../drizzle/schema";
import type { EmergencyContact } from "../shared/max-seg";
import { CENTRAL_MAX_PROFILE } from "../shared/max-seg";
import { ENV } from "./_core/env";

let _db: ReturnType<typeof drizzle> | null = null;

export async function getDb() {
  if (!_db && process.env.DATABASE_URL) {
    try { _db = drizzle(process.env.DATABASE_URL); } catch (error) { console.warn("[Database] Failed to connect:", error); _db = null; }
  }
  return _db;
}

export async function upsertUser(user: InsertUser): Promise<void> {
  if (!user.openId) throw new Error("User openId is required for upsert");
  const db = await getDb();
  if (!db) { console.warn("[Database] Cannot upsert user: database not available"); return; }
  const values: InsertUser = { openId: user.openId };
  const updateSet: Record<string, unknown> = {};
  const textFields = ["name", "email", "loginMethod"] as const;
  for (const field of textFields) {
    if (user[field] !== undefined) { values[field] = user[field] ?? null; updateSet[field] = user[field] ?? null; }
  }
  values.lastSignedIn = user.lastSignedIn ?? new Date();
  updateSet.lastSignedIn = values.lastSignedIn;
  if (user.role !== undefined) { values.role = user.role; updateSet.role = user.role; }
  else if (user.openId === ENV.ownerOpenId) { values.role = "admin"; updateSet.role = "admin"; }
  await db.insert(users).values(values).onDuplicateKeyUpdate({ set: updateSet });
}

export async function getUserByOpenId(openId: string) {
  const db = await getDb();
  if (!db) return undefined;
  const result = await db.select().from(users).where(eq(users.openId, openId)).limit(1);
  return result[0];
}

export type ProfileRecord = { name: string; email: string; phone: string; central: { name: string; phone: string }; contacts: EmergencyContact[] };

export async function getUserProfile(userId: number) {
  const db = await getDb();
  if (!db) return null;
  const result = await db.select().from(userProfiles).where(eq(userProfiles.userId, userId)).limit(1);
  const row = result[0];
  if (!row) return null;
  let contacts: EmergencyContact[] = [];
  try { contacts = JSON.parse(row.contactsJson) as EmergencyContact[]; } catch { contacts = []; }
  return { profileId: row.id, name: row.name, email: row.email, phone: row.phone, central: { name: row.centralName, phone: row.centralPhone }, contacts };
}

export async function upsertUserProfile(userId: number, input: ProfileRecord) {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  const values = { userId, name: input.name, email: input.email, phone: input.phone, centralName: input.central.name, centralPhone: input.central.phone, contactsJson: JSON.stringify(input.contacts) };
  await db.insert(userProfiles).values(values).onDuplicateKeyUpdate({ set: values });
  const row = await db.select({ id: userProfiles.id }).from(userProfiles).where(eq(userProfiles.userId, userId)).limit(1);
  return row[0]?.id ?? userId;
}

export async function upsertPushToken(userId: number, token: string, platform: string) {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  await db.insert(pushTokens).values({ userId, token, platform, enabled: true }).onDuplicateKeyUpdate({ set: { userId, token, platform, enabled: true } });
}

export async function getActivePushTokens(userId: number) {
  const db = await getDb();
  if (!db) return [];
  return db.select({ token: pushTokens.token }).from(pushTokens).where(and(eq(pushTokens.userId, userId), eq(pushTokens.enabled, true)));
}

export type CentralProfileRecord = {
  id: number | string;
  centralId: string;
  name: string;
  city: string;
  phone: string;
  service: string;
  availability: string;
  responseTarget: string;
  status: "online" | "degraded" | "offline";
  channels: string[];
  updatedAt?: Date;
};

export async function getCentralProfile(): Promise<CentralProfileRecord> {
  const db = await getDb();
  if (!db) return { ...CENTRAL_MAX_PROFILE, centralId: CENTRAL_MAX_PROFILE.id, channels: [...CENTRAL_MAX_PROFILE.channels] };
  const rows = await db.select().from(centralSettings).limit(1);
  const row = rows[0];
  if (!row) return { ...CENTRAL_MAX_PROFILE, centralId: CENTRAL_MAX_PROFILE.id, channels: [...CENTRAL_MAX_PROFILE.channels] };
  let channels: string[] = [];
  try { channels = JSON.parse(row.channelsJson) as string[]; } catch { channels = [...CENTRAL_MAX_PROFILE.channels]; }
  return { id: row.id, centralId: row.centralId, name: row.name, city: row.city, phone: row.phone, service: row.service, availability: row.availability, responseTarget: row.responseTarget, status: row.status, channels, updatedAt: row.updatedAt };
}

export async function upsertCentralProfile(userId: number, input: Omit<CentralProfileRecord, "id" | "centralId" | "updatedAt">) {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  const values = { centralId: CENTRAL_MAX_PROFILE.id, name: input.name, city: input.city, phone: input.phone, service: input.service, availability: input.availability, responseTarget: input.responseTarget, status: input.status, channelsJson: JSON.stringify(input.channels), updatedBy: userId };
  await db.insert(centralSettings).values(values).onDuplicateKeyUpdate({ set: values });
  return getCentralProfile();
}

export async function createSosAlert(input: { alertId: string; userId: number; emergencyType: "security" | "medical" | "fire" | "accident" | "other"; priority: "low" | "medium" | "high" | "critical"; latitude: number | null; longitude: number | null; accuracy: number | null; contactsQueued: number; pushSent: number }) {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  await db.insert(sosAlerts).values(input);
  return input.alertId;
}

export async function listActiveSosAlerts() {
  const db = await getDb();
  if (!db) return [];
  return db.select({ alert: sosAlerts, userName: users.name, userEmail: users.email }).from(sosAlerts).leftJoin(users, eq(users.id, sosAlerts.userId)).where(and(ne(sosAlerts.status, "canceled"), ne(sosAlerts.status, "closed"))).orderBy(desc(sosAlerts.createdAt)).limit(50);
}

export async function updateSosAlertStatus(alertId: string, status: "received" | "dispatching" | "enroute" | "arrived" | "canceled" | "closed") {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  await db.update(sosAlerts).set({ status }).where(eq(sosAlerts.alertId, alertId));
  const rows = await db.select().from(sosAlerts).where(eq(sosAlerts.alertId, alertId)).limit(1);
  return rows[0] ?? null;
}

export async function cancelSosAlertForUser(alertId: string, userId: number) {
  const db = await getDb();
  if (!db) throw new Error("Database not available");
  await db.update(sosAlerts).set({ status: "canceled" }).where(and(eq(sosAlerts.alertId, alertId), eq(sosAlerts.userId, userId)));
  return { alertId, status: "canceled" as const };
}
