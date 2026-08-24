import { useEffect, useMemo, useState } from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import MovieCard from "../components/MovieCard.jsx";
import MovieDetailModal from "../components/MovieDetailModal.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { catalogApi } from "../services/api.js";
import { nextDays, toIsoDate } from "../utils/format.js";

const TABS = [
  { id: "now", label: "En Cartelera" },
  { id: "presale", label: "Preventa" },
  { id: "upcoming", label: "Próximos Estrenos" },
];

function matchesDateAndTheater(screening, theaterId, date) {
  if (!screening?.startTime) return false;
  const sameTheater = !theaterId || screening.theaterId === theaterId;
  return sameTheater && String(screening.startTime).startsWith(date);
}

export default function HomeView() {
  const navigate = useNavigate();
  const { selectedTheater, openAuth, isAuthenticated, isAdmin } = useAuth();
  const [tab, setTab] = useState("now");
  const [movies, setMovies] = useState([]);
  const [presale, setPresale] = useState([]);
  const [upcoming, setUpcoming] = useState([]);
  const [date, setDate] = useState(toIsoDate(new Date()));
  const [slide, setSlide] = useState(0);
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState(null);
  const days = useMemo(() => nextDays(7), []);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      const theaterId = selectedTheater?.id;
      if (!theaterId) {
        setMovies([]);
        setPresale([]);
        setUpcoming([]);
        setLoading(false);
        return;
      }
      setLoading(true);
      try {
        const [{ data: nowShowing }, { data: presaleMovies }, { data: upcomingMovies }] =
          await Promise.all([
            catalogApi.nowShowing(theaterId),
            catalogApi.presale(theaterId),
            catalogApi.upcoming(theaterId),
          ]);
        const withFunctions = nowShowing.map((movie) => {
          const match = (movie.screenings || []).find((screening) =>
            matchesDateAndTheater(screening, theaterId, date),
          );
          return match ? { ...movie, nextScreening: match } : movie;
        });
        if (!cancelled) {
          setMovies(withFunctions.filter((movie) => movie.nextScreening));
          setPresale(presaleMovies);
          setUpcoming(upcomingMovies);
        }
      } catch {
        if (!cancelled) {
          setMovies([]);
          setPresale([]);
          setUpcoming([]);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => {
      cancelled = true;
    };
  }, [selectedTheater?.id, date]);

  const catalog = tab === "now" ? movies : tab === "presale" ? presale : upcoming;
  const featured = catalog;
  const current =
    featured.length > 0
      ? featured[((slide % featured.length) + featured.length) % featured.length]
      : null;

  useEffect(() => {
    setSlide(0);
  }, [tab]);

  useEffect(() => {
    if (featured.length < 2) return undefined;
    const timer = setInterval(() => setSlide((value) => value + 1), 5500);
    return () => clearInterval(timer);
  }, [featured.length, tab]);

  const buy = (movie) => {
    if (movie.ticketsEnabled === false) {
      setDetail(movie);
      return;
    }
    if (isAdmin) {
      navigate("/admin");
      return;
    }
    const goBook = () => navigate(`/reservar/${movie.id}`);
    if (!isAuthenticated) {
      openAuth("login", goBook);
      return;
    }
    goBook();
  };

  return (
    <div className="space-y-10 pt-8">
      <section className="relative overflow-hidden rounded-[28px] border border-white/10 bg-cinema-panel">
        {current && (
          <div className="grid min-h-[420px] grid-cols-[1.2fr_0.8fr]">
            <div className="flex flex-col justify-end p-12">
              <p className="text-sm uppercase tracking-[0.35em] text-gold">
                {tab === "presale" ? "Preventa exclusiva" : tab === "upcoming" ? "Próximo estreno" : "En cartelera"}
              </p>
              <h1 className="font-display mt-3 text-5xl text-white">{current.title}</h1>
              <p className="mt-4 max-w-xl text-zinc-300">{current.description}</p>
              <div className="mt-8 flex gap-3">
                {current.ticketsEnabled !== false ? (
                  <button
                    type="button"
                    onClick={() => buy(current)}
                    className="rounded-xl bg-rose-600 px-6 py-3 font-semibold hover:bg-rose-500"
                  >
                    {tab === "presale" ? "Reserva anticipada" : "Comprar entradas"}
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={() => setDetail(current)}
                    className="rounded-xl border border-gold/40 px-6 py-3 font-semibold text-gold hover:bg-gold/10"
                  >
                    Ver ficha técnica
                  </button>
                )}
                <span className="rounded-xl border border-white/10 px-4 py-3 text-sm text-zinc-300">
                  {current.genre} · {current.ageRating}
                  {current.format ? ` · ${current.format}` : ""}
                </span>
              </div>
            </div>
            <img
              src={current.posterUrl}
              alt={current.title}
              className="h-full w-full object-cover"
            />
          </div>
        )}
        {featured.length > 1 && (
          <>
            <button
              type="button"
              className="absolute left-4 top-1/2 -translate-y-1/2 rounded-full bg-black/50 p-2"
              onClick={() => setSlide((value) => value - 1)}
            >
              <ChevronLeft />
            </button>
            <button
              type="button"
              className="absolute right-4 top-1/2 -translate-y-1/2 rounded-full bg-black/50 p-2"
              onClick={() => setSlide((value) => value + 1)}
            >
              <ChevronRight />
            </button>
          </>
        )}
      </section>

      <section>
        <div className="mb-6 flex items-end justify-between gap-6">
          <div>
            <h2 className="font-display text-3xl">Cartelera</h2>
            <p className="text-sm text-zinc-400">
              {selectedTheater
                ? `Funciones en ${selectedTheater.name}`
                : "Selecciona un teatro en la barra superior"}
            </p>
          </div>
          <div className="flex rounded-2xl bg-cinema-panel p-1">
            {TABS.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setTab(item.id)}
                className={`rounded-xl px-5 py-2 text-sm font-semibold ${
                  tab === item.id ? "bg-gold text-cinema" : "text-zinc-400 hover:text-white"
                }`}
              >
                {item.label}
              </button>
            ))}
          </div>
        </div>

        {tab === "now" && (
          <div className="mb-6 flex gap-2">
            {days.map((day) => {
              const iso = toIsoDate(day);
              const label = day.toLocaleDateString("es-CO", { weekday: "short", day: "numeric" });
              const active = iso === date;
              return (
                <button
                  key={iso}
                  type="button"
                  onClick={() => setDate(iso)}
                  className={`rounded-xl px-3 py-2 text-sm capitalize ${
                    active ? "bg-gold text-cinema" : "bg-cinema-panel text-zinc-300"
                  }`}
                >
                  {label}
                </button>
              );
            })}
          </div>
        )}

        {tab === "presale" && (
          <p className="mb-6 text-sm text-zinc-400">
            Estreno en 15 días o menos. Solo puedes comprar funciones del día de estreno.
          </p>
        )}
        {tab === "upcoming" && (
          <p className="mb-6 text-sm text-zinc-400">
            Estreno a más de 15 días. Haz clic en la ficha para ver la Descripción-Película. La compra
            aún no está habilitada.
          </p>
        )}

        {loading ? (
          <p className="text-zinc-400">Cargando cartelera...</p>
        ) : catalog.length === 0 ? (
          <p className="text-zinc-400">
            {!selectedTheater
              ? "Selecciona un teatro en la barra superior para ver su cartelera."
              : tab === "now"
                ? `No hay funciones para esta fecha en ${selectedTheater.name}.`
                : tab === "presale"
                  ? `No hay preventas en ${selectedTheater.name}.`
                  : `No hay próximos estrenos para ${selectedTheater.name}.`}
          </p>
        ) : (
          <div className="grid grid-cols-3 gap-7">
            {catalog.map((movie) => (
              <MovieCard
                key={movie.id}
                movie={movie}
                badge={tab === "presale" ? "PREVENTA EXCLUSIVA" : null}
                buyLabel={tab === "presale" ? "Reserva anticipada" : "Comprar entradas"}
                onBuy={tab === "upcoming" ? undefined : buy}
                onOpen={tab === "upcoming" ? setDetail : undefined}
              />
            ))}
          </div>
        )}
      </section>

      {detail && <MovieDetailModal movie={detail} onClose={() => setDetail(null)} />}
    </div>
  );
}
