import axios from "axios";

const TOKEN_KEY = "cinefercho_token";

export const api = axios.create({
  baseURL: "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

export const SESSION_EXPIRED_EVENT = "cinefercho:session-expired";
export const SESSION_TIMEOUT_FLAG = "cinefercho_session_timeout";

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const path = error.config?.url || "";
      if (!path.includes("/auth/login") && !path.includes("/auth/register")) {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem("cinefercho_user");
        window.dispatchEvent(new CustomEvent(SESSION_EXPIRED_EVENT, { detail: { reason: "unauthorized" } }));
      }
    }
    return Promise.reject(error);
  },
);

export function getApiError(error, fallback = "Ha ocurrido un error.") {
  return error.response?.data?.message || error.message || fallback;
}

export const catalogApi = {
  cities: () => api.get("/public/cities"),
  theatersByCity: (cityId) => api.get(`/public/cities/${cityId}/theaters`),
  movies: (status) => api.get("/public/movies", { params: status ? { status } : {} }),
  nowShowing: (theaterId) => api.get("/public/movies/now-showing", { params: theaterId ? { theaterId } : {} }),
  presale: (theaterId) => api.get("/public/movies/presale", { params: theaterId ? { theaterId } : {} }),
  upcoming: (theaterId) => api.get("/public/movies/upcoming", { params: theaterId ? { theaterId } : {} }),
  screenings: (movieId, theaterId, date) =>
    api.get("/public/screenings", { params: { movieId, theaterId, date } }),
  seatMap: (screeningId) => api.get(`/public/screenings/${screeningId}/seats`),
  products: () => api.get("/public/products"),
  memberships: () => api.get("/public/memberships"),
};

export const authApi = {
  login: (payload) => api.post("/auth/login", payload),
  register: (payload) => api.post("/auth/register", payload),
};

export const clientApi = {
  me: () => api.get("/client/me"),
  updateProfile: (payload) => api.put("/client/profile", payload),
  purchase: (payload) => api.post("/client/purchases", payload),
  invoices: () => api.get("/client/invoices"),
};

export const adminApi = {
  movies: () => api.get("/admin/movies"),
  createMovie: (payload) => api.post("/admin/movies", payload),
  updateMovie: (id, payload) => api.put(`/admin/movies/${id}`, payload),
  deleteMovie: (id) => api.delete(`/admin/movies/${id}`),
  screenings: () => api.get("/admin/screenings"),
  createScreening: (payload) => api.post("/admin/screenings", payload),
  createRecurringScreenings: (payload) => api.post("/admin/screenings/recurring", payload),
  deleteScreening: (id) => api.delete(`/admin/screenings/${id}`),
  halls: () => api.get("/admin/halls"),
  theaters: () => api.get("/admin/theaters"),
  products: () => api.get("/admin/products"),
  createProduct: (payload) => api.post("/admin/products", payload),
  updateProduct: (id, payload) => api.put(`/admin/products/${id}`, payload),
  deleteProduct: (id) => api.delete(`/admin/products/${id}`),
};

export { TOKEN_KEY };
