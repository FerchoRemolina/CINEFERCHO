import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Timer } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";
import { formatCountdown } from "../utils/format.js";

export default function SessionCountdown() {
  const navigate = useNavigate();
  const { isClient, user, expireSession } = useAuth();
  const [remainingMs, setRemainingMs] = useState(null);

  useEffect(() => {
    if (!isClient || !user?.sessionExpiresAt) {
      setRemainingMs(null);
      return undefined;
    }
    const expiresAt = new Date(user.sessionExpiresAt).getTime();
    const tick = () => {
      const left = expiresAt - Date.now();
      setRemainingMs(left);
      if (left <= 0) {
        expireSession("timeout");
        navigate("/");
      }
    };
    tick();
    const id = window.setInterval(tick, 250);
    return () => window.clearInterval(id);
  }, [isClient, user?.sessionExpiresAt, expireSession, navigate]);

  if (!isClient || remainingMs == null || remainingMs <= 0) {
    return null;
  }

  const seconds = Math.ceil(remainingMs / 1000);
  const urgent = seconds <= 30;

  return (
    <div
      className={`border-t px-4 py-2 text-center text-sm font-medium ${
        urgent
          ? "border-rose-500/40 bg-rose-600 text-white"
          : "border-gold/20 bg-gold/15 text-gold"
      }`}
    >
      <span className="inline-flex items-center gap-2">
        <Timer className="h-4 w-4" />
        Tiempo para completar tus compras:{" "}
        <span className="font-mono text-base tracking-widest">{formatCountdown(seconds)}</span>
      </span>
      <span className="ml-2 hidden sm:inline text-xs opacity-80">
        Si se agota, se cancelará todo y se cerrará tu sesión.
      </span>
    </div>
  );
}
