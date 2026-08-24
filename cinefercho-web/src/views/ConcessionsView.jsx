import { useEffect, useState } from "react";
import ConcessionSelector from "../components/ConcessionSelector.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { catalogApi } from "../services/api.js";

export default function ConcessionsView() {
  const { isAuthenticated, isClient, openAuth } = useAuth();
  const [products, setProducts] = useState([]);
  const [quantities, setQuantities] = useState({});
  const readOnly = !isClient;

  useEffect(() => {
    catalogApi.products().then(({ data }) => setProducts(data));
  }, []);

  return (
    <div className="space-y-6 pt-10">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Snack bar</p>
        <h1 className="font-display text-4xl">Confitería</h1>
        <p className="text-zinc-400">
          {readOnly
            ? "Consulta el menú. Para agregar snacks a tu compra, inicia sesión y reserva una función."
            : "Arma tu combo y agrégalo al checkout cuando reserves tus asientos."}
        </p>
      </div>
      {readOnly && (
        <button
          type="button"
          onClick={() => openAuth(isAuthenticated ? "login" : "register")}
          className="rounded-xl bg-gold px-5 py-2 text-sm font-semibold text-cinema"
        >
          Iniciar sesión para comprar
        </button>
      )}
      <ConcessionSelector
        products={products}
        quantities={quantities}
        readOnly={readOnly}
        onChange={(id, qty) => setQuantities((current) => ({ ...current, [id]: qty }))}
      />
    </div>
  );
}
