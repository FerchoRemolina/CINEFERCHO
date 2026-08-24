export function formatCOP(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatDateTime(value) {
  if (!value) return "";
  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function formatDate(value) {
  if (!value) return "";
  const parsed = /^\d{4}-\d{2}-\d{2}$/.test(value) ? new Date(`${value}T00:00:00`) : new Date(value);
  return new Intl.DateTimeFormat("es-CO", { dateStyle: "long" }).format(parsed);
}

export function formatTime(value) {
  if (!value) return "";
  return new Intl.DateTimeFormat("es-CO", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}

export function toIsoDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function nextDays(count = 7) {
  const days = [];
  const now = new Date();
  for (let i = 0; i < count; i += 1) {
    const date = new Date(now);
    date.setDate(now.getDate() + i);
    days.push(date);
  }
  return days;
}

export function membershipDiscountRate(membershipType) {
  if (membershipType === "PRO") return 0.2;
  if (membershipType === "GOLD") return 0.1;
  return 0;
}

export function membershipLabel(membershipType) {
  if (membershipType === "PRO") return "PRO";
  if (membershipType === "GOLD") return "GOLD";
  return "Sin membresía";
}

export function isMembershipActive(user) {
  if (!user) return false;
  if (user.membershipType !== "GOLD" && user.membershipType !== "PRO") return false;
  if (user.membershipExpiresAt) {
    return new Date(user.membershipExpiresAt).getTime() > Date.now();
  }
  return Boolean(user.membershipActive);
}

export function formatCountdown(totalSeconds) {
  const safe = Math.max(0, Math.floor(totalSeconds));
  const minutes = String(Math.floor(safe / 60)).padStart(2, "0");
  const seconds = String(safe % 60).padStart(2, "0");
  return `${minutes}:${seconds}`;
}

export function readJwtExpiresAt(token) {
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return payload.exp ? new Date(payload.exp * 1000).toISOString() : null;
  } catch {
    return null;
  }
}
