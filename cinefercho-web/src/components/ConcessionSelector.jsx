import { Minus, Plus } from "lucide-react";
import { formatCOP } from "../utils/format.js";

export default function ConcessionSelector({ products, quantities, onChange, readOnly = false }) {
  if (!products?.length) {
    return <p className="text-zinc-400">No hay productos de confitería disponibles.</p>;
  }

  return (
    <div className="grid grid-cols-2 gap-6">
      {products.map((product) => {
        const qty = quantities[product.id] || 0;
        return (
          <article
            key={product.id}
            className="flex gap-4 overflow-hidden rounded-2xl border border-white/10 bg-cinema-panel"
          >
            <img
              src={product.imageUrl}
              alt={product.name}
              className="h-36 w-36 object-cover"
              onError={(event) => {
                event.currentTarget.src =
                  "https://images.unsplash.com/photo-1578849277030-d13a5a4d0d3d?auto=format&fit=crop&w=400&q=80";
              }}
            />
            <div className="flex flex-1 flex-col justify-between p-4">
              <div>
                <p className="text-xs uppercase tracking-wide text-gold">{product.category}</p>
                <h3 className="text-lg font-semibold">{product.name}</h3>
                <p className="mt-1 line-clamp-2 text-sm text-zinc-400">{product.description}</p>
                <p className="mt-1 text-xs text-zinc-500">Stock: {product.stock}</p>
              </div>
              <div className="mt-3 flex items-center justify-between">
                <span className="font-semibold text-gold">{formatCOP(product.price)}</span>
                {readOnly ? (
                  <span className="text-xs text-zinc-500">Solo lectura</span>
                ) : (
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      disabled={qty === 0}
                      onClick={() => onChange(product.id, Math.max(0, qty - 1))}
                      className="rounded-lg border border-white/10 p-1.5 hover:bg-white/5 disabled:opacity-30"
                    >
                      <Minus className="h-4 w-4" />
                    </button>
                    <span className="w-6 text-center font-semibold">{qty}</span>
                    <button
                      type="button"
                      disabled={qty >= product.stock}
                      onClick={() => onChange(product.id, qty + 1)}
                      className="rounded-lg border border-white/10 p-1.5 hover:bg-white/5 disabled:opacity-30"
                    >
                      <Plus className="h-4 w-4" />
                    </button>
                  </div>
                )}
              </div>
            </div>
          </article>
        );
      })}
    </div>
  );
}
