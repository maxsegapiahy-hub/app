import { eq } from "drizzle-orm";
import { drizzle } from "drizzle-orm/mysql2";
import { InsertUser, pushTokens, userProfiles, users } from "../drizzle/schema";
import type { EmergencyContact } from "../shared/max-seg";
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
  return db.select({ token: pushTokens.token }).from(pushTokens).where(eq(pushTokens.userId, userId));
}
