import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ConcessionSelector from "../components/ConcessionSelector.jsx";
import SeatMap from "../components/SeatMap.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { catalogApi, clientApi, getApiError } from "../services/api.js";
import {
  formatCOP,
  formatTime,
  isMembershipActive,
  membershipDiscountRate,
  membershipLabel,
  nextDays,
  toIsoDate,
} from "../utils/format.js";

const STEPS = ["Función", "Asientos", "Confitería", "Pago"];

export default function BookingView() {
  const { movieId } = useParams();
  const navigate = useNavigate();
  const { selectedTheater, user, isClient, hasActiveMembership, openAuth, refreshUser } = useAuth();
  const [step, setStep] = useState(1);
  const [date, setDate] = useState(toIsoDate(new Date()));
  const [movie, setMovie] = useState(null);
  const [screenings, setScreenings] = useState([]);
  const [screening, setScreening] = useState(null);
  const [seatMap, setSeatMap] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [products, setProducts] = useState([]);
  const [quantities, setQuantities] = useState({});
  const [plans, setPlans] = useState([]);
  const [buyPlanId, setBuyPlanId] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const days = useMemo(() => nextDays(7), []);

  useEffect(() => {
    Promise.all([
      catalogApi.movies(),
      selectedTheater?.id ? catalogApi.upcoming(selectedTheater.id) : Promise.resolve({ data: [] }),
    ]).then(([{ data }, { data: upcoming }]) => {
      const found = data.find((item) => String(item.id) === String(movieId)) || null;
      const upcomingMatch = upcoming.find((item) => String(item.id) === String(movieId));
      setMovie(upcomingMatch ? { ...found, ...upcomingMatch } : found);
    });
    catalogApi.products().then(({ data }) => setProducts(data));
    catalogApi.memberships().then(({ data }) => setPlans(data));
  }, [movieId, selectedTheater?.id]);

  useEffect(() => {
    if (!selectedTheater?.id || !movieId) return;
    setScreening(null);
    setSelectedSeats([]);
    setSeatMap(null);
    catalogApi
      .screenings(movieId, selectedTheater.id, date)
      .then(({ data }) => setScreenings(data))
      .catch(() => setScreenings([]));
  }, [movieId, selectedTheater?.id, date]);

  useEffect(() => {
    if (!screening?.id) return;
    catalogApi.seatMap(screening.id).then(({ data }) => setSeatMap(data));
  }, [screening?.id]);

  const toggleSeat = (seat) => {
    if (!isClient) {
      openAuth("login");
      return;
    }
    setSelectedSeats((current) => {
      const exists = current.some((item) => item.id === seat.id);
      return exists ? current.filter((item) => item.id !== seat.id) : [...current, seat];
    });
  };

  const ticketSubtotal = selectedSeats.length * Number(screening?.ticketPrice || 0);
  const concessionLines = products
    .filter((product) => (quantities[product.id] || 0) > 0)
    .map((product) => ({
      product,
      quantity: quantities[product.id],
      subtotal: quantities[product.id] * Number(product.price),
    }));
  const concessionSubtotal = concessionLines.reduce((sum, line) => sum + line.subtotal, 0);
  const selectedPlan = hasActiveMembership ? null : plans.find((plan) => String(plan.id) === String(buyPlanId));
  const membershipFee = selectedPlan ? Number(selectedPlan.monthlyPrice) : 0;
  const effectiveMembership = selectedPlan?.name || (isMembershipActive(user) ? user.membershipType : "NONE");
  const discountRate = membershipDiscountRate(effectiveMembership);
  const discountable = ticketSubtotal + concessionSubtotal;
  const discountAmount = discountable * discountRate;
  const total = discountable + membershipFee - discountAmount;

  const confirm = async () => {
    if (!isClient) {
      openAuth("login");
      return;
    }
    if (!screening || selectedSeats.length === 0) {
      setError("Selecciona una función y al menos un asiento.");
      return;
    }
    setSubmitting(true);
    setError("");
    try {
      const { data } = await clientApi.purchase({
        screeningId: screening.id,
        seatIds: selectedSeats.map((seat) => seat.id),
        concessionItems: concessionLines.map((line) => ({
          productId: line.product.id,
          quantity: line.quantity,
        })),
        buyMembershipPlanId: hasActiveMembership ? null : selectedPlan ? selectedPlan.id : null,
      });
      try {
        await refreshUser();
      } catch {
        /* la factura ya se emitió; la sesión se sincroniza en la siguiente carga */
      }
      navigate(`/factura/${data.invoiceNumber}`, { state: { invoice: data } });
    } catch (err) {
      setError(getApiError(err, "No se pudo completar la compra."));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-8 pt-8">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Reserva</p>
        <h1 className="font-display text-4xl">{movie?.title || "Cargando película"}</h1>
        <p className="text-zinc-400">
          {selectedTheater ? selectedTheater.name : "Selecciona un teatro para continuar"}
        </p>
      </div>

      {movie?.ticketsEnabled === false && (
        <p className="rounded-2xl border border-white/10 bg-cinema-panel p-6 text-zinc-300">
          Esta película aún no está disponible para la venta de boletos. El estreno está a más de 15
          días.
        </p>
      )}

      {movie && movie.ticketsEnabled !== false && (
        <>
      <ol className="grid grid-cols-4 gap-3">
        {STEPS.map((label, index) => {
          const number = index + 1;
          const active = step === number;
          return (
            <li
              key={label}
              className={`rounded-xl border px-4 py-3 text-sm ${
                active ? "border-gold bg-gold/10 text-gold" : "border-white/10 text-zinc-400"
              }`}
            >
              <span className="mr-2 font-semibold">{number}.</span>
              {label}
            </li>
          );
        })}
      </ol>

      {step === 1 && (
        <section className="space-y-6">
          <div className="flex gap-2">
            {days.map((day) => {
              const iso = toIsoDate(day);
              return (
                <button
                  key={iso}
                  type="button"
                  onClick={() => setDate(iso)}
                  className={`rounded-xl px-4 py-2 text-sm capitalize ${
                    iso === date ? "bg-gold text-cinema" : "bg-cinema-panel"
                  }`}
                >
                  {day.toLocaleDateString("es-CO", { weekday: "short", day: "numeric" })}
                </button>
              );
            })}
          </div>
          <div className="grid grid-cols-2 gap-4">
            {screenings.length === 0 && (
              <p className="col-span-2 text-zinc-400">No hay funciones para esta fecha en el teatro seleccionado.</p>
            )}
            {screenings.map((item) => (
              <button
                key={item.id}
                type="button"
                onClick={() => setScreening(item)}
                className={`rounded-2xl border p-5 text-left ${
                  screening?.id === item.id ? "border-gold bg-gold/10" : "border-white/10 bg-cinema-panel"
                }`}
              >
                <p className="text-lg font-semibold">{formatTime(item.startTime)}</p>
                <p className="text-sm text-zinc-400">
                  {item.hallName} · {item.hallType} · {item.format}
                </p>
                <p className="mt-2 text-gold">{formatCOP(item.ticketPrice)}</p>
              </button>
            ))}
          </div>
        </section>
      )}

      {step === 2 && (
        <>
          {!isClient && (
            <p className="text-sm text-amber-300">
              Inicia sesión o regístrate para seleccionar asientos. Gris = libre, rojo = ocupado, verde
              = seleccionado.
            </p>
          )}
          <SeatMap
            seatMap={seatMap}
            selectedIds={selectedSeats.map((seat) => seat.id)}
            onToggle={toggleSeat}
          />
        </>
      )}

      {step === 3 && (
        <section>
          <p className="mb-4 text-sm text-zinc-400">
            La confitería se liga a esta función. Puedes continuar sin agregar productos.
          </p>
          <ConcessionSelector
            products={products}
            quantities={quantities}
            onChange={(id, qty) => setQuantities((current) => ({ ...current, [id]: qty }))}
          />
        </section>
      )}

      {step === 4 && (
        <section className="grid grid-cols-[1.2fr_0.8fr] gap-8">
          <div className="space-y-4 rounded-2xl border border-white/10 bg-cinema-panel p-6">
            <h2 className="font-display text-2xl">Resumen</h2>
            <p>
              {selectedSeats.length} asiento(s):{" "}
              {selectedSeats.map((seat) => `${seat.rowLetter}${seat.seatNumber}`).join(", ") || "Ninguno"}
            </p>
            <p>Boletos: {formatCOP(ticketSubtotal)}</p>
            {concessionLines.map((line) => (
              <p key={line.product.id}>
                {line.product.name} x{line.quantity}: {formatCOP(line.subtotal)}
              </p>
            ))}
            <div className="border-t border-white/10 pt-4">
              {hasActiveMembership ? (
                <div className="rounded-xl border border-gold/30 bg-gold/10 p-4">
                  <p className="text-sm uppercase tracking-[0.2em] text-gold">Membresía activa</p>
                  <p className="mt-1 text-lg font-semibold">{membershipLabel(user.membershipType)}</p>
                  <p className="text-sm text-zinc-400">
                    Vigente hasta {new Date(user.membershipExpiresAt).toLocaleDateString("es-CO", { dateStyle: "long" })}.
                    El descuento ya está aplicado.
                  </p>
                </div>
              ) : (
                <>
                  <p className="mb-2 text-sm text-zinc-400">¿Quieres una membresía en esta compra?</p>
                  <select
                    value={buyPlanId}
                    onChange={(event) => setBuyPlanId(event.target.value)}
                    className="w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
                  >
                    <option value="">No, gracias</option>
                    {plans.map((plan) => (
                      <option key={plan.id} value={plan.id} className="text-zinc-900">
                        {plan.name} · {formatCOP(plan.monthlyPrice)} · {plan.durationLabel}
                      </option>
                    ))}
                  </select>
                </>
              )}
            </div>
          </div>
          <aside className="rounded-2xl border border-gold/30 bg-gold/5 p-6">
            <p className="text-sm text-zinc-400">Subtotal</p>
            <p className="text-xl">{formatCOP(discountable + membershipFee)}</p>
            <p className="mt-3 text-sm text-emerald-400">
              Descuento membresía ({Math.round(discountRate * 100)}%): -{formatCOP(discountAmount)}
            </p>
            <p className="mt-4 font-display text-3xl text-gold">{formatCOP(total)}</p>
            {error && <p className="mt-3 text-sm text-rose-400">{error}</p>}
            <button
              type="button"
              disabled={submitting}
              onClick={confirm}
              className="mt-6 w-full rounded-xl bg-rose-600 py-3 font-semibold hover:bg-rose-500 disabled:opacity-60"
            >
              {isClient ? (submitting ? "Confirmando..." : "Confirmar compra") : "Inicia sesión para pagar"}
            </button>
          </aside>
        </section>
      )}

      <div className="flex justify-between">
        <button
          type="button"
          disabled={step === 1}
          onClick={() => setStep((value) => value - 1)}
          className="rounded-xl border border-white/10 px-5 py-2 disabled:opacity-30"
        >
          Atrás
        </button>
        {step < 4 && (
          <button
            type="button"
            onClick={() => {
              if (step === 1 && !screening) {
                setError("Selecciona un horario.");
                return;
              }
              if (step === 2 && !isClient) {
                openAuth("login");
                return;
              }
              if (step === 2 && selectedSeats.length === 0) {
                setError("Selecciona al menos un asiento.");
                return;
              }
              setError("");
              setStep((value) => value + 1);
            }}
            className="rounded-xl bg-gold px-5 py-2 font-semibold text-cinema"
          >
            Continuar
          </button>
        )}
      </div>
      {error && step < 4 && <p className="text-sm text-rose-400">{error}</p>}
        </>
      )}
    </div>
  );
}
