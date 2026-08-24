import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from "react";
import {
  SESSION_EXPIRED_EVENT,
  SESSION_TIMEOUT_FLAG,
  TOKEN_KEY,
  authApi,
  catalogApi,
  clientApi,
  getApiError,
} from "../services/api.js";
import { isMembershipActive, readJwtExpiresAt } from "../utils/format.js";

const USER_KEY = "cinefercho_user";
const CITY_KEY = "cinefercho_city";
const THEATER_KEY = "cinefercho_theater";

const AuthContext = createContext(null);

function readJson(key) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function toSessionUser(data, previous = null) {
  return {
    id: data.id,
    email: data.email,
    fullName: data.fullName,
    nationalId: data.nationalId,
    phone: data.phone || "",
    role: data.role,
    membershipType: data.membershipType,
    membershipExpiresAt: data.membershipExpiresAt ?? previous?.membershipExpiresAt ?? null,
    membershipActive: data.membershipActive ?? previous?.membershipActive ?? false,
    sessionExpiresAt: data.sessionExpiresAt ?? previous?.sessionExpiresAt ?? readJwtExpiresAt(data.token) ?? null,
  };
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY));
  const [user, setUser] = useState(() => readJson(USER_KEY));
  const [cities, setCities] = useState([]);
  const [theaters, setTheaters] = useState([]);
  const [selectedCity, setSelectedCity] = useState(() => readJson(CITY_KEY));
  const [selectedTheater, setSelectedTheater] = useState(() => readJson(THEATER_KEY));
  const [authOpen, setAuthOpen] = useState(false);
  const [authTab, setAuthTab] = useState("login");
  const [authError, setAuthError] = useState("");
  const [authLoading, setAuthLoading] = useState(false);
  const [sessionNotice, setSessionNotice] = useState(() =>
    sessionStorage.getItem(SESSION_TIMEOUT_FLAG) ? "timeout" : "",
  );
  const afterAuthRef = useRef(null);

  const persistSession = useCallback((nextToken, nextUser) => {
    setToken(nextToken);
    setUser(nextUser);
    if (nextToken) localStorage.setItem(TOKEN_KEY, nextToken);
    else localStorage.removeItem(TOKEN_KEY);
    if (nextUser) localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    else localStorage.removeItem(USER_KEY);
  }, []);

  const applyAuthResponse = useCallback(
    (data) => {
      const nextUser = toSessionUser(data);
      persistSession(data.token, nextUser);
      return nextUser;
    },
    [persistSession],
  );

  const runAfterAuth = useCallback((nextUser) => {
    const callback = afterAuthRef.current;
    afterAuthRef.current = null;
    if (nextUser?.role !== "ROLE_ADMIN" && typeof callback === "function") {
      callback(nextUser);
    }
  }, []);

  const login = useCallback(
    async (email, password) => {
      setAuthLoading(true);
      setAuthError("");
      try {
        const { data } = await authApi.login({ email, password });
        const nextUser = applyAuthResponse(data);
        setAuthOpen(false);
        setSessionNotice("");
        sessionStorage.removeItem(SESSION_TIMEOUT_FLAG);
        runAfterAuth(nextUser);
        return nextUser;
      } catch (error) {
        setAuthError(getApiError(error, "Credenciales inválidas."));
        throw error;
      } finally {
        setAuthLoading(false);
      }
    },
    [applyAuthResponse, runAfterAuth],
  );

  const register = useCallback(
    async (fullName, email, password, nationalId) => {
      setAuthLoading(true);
      setAuthError("");
      try {
        const { data } = await authApi.register({ fullName, email, password, nationalId });
        const nextUser = applyAuthResponse(data);
        setAuthOpen(false);
        setSessionNotice("");
        sessionStorage.removeItem(SESSION_TIMEOUT_FLAG);
        runAfterAuth(nextUser);
        return nextUser;
      } catch (error) {
        setAuthError(getApiError(error, "No se pudo completar el registro."));
        throw error;
      } finally {
        setAuthLoading(false);
      }
    },
    [applyAuthResponse, runAfterAuth],
  );

  const updateProfile = useCallback(
    async (payload) => {
      const { data } = await clientApi.updateProfile(payload);
      const nextUser = toSessionUser(data, user);
      persistSession(token, nextUser);
      return nextUser;
    },
    [persistSession, token, user],
  );

  const refreshUser = useCallback(async () => {
    const { data } = await clientApi.me();
    let nextUser = null;
    setUser((current) => {
      nextUser = toSessionUser(data, current);
      localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
      return nextUser;
    });
    return nextUser;
  }, []);

  const logout = useCallback(() => {
    afterAuthRef.current = null;
    persistSession(null, null);
  }, [persistSession]);

  const expireSession = useCallback(
    (reason = "timeout") => {
      if (reason === "timeout") {
        sessionStorage.setItem(SESSION_TIMEOUT_FLAG, "1");
        setSessionNotice("timeout");
      }
      logout();
    },
    [logout],
  );

  const clearSessionNotice = useCallback(() => {
    sessionStorage.removeItem(SESSION_TIMEOUT_FLAG);
    setSessionNotice("");
  }, []);

  const openAuth = useCallback((tab = "login", onSuccess) => {
    setAuthTab(tab);
    setAuthError("");
    afterAuthRef.current = typeof onSuccess === "function" ? onSuccess : null;
    setAuthOpen(true);
  }, []);

  const closeAuth = useCallback(() => {
    setAuthOpen(false);
    setAuthError("");
    afterAuthRef.current = null;
  }, []);

  const selectCity = useCallback((city) => {
    setSelectedCity(city);
    setSelectedTheater(null);
    if (city) localStorage.setItem(CITY_KEY, JSON.stringify(city));
    else localStorage.removeItem(CITY_KEY);
    localStorage.removeItem(THEATER_KEY);
  }, []);

  const selectTheater = useCallback((theater) => {
    setSelectedTheater(theater);
    if (theater) localStorage.setItem(THEATER_KEY, JSON.stringify(theater));
    else localStorage.removeItem(THEATER_KEY);
  }, []);

  useEffect(() => {
    const onExpired = (event) => {
      expireSession(event.detail?.reason || "unauthorized");
    };
    window.addEventListener(SESSION_EXPIRED_EVENT, onExpired);
    return () => window.removeEventListener(SESSION_EXPIRED_EVENT, onExpired);
  }, [expireSession]);

  useEffect(() => {
    catalogApi
      .cities()
      .then(({ data }) => {
        setCities(data);
        if (!selectedCity && data.length > 0) {
          selectCity(data[0]);
        }
      })
      .catch(() => setCities([]));
  }, []);

  useEffect(() => {
    if (!selectedCity?.id) {
      setTheaters([]);
      return;
    }
    catalogApi
      .theatersByCity(selectedCity.id)
      .then(({ data }) => {
        setTheaters(data);
        const stillValid = data.some((theater) => theater.id === selectedTheater?.id);
        if (!stillValid) {
          selectTheater(data[0] || null);
        }
      })
      .catch(() => setTheaters([]));
  }, [selectedCity?.id]);

  const isAuthenticated = Boolean(token && user);
  const isAdmin = isAuthenticated && user?.role === "ROLE_ADMIN";
  const isClient = isAuthenticated && user?.role === "ROLE_CLIENT";
  const hasActiveMembership = isClient && isMembershipActive(user);

  const value = useMemo(
    () => ({
      token,
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
      login,
      register,
      updateProfile,
      refreshUser,
      logout,
      expireSession,
      sessionNotice,
      clearSessionNotice,
      authOpen,
      authTab,
      setAuthTab,
      authError,
      authLoading,
      openAuth,
      closeAuth,
    }),
    [
      token,
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
      login,
      register,
      updateProfile,
      refreshUser,
      logout,
      expireSession,
      sessionNotice,
      clearSessionNotice,
      authOpen,
      authTab,
      authError,
      authLoading,
      openAuth,
      closeAuth,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe usarse dentro de AuthProvider");
  }
  return context;
}
