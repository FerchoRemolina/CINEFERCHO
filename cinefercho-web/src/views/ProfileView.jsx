import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { getApiError } from "../services/api.js";
import { formatDate, membershipLabel } from "../utils/format.js";

export default function ProfileView() {
  const { user, updateProfile, isClient } = useAuth();
  const [fullName, setFullName] = useState(user?.fullName || "");
  const [nationalId, setNationalId] = useState(user?.nationalId || "");
  const [phone, setPhone] = useState(user?.phone || "");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setFullName(user?.fullName || "");
    setNationalId(user?.nationalId || "");
    setPhone(user?.phone || "");
  }, [user]);

  if (!isClient) {
    return <p className="pt-16 text-center text-zinc-400">Inicia sesión como cliente para editar tu cuenta.</p>;
  }

  const onSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      await updateProfile({
        fullName,
        nationalId: nationalId.replace(/\D/g, ""),
        phone: phone.replace(/\D/g, ""),
      });
      setMessage("Tus datos se actualizaron correctamente.");
    } catch (err) {
      setError(getApiError(err, "No se pudo actualizar el perfil."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="mx-auto max-w-xl space-y-6 pt-10">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Perfil</p>
        <h1 className="font-display text-4xl">Mi cuenta</h1>
        <p className="text-zinc-400">Actualiza tu nombre, teléfono y cédula.</p>
      </div>
      {user.membershipType && user.membershipType !== "NONE" && (
        <div className="rounded-2xl border border-gold/30 bg-gold/10 p-5">
          <p className="text-sm uppercase tracking-[0.2em] text-gold">Membresía</p>
          <p className="mt-1 text-xl font-semibold">{membershipLabel(user.membershipType)}</p>
          <p className="text-sm text-zinc-400">
            {user.membershipExpiresAt
              ? `Vigente hasta ${formatDate(user.membershipExpiresAt)}`
              : "Sin fecha de vencimiento"}
          </p>
        </div>
      )}
      <form onSubmit={onSubmit} className="space-y-4 rounded-2xl border border-white/10 bg-cinema-panel p-6">
        <label className="block text-sm text-zinc-300">
          Nombre
          <input
            required
            value={fullName}
            onChange={(event) => setFullName(event.target.value)}
            className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
          />
        </label>
        <label className="block text-sm text-zinc-300">
          Teléfono
          <input
            inputMode="numeric"
            value={phone}
            onChange={(event) => setPhone(event.target.value.replace(/\D/g, ""))}
            maxLength={10}
            className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
          />
        </label>
        <label className="block text-sm text-zinc-300">
          Cédula
          <input
            required
            inputMode="numeric"
            value={nationalId}
            onChange={(event) => setNationalId(event.target.value.replace(/\D/g, ""))}
            minLength={6}
            maxLength={10}
            className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
          />
        </label>
        <p className="text-xs text-zinc-500">Correo: {user.email} (no editable)</p>
        {error && (
          <div className="rounded-xl border border-rose-500/40 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
            {error}
          </div>
        )}
        {message && <p className="text-sm text-emerald-400">{message}</p>}
        <button
          type="submit"
          disabled={saving}
          className="w-full rounded-xl bg-gold py-3 font-semibold text-cinema disabled:opacity-60"
        >
          {saving ? "Guardando..." : "Guardar cambios"}
        </button>
      </form>
    </div>
  );
}
