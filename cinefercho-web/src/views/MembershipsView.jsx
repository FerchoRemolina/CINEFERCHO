import { useEffect, useState } from "react";
import { Crown } from "lucide-react";
import { useAuth } from "../context/AuthContext.jsx";
import { catalogApi } from "../services/api.js";
import { formatCOP, formatDate, membershipLabel } from "../utils/format.js";

export default function MembershipsView() {
  const { hasActiveMembership, user } = useAuth();
  const [plans, setPlans] = useState([]);

  useEffect(() => {
    catalogApi.memberships().then(({ data }) => setPlans(data));
  }, []);

  const visiblePlans = hasActiveMembership
    ? plans.filter((plan) => plan.name === user?.membershipType)
    : plans;

  return (
    <div className="space-y-8 pt-10">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Club CINEFERCHO</p>
        <h1 className="font-display text-4xl">Membresías</h1>
        {hasActiveMembership ? (
          <p className="text-zinc-400">
            Tu plan {membershipLabel(user.membershipType)} está activo hasta{" "}
            {formatDate(user.membershipExpiresAt)}. El descuento se aplica solo en tus compras.
          </p>
        ) : (
          <p className="text-zinc-400">
            GOLD dura un año y PRO un mes, ambos desde el día de la compra. Se adquieren en el
            checkout y el descuento se aplica de inmediato.
          </p>
        )}
      </div>
      <div className={`grid gap-8 ${visiblePlans.length > 1 ? "grid-cols-2" : "grid-cols-1 max-w-xl"}`}>
        {visiblePlans.map((plan) => (
          <article
            key={plan.id}
            className="rounded-[28px] border border-gold/30 bg-cinema-panel p-10"
          >
            <Crown className="h-10 w-10 text-gold" />
            <h2 className="font-display mt-4 text-3xl">{plan.name}</h2>
            <p className="mt-2 text-4xl font-semibold text-gold">{formatCOP(plan.monthlyPrice)}</p>
            <p className="mt-1 text-sm text-zinc-500">{plan.durationLabel}</p>
            <ul className="mt-6 space-y-2 text-zinc-300">
              <li>{plan.discountPercentageTickets}% de descuento en boletas</li>
              <li>{plan.discountPercentageConcession}% de descuento en confitería</li>
              {hasActiveMembership ? (
                <li>Ya forma parte de tu cuenta. No puedes comprar otra mientras esté vigente.</li>
              ) : (
                <li>Se adquiere junto a tu próxima compra de boletos</li>
              )}
            </ul>
          </article>
        ))}
      </div>
    </div>
  );
}
