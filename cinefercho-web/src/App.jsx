import { useEffect } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import AuthModal from "./components/AuthModal.jsx";
import Navbar from "./components/Navbar.jsx";
import { useAuth } from "./context/AuthContext.jsx";
import AdminView from "./views/AdminView.jsx";
import BookingView from "./views/BookingView.jsx";
import ConcessionsView from "./views/ConcessionsView.jsx";
import HomeView from "./views/HomeView.jsx";
import InvoiceView from "./views/InvoiceView.jsx";
import InvoicesView from "./views/InvoicesView.jsx";
import MembershipsView from "./views/MembershipsView.jsx";
import ProfileView from "./views/ProfileView.jsx";

function RequireAdmin({ children }) {
  const { isAuthenticated, isAdmin, openAuth } = useAuth();
  useEffect(() => {
    if (!isAuthenticated) {
      openAuth("login");
    }
  }, [isAuthenticated, openAuth]);
  if (!isAuthenticated) {
    return (
      <p className="pt-16 text-center text-zinc-400">
        Inicia sesión con la cuenta administradora para abrir el dashboard.
      </p>
    );
  }
  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }
  return children;
}

function RequireClient({ children }) {
  const { isClient, openAuth, isAuthenticated } = useAuth();
  useEffect(() => {
    if (!isAuthenticated) {
      openAuth("login");
    }
  }, [isAuthenticated, openAuth]);
  if (!isClient) {
    return (
      <p className="pt-16 text-center text-zinc-400">Esta sección es exclusiva para clientes autenticados.</p>
    );
  }
  return children;
}

function AdminGate() {
  const { isAdmin } = useAuth();
  const location = useLocation();
  if (isAdmin && !location.pathname.startsWith("/admin")) {
    return <Navigate to="/admin" replace />;
  }
  return null;
}

export default function App() {
  const { sessionNotice, clearSessionNotice } = useAuth();
  return (
    <div className="min-h-screen">
      <Navbar />
      <AuthModal />
      <AdminGate />
      {sessionNotice === "timeout" && (
        <div className="mx-auto max-w-[1400px] px-8 pt-4">
          <div className="flex items-center justify-between rounded-2xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
            <p>
              Se agotó el tiempo de sesión (2 minutos). El sistema canceló la compra en curso y cerró
              tu sesión para evitar simultaneidad elevada.
            </p>
            <button type="button" onClick={clearSessionNotice} className="ml-4 text-rose-100 underline">
              Entendido
            </button>
          </div>
        </div>
      )}
      <main className="mx-auto max-w-[1400px] px-8 pb-16">
        <Routes>
          <Route path="/" element={<HomeView />} />
          <Route path="/confiteria" element={<ConcessionsView />} />
          <Route path="/membresias" element={<MembershipsView />} />
          <Route path="/reservar/:movieId" element={<BookingView />} />
          <Route path="/factura/:invoiceNumber" element={<InvoiceView />} />
          <Route
            path="/facturas"
            element={
              <RequireClient>
                <InvoicesView />
              </RequireClient>
            }
          />
          <Route
            path="/cuenta"
            element={
              <RequireClient>
                <ProfileView />
              </RequireClient>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <AdminView />
              </RequireAdmin>
            }
          />
        </Routes>
      </main>
    </div>
  );
}
