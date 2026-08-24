import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { clientApi, getApiError } from "../services/api.js";
import { formatCOP, formatDateTime } from "../utils/format.js";

export default function InvoicesView() {
  const { isClient, openAuth } = useAuth();
  const [invoices, setInvoices] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isClient) return;
    clientApi
      .invoices()
      .then(({ data }) => setInvoices(data))
      .catch((err) => setError(getApiError(err, "No se pudo cargar el historial.")));
  }, [isClient]);

  if (!isClient) {
    return (
      <div className="pt-16 text-center">
        <p className="text-zinc-400">Inicia sesión para ver tu historial de facturas.</p>
        <button
          type="button"
          onClick={() => openAuth("login")}
          className="mt-4 rounded-xl bg-gold px-5 py-2 font-semibold text-cinema"
        >
          Iniciar sesión
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6 pt-10">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Historial</p>
        <h1 className="font-display text-4xl">Mis facturas</h1>
        <p className="text-zinc-400">Cada reserva incluye UUID y código QR.</p>
      </div>
      {error && <p className="text-rose-400">{error}</p>}
      {invoices.length === 0 && !error && (
        <p className="text-zinc-400">Aún no tienes compras. Reserva una función para generar tu primera factura.</p>
      )}
      <div className="grid grid-cols-2 gap-6">
        {invoices.map((invoice) => {
          const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=120x120&data=${encodeURIComponent(
            invoice.invoiceNumber,
          )}`;
          return (
            <Link
              key={invoice.id}
              to={`/factura/${invoice.invoiceNumber}`}
              state={{ invoice }}
              className="flex gap-5 rounded-2xl border border-white/10 bg-cinema-panel p-5 hover:border-gold/40"
            >
              <img src={qrUrl} alt="QR de reserva" className="h-24 w-24 rounded-lg bg-white p-1" />
              <div className="min-w-0">
                <p className="text-xs uppercase tracking-wide text-zinc-500">UUID</p>
                <p className="truncate font-mono text-sm text-gold">{invoice.invoiceNumber}</p>
                <p className="mt-2 text-sm text-zinc-300">{invoice.theater?.name}</p>
                <p className="text-xs text-zinc-500">{formatDateTime(invoice.createdAt)}</p>
                <p className="mt-2 font-semibold">{formatCOP(invoice.totalAmount)}</p>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
}
