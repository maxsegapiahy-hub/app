export type PushMessage = { to: string; title: string; body: string; data?: Record<string, unknown> };

export async function sendExpoPushMessages(messages: PushMessage[]) {
  if (messages.length === 0) return { sent: 0, failed: 0 };
  const response = await fetch("https://exp.host/--/api/v2/push/send", {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify(messages),
  });
  if (!response.ok) throw new Error(`Expo Push Service returned ${response.status}`);
  const payload = await response.json() as { data?: Array<{ status?: string }> };
  const results = payload.data ?? [];
  return { sent: results.filter((item) => item.status === "ok").length, failed: results.filter((item) => item.status !== "ok").length };
}
