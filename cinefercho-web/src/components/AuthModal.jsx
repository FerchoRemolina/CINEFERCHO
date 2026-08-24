import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function AuthModal() {
  const navigate = useNavigate();
  const { authOpen, authTab, setAuthTab, closeAuth, login, register, authError, authLoading } =
    useAuth();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [nationalId, setNationalId] = useState("");
  const [password, setPassword] = useState("");

  useEffect(() => {
    setFullName("");
    setEmail("");
    setNationalId("");
    setPassword("");
  }, [authOpen, authTab]);

  const switchTab = (tab) => {
    setFullName("");
    setEmail("");
    setNationalId("");
    setPassword("");
    setAuthTab(tab);
  };

  if (!authOpen) return null;

  const onSubmit = async (event) => {
    event.preventDefault();
    try {
      let nextUser;
      if (authTab === "register") {
        nextUser = await register(fullName, email, password, nationalId.replace(/\D/g, ""));
      } else {
        nextUser = await login(email, password);
      }
      if (nextUser?.role === "ROLE_ADMIN") {
        navigate("/admin", { replace: true });
      }
    } catch {
      /* el error se muestra desde el contexto */
    }
  };

  const duplicateId = /cédula ya se encuentra registrado/i.test(authError || "");

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
      <div className="w-[460px] rounded-2xl border border-white/10 bg-cinema-panel p-8 shadow-2xl">
        <div className="mb-6 flex items-center justify-between">
          <h2 className="font-display text-2xl text-gold">
            {authTab === "login" ? "Entrar" : "Crear cuenta"}
          </h2>
          <button type="button" onClick={closeAuth} className="text-zinc-400 hover:text-white">
            ✕
          </button>
        </div>
        <div className="mb-6 grid grid-cols-2 rounded-xl bg-cinema p-1">
          <button
            type="button"
            onClick={() => switchTab("login")}
            className={`rounded-lg py-2 text-sm font-medium ${
              authTab === "login" ? "bg-gold text-cinema" : "text-zinc-400"
            }`}
          >
            Login
          </button>
          <button
            type="button"
            onClick={() => switchTab("register")}
            className={`rounded-lg py-2 text-sm font-medium ${
              authTab === "register" ? "bg-gold text-cinema" : "text-zinc-400"
            }`}
          >
            Registro
          </button>
        </div>
        <form onSubmit={onSubmit} className="space-y-4" autoComplete="off">
          {authTab === "register" && (
            <>
              <label className="block text-sm text-zinc-300">
                Nombre completo
                <input
                  required
                  value={fullName}
                  autoComplete="off"
                  onChange={(event) => setFullName(event.target.value)}
                  className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 text-white outline-none focus:border-gold"
                />
              </label>
              <label className="block text-sm text-zinc-300">
                Número de Cédula
                <input
                  required
                  inputMode="numeric"
                  pattern="\d{6,10}"
                  minLength={6}
                  maxLength={10}
                  value={nationalId}
                  autoComplete="off"
                  onChange={(event) => setNationalId(event.target.value.replace(/\D/g, ""))}
                  className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 text-white outline-none focus:border-gold"
                />
              </label>
            </>
          )}
          <label className="block text-sm text-zinc-300">
            Correo
            <input
              required
              type="email"
              autoComplete="off"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 text-white outline-none focus:border-gold"
            />
          </label>
          <label className="block text-sm text-zinc-300">
            Contraseña
            <input
              required
              minLength={authTab === "register" ? 8 : 1}
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 text-white outline-none focus:border-gold"
            />
          </label>
          {authError && (
            <div
              className={`rounded-xl border px-4 py-3 text-sm ${
                duplicateId
                  ? "border-rose-500/50 bg-rose-500/15 text-rose-200"
                  : "border-rose-500/30 bg-rose-500/10 text-rose-300"
              }`}
            >
              {duplicateId ? "El número de cédula ya se encuentra registrado" : authError}
            </div>
          )}
          <button
            type="submit"
            disabled={authLoading}
            className="w-full rounded-xl bg-gold py-3 font-semibold text-cinema hover:bg-gold-dim disabled:opacity-60"
          >
            {authLoading ? "Procesando..." : authTab === "login" ? "Iniciar sesión" : "Registrarme"}
          </button>
          <p className="text-center text-xs text-zinc-500">
            El registro crea una cuenta de cliente. La cédula es única y no se puede repetir.
          </p>
        </form>
      </div>
    </div>
  );
}
