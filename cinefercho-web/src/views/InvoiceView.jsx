import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { clientApi } from "../services/api.js";
import { formatCOP, formatDateTime, membershipLabel } from "../utils/format.js";

export default function InvoiceView() {
  const { invoiceNumber } = useParams();
  const location = useLocation();
  const [invoice, setInvoice] = useState(location.state?.invoice || null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (invoice) return;
    clientApi
      .invoices()
      .then(({ data }) => {
        const found = data.find((item) => String(item.invoiceNumber) === String(invoiceNumber));
        setInvoice(found || null);
        if (!found) setError("No encontramos esa factura en tu historial.");
      })
      .catch(() => setError("Inicia sesión para consultar tu factura."));
  }, [invoice, invoiceNumber]);

  if (error && !invoice) {
    return <p className="pt-16 text-center text-zinc-400">{error}</p>;
  }
  if (!invoice) {
    return <p className="pt-16 text-center text-zinc-400">Cargando factura...</p>;
  }

  const qrUrl = `https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(invoice.invoiceNumber)}`;

  return (
    <div className="mx-auto max-w-4xl space-y-8 pt-10">
      <div className="rounded-[28px] border border-gold/40 bg-cinema-panel p-10 shadow-2xl">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.3em] text-gold">Factura digital</p>
            <h1 className="font-display mt-2 text-4xl">CINEFERCHO</h1>
            <p className="mt-2 text-zinc-400">{invoice.theater?.name}</p>
          </div>
          <img src={qrUrl} alt="Código QR de reserva" className="rounded-xl bg-white p-2" />
        </div>
        <div className="mt-8 grid grid-cols-2 gap-6 text-sm">
          <p>
            <span className="text-zinc-500">UUID / reserva</span>
            <br />
            <span className="font-mono text-gold">{invoice.invoiceNumber}</span>
          </p>
          <p>
            <span className="text-zinc-500">Fecha</span>
            <br />
            {formatDateTime(invoice.createdAt)}
          </p>
          <p>
            <span className="text-zinc-500">Cliente</span>
            <br />
            {invoice.client?.fullName} · {invoice.client?.email}
            {invoice.client?.nationalId ? ` · CC ${invoice.client.nationalId}` : ""}
          </p>
          <p>
            <span className="text-zinc-500">Membresía</span>
            <br />
            {membershipLabel(invoice.client?.membershipType)}
            {invoice.client?.membershipExpiresAt
              ? ` · hasta ${formatDateTime(invoice.client.membershipExpiresAt)}`
              : ""}
          </p>
        </div>

        <div className="mt-8 overflow-hidden rounded-2xl border border-white/10">
          <table className="w-full text-left text-sm">
            <thead className="bg-cinema text-zinc-400">
              <tr>
                <th className="px-4 py-3">Descripción</th>
                <th className="px-4 py-3">Detalle</th>
                <th className="px-4 py-3 text-right">Valor</th>
              </tr>
            </thead>
            <tbody>
              {invoice.tickets?.map((ticket) => (
                <tr key={ticket.id} className="border-t border-white/5">
                  <td className="px-4 py-3">{ticket.movieTitle}</td>
                  <td className="px-4 py-3 text-zinc-400">
                    Asiento {ticket.rowLetter}
                    {ticket.seatNumber} {ticket.vip ? "VIP" : ""}
                  </td>
                  <td className="px-4 py-3 text-right">{formatCOP(ticket.price)}</td>
                </tr>
              ))}
              {invoice.concessions?.map((item) => (
                <tr key={item.id} className="border-t border-white/5">
                  <td className="px-4 py-3">{item.productName}</td>
                  <td className="px-4 py-3 text-zinc-400">x{item.quantity}</td>
                  <td className="px-4 py-3 text-right">{formatCOP(item.subtotal)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-6 space-y-1 text-right">
          <p className="text-zinc-400">Subtotal: {formatCOP(invoice.subtotal)}</p>
          <p className="text-emerald-400">Descuento: -{formatCOP(invoice.discountAmount)}</p>
          <p className="font-display text-3xl text-gold">Total {formatCOP(invoice.totalAmount)}</p>
        </div>
      </div>
    </div>
  );
}
