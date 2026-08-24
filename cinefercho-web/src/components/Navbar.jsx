import { NavLink, useNavigate } from "react-router-dom";
import { Clapperboard, Crown, LayoutDashboard, LogOut, Popcorn, Receipt, Ticket, UserRound } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";
import { membershipLabel } from "../utils/format.js";
import SessionCountdown from "./SessionCountdown.jsx";

const publicLinks = [
  { to: "/", label: "Cartelera", icon: Ticket },
  { to: "/confiteria", label: "Confitería", icon: Popcorn },
  { to: "/membresias", label: "Membresías", icon: Crown },
];

const clientLinks = [
  { to: "/facturas", label: "Mis facturas", icon: Receipt },
  { to: "/cuenta", label: "Mi cuenta", icon: UserRound },
];

export default function Navbar() {
  const navigate = useNavigate();
  const {
    user,
    isAuthenticated,
    isAdmin,
    isClient,
    hasActiveMembership,
    cities,
    theaters,
    selectedCity,
    selectedTheater,
    selectCity,
    selectTheater,
    logout,
    openAuth,
  } = useAuth();

  const visiblePublicLinks = hasActiveMembership
    ? publicLinks.filter((link) => link.to !== "/membresias")
    : publicLinks;

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <header className="sticky top-0 z-40 border-b border-white/10 bg-cinema/90 backdrop-blur-xl">
      <div className="mx-auto flex max-w-[1400px] items-center gap-8 px-8 py-4">
        <button
          type="button"
          onClick={() => navigate(isAdmin ? "/admin" : "/")}
          className="flex items-center gap-3 text-left"
        >
          <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-gold text-cinema">
            <Clapperboard className="h-6 w-6" />
          </span>
          <span>
            <span className="font-display block text-xl tracking-[0.18em] text-gold">CINEFERCHO</span>
            <span className="text-xs uppercase tracking-[0.22em] text-zinc-400">
              {isAdmin ? "Administración" : "Multi-teatro"}
            </span>
          </span>
        </button>

        {!isAdmin && (
          <div className="flex items-center gap-2 rounded-xl border border-white/10 bg-cinema-panel px-3 py-2">
            <select
              className="bg-transparent text-sm text-zinc-100 outline-none"
              value={selectedCity?.id || ""}
              onChange={(event) => {
                const city = cities.find((item) => String(item.id) === event.target.value);
                selectCity(city || null);
              }}
            >
              <option value="">Ciudad</option>
              {cities.map((city) => (
                <option key={city.id} value={city.id} className="text-zinc-900">
                  {city.name}
                </option>
              ))}
            </select>
            <span className="text-zinc-600">|</span>
            <select
              className="min-w-[220px] bg-transparent text-sm text-zinc-100 outline-none"
              value={selectedTheater?.id || ""}
              onChange={(event) => {
                const theater = theaters.find((item) => String(item.id) === event.target.value);
                selectTheater(theater || null);
              }}
            >
              <option value="">Teatro</option>
              {theaters.map((theater) => (
                <option key={theater.id} value={theater.id} className="text-zinc-900">
                  {theater.name}
                </option>
              ))}
            </select>
          </div>
        )}

        <nav className="ml-auto flex items-center gap-6">
          {isAdmin ? (
            <NavLink
              to="/admin"
              className={({ isActive }) =>
                `flex items-center gap-2 text-sm font-medium transition ${
                  isActive ? "text-gold" : "text-zinc-300 hover:text-white"
                }`
              }
            >
              <LayoutDashboard className="h-4 w-4" />
              Dashboard
            </NavLink>
          ) : (
            <>
              {visiblePublicLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  className={({ isActive }) =>
                    `flex items-center gap-2 text-sm font-medium transition ${
                      isActive ? "text-gold" : "text-zinc-300 hover:text-white"
                    }`
                  }
                >
                  <link.icon className="h-4 w-4" />
                  {link.label}
                </NavLink>
              ))}
              {isClient &&
                clientLinks.map((link) => (
                  <NavLink
                    key={link.to}
                    to={link.to}
                    className={({ isActive }) =>
                      `flex items-center gap-2 text-sm font-medium transition ${
                        isActive ? "text-gold" : "text-zinc-300 hover:text-white"
                      }`
                    }
                  >
                    <link.icon className="h-4 w-4" />
                    {link.label}
                  </NavLink>
                ))}
            </>
          )}
        </nav>

        {isAuthenticated ? (
          <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-cinema-panel px-3 py-2">
            <UserRound className="h-4 w-4 text-gold" />
            <div className="leading-tight">
              <p className="text-sm font-medium">{user.fullName}</p>
              <p className="text-[11px] text-zinc-400">
                {isAdmin ? "Administrador" : membershipLabel(user.membershipType)}
              </p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-lg p-2 text-zinc-400 hover:bg-white/5 hover:text-white"
              title="Cerrar sesión"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => openAuth("login")}
            className="rounded-xl bg-gold px-4 py-2 text-sm font-semibold text-cinema hover:bg-gold-dim"
          >
            Iniciar sesión
          </button>
        )}
      </div>
      <SessionCountdown />
    </header>
  );
}
