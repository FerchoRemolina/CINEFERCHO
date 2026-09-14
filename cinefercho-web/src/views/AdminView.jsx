import { useEffect, useMemo, useState } from "react";
import { adminApi, getApiError } from "../services/api.js";
import { formatCOP, formatDateTime } from "../utils/format.js";

const SECTIONS = [
  { id: "movies", label: "Descripción-Película" },
  { id: "screenings", label: "Funciones" },
  { id: "products", label: "Inventario confitería" },
];

const emptyMovie = {
  title: "",
  description: "",
  posterUrl: "",
  ageRating: "PG-13",
  format: "2D",
  releaseDate: "",
  durationMinutes: 120,
  genre: "",
  status: "COMING_SOON",
};

const emptyScreening = {
  movieId: "",
  hallId: "",
  startTime: "",
  startDate: "",
  endDate: "",
  time: "18:00",
  repeat: false,
  ticketPrice: "18000",
  format: "STANDARD_2D",
};

function datesInclusive(from, to) {
  if (!from || !to || from > to) return [];
  const days = [];
  const cursor = new Date(`${from}T00:00:00`);
  const last = new Date(`${to}T00:00:00`);
  while (cursor <= last) {
    const year = cursor.getFullYear();
    const month = String(cursor.getMonth() + 1).padStart(2, "0");
    const day = String(cursor.getDate()).padStart(2, "0");
    days.push(`${year}-${month}-${day}`);
    cursor.setDate(cursor.getDate() + 1);
  }
  return days;
}

function addDays(isoDate, amount) {
  const date = new Date(`${isoDate}T00:00:00`);
  date.setDate(date.getDate() + amount);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

const emptyProduct = {
  name: "",
  description: "",
  price: "8000",
  category: "POPCORN",
  stock: 50,
  imageUrl: "",
};

export default function AdminView() {
  const [section, setSection] = useState("movies");
  const [movies, setMovies] = useState([]);
  const [screenings, setScreenings] = useState([]);
  const [halls, setHalls] = useState([]);
  const [products, setProducts] = useState([]);
  const [movieForm, setMovieForm] = useState(emptyMovie);
  const [screeningForm, setScreeningForm] = useState(emptyScreening);
  const [productForm, setProductForm] = useState(emptyProduct);
  const [editingMovieId, setEditingMovieId] = useState(null);
  const [editingProductId, setEditingProductId] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const [tmdbQuery, setTmdbQuery] = useState("");
  const [tmdbResults, setTmdbResults] = useState([]);
  const [tmdbEnabled, setTmdbEnabled] = useState(true);
  const [tmdbSearching, setTmdbSearching] = useState(false);
  const [tmdbOpen, setTmdbOpen] = useState(false);

  const load = async () => {
    const [{ data: movieData }, { data: screeningData }, { data: hallData }, { data: productData }] =
      await Promise.all([
        adminApi.movies(),
        adminApi.screenings(),
        adminApi.halls(),
        adminApi.products(),
      ]);
    setMovies(movieData);
    setScreenings(screeningData);
    setHalls(hallData);
    setProducts(productData);
  };

  useEffect(() => {
    load().catch((err) => setError(getApiError(err, "No se pudo cargar el dashboard.")));
  }, []);

  const flash = (text, isError = false) => {
    setError(isError ? text : "");
    setMessage(isError ? "" : text);
  };

  const searchTmdb = async (event) => {
    event.preventDefault();
    const query = tmdbQuery.trim();
    if (!query) {
      flash("Escribe un título para buscar en TMDB.", true);
      return;
    }
    setTmdbSearching(true);
    try {
      const { data } = await adminApi.searchTmdb(query);
      setTmdbEnabled(data.enabled !== false);
      setTmdbResults(data.results || []);
      setTmdbOpen(true);
      if (data.enabled === false) {
        flash("TMDB no está configurado. Puedes crear la película a mano.", true);
      } else if (!(data.results || []).length) {
        flash("TMDB no devolvió coincidencias. Completa el formulario manualmente.", true);
      } else {
        flash("");
      }
    } catch {
      setTmdbResults([]);
      setTmdbOpen(false);
      flash("No se pudo consultar TMDB. Crea la película a mano.", true);
    } finally {
      setTmdbSearching(false);
    }
  };

  const applyTmdbMovie = (movie) => {
    setMovieForm((current) => ({
      ...current,
      title: movie.title || current.title,
      description: movie.overview || current.description,
      posterUrl: movie.posterUrl || current.posterUrl,
      releaseDate: movie.releaseDate || current.releaseDate,
      ageRating: current.ageRating || "PG-13",
      durationMinutes: current.durationMinutes || 120,
    }));
    setTmdbOpen(false);
    flash(`Datos de “${movie.title}” cargados. Revisa formato y fecha de estreno.`);
  };

  const submitMovie = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...movieForm,
        durationMinutes: Number(movieForm.durationMinutes),
      };
      if (editingMovieId) {
        await adminApi.updateMovie(editingMovieId, payload);
        flash("Película actualizada.");
      } else {
        await adminApi.createMovie(payload);
        flash("Descripción-Película creada.");
      }
      setMovieForm(emptyMovie);
      setEditingMovieId(null);
      await load();
    } catch (err) {
      flash(getApiError(err, "No se pudo guardar la película."), true);
    } finally {
      setSaving(false);
    }
  };

  const submitScreening = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      if (screeningForm.repeat) {
        const dates = datesInclusive(screeningForm.startDate, screeningForm.endDate);
        if (dates.length === 0) {
          flash("Selecciona un rango de fechas válido (desde / hasta).", true);
          return;
        }
        if (dates.length > 31) {
          flash("El rango no puede superar 31 días.", true);
          return;
        }
        const time = screeningForm.time.length === 5 ? `${screeningForm.time}:00` : screeningForm.time;
        const { data } = await adminApi.createRecurringScreenings({
          movieId: Number(screeningForm.movieId),
          hallId: Number(screeningForm.hallId),
          startTime: time,
          dates,
          ticketPrice: Number(screeningForm.ticketPrice),
          format: screeningForm.format,
        });
        flash(
          data.created === 1
            ? "Se programó 1 función."
            : `Se programaron ${data.created} funciones a la misma hora.`,
        );
      } else {
        const startTime =
          screeningForm.startTime.length === 16 ? `${screeningForm.startTime}:00` : screeningForm.startTime;
        await adminApi.createScreening({
          movieId: Number(screeningForm.movieId),
          hallId: Number(screeningForm.hallId),
          startTime,
          ticketPrice: Number(screeningForm.ticketPrice),
          format: screeningForm.format,
        });
        flash("Función programada.");
      }
      setScreeningForm(emptyScreening);
      await load();
    } catch (err) {
      flash(getApiError(err, "No se pudo programar la función."), true);
    } finally {
      setSaving(false);
    }
  };

  const submitProduct = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...productForm,
        price: Number(productForm.price),
        stock: Number(productForm.stock),
      };
      if (editingProductId) {
        await adminApi.updateProduct(editingProductId, payload);
        flash("Producto actualizado.");
      } else {
        await adminApi.createProduct(payload);
        flash("Producto creado.");
      }
      setProductForm(emptyProduct);
      setEditingProductId(null);
      await load();
    } catch (err) {
      flash(getApiError(err, "No se pudo guardar el producto."), true);
    } finally {
      setSaving(false);
    }
  };

  const hallsByTheater = useMemo(() => {
    const groups = {};
    halls.forEach((hall) => {
      const key = hall.theaterName || "Teatro";
      groups[key] = groups[key] || [];
      groups[key].push(hall);
    });
    return groups;
  }, [halls]);

  return (
    <div className="space-y-8 pt-8">
      <div>
        <p className="text-sm uppercase tracking-[0.3em] text-gold">Administración</p>
        <h1 className="font-display text-4xl">Dashboard</h1>
        <p className="text-zinc-400">Gestiona películas, horarios e inventario de confitería.</p>
      </div>

      <div className="flex rounded-2xl bg-cinema-panel p-1">
        {SECTIONS.map((item) => (
          <button
            key={item.id}
            type="button"
            onClick={() => {
              setSection(item.id);
              setError("");
              setMessage("");
            }}
            className={`flex-1 rounded-xl px-4 py-3 text-sm font-semibold ${
              section === item.id ? "bg-gold text-cinema" : "text-zinc-400"
            }`}
          >
            {item.label}
          </button>
        ))}
      </div>

      {error && (
        <div className="rounded-xl border border-rose-500/40 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
          {error}
        </div>
      )}
      {message && (
        <div className="rounded-xl border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-200">
          {message}
        </div>
      )}

      {section === "movies" && (
        <div className="grid grid-cols-[0.9fr_1.1fr] gap-8">
          <div className="space-y-4">
            <form
              onSubmit={searchTmdb}
              className="space-y-3 rounded-2xl border border-gold/30 bg-cinema-panel p-6"
            >
              <h2 className="font-display text-xl">🔍 Autocompletar con TMDB</h2>
              <p className="text-xs text-zinc-400">
                Busca un título para rellenar sinopsis y póster. Sala, formato de función y precio se
                asignan al programar la función.
              </p>
              <div className="flex gap-2">
                <input
                  value={tmdbQuery}
                  onChange={(event) => setTmdbQuery(event.target.value)}
                  placeholder="Ej. Dune, Intensamente..."
                  className="flex-1 rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
                />
                <button
                  type="submit"
                  disabled={tmdbSearching}
                  className="rounded-xl bg-gold px-4 py-2 font-semibold text-cinema disabled:opacity-60"
                >
                  {tmdbSearching ? "Buscando..." : "Buscar"}
                </button>
              </div>
              {!tmdbEnabled && (
                <p className="text-xs text-amber-300">
                  La API key de TMDB no está configurada. El formulario manual sigue disponible.
                </p>
              )}
              {tmdbOpen && tmdbResults.length > 0 && (
                <ul className="max-h-72 space-y-2 overflow-y-auto rounded-xl border border-white/10 p-2">
                  {tmdbResults.map((movie) => (
                    <li key={movie.id}>
                      <button
                        type="button"
                        onClick={() => applyTmdbMovie(movie)}
                        className="flex w-full items-center gap-3 rounded-lg px-2 py-2 text-left hover:bg-white/5"
                      >
                        {movie.posterUrl ? (
                          <img
                            src={movie.posterUrl}
                            alt=""
                            className="h-16 w-11 rounded object-cover"
                          />
                        ) : (
                          <span className="flex h-16 w-11 items-center justify-center rounded bg-cinema text-xs text-zinc-500">
                            N/A
                          </span>
                        )}
                        <span>
                          <span className="block font-semibold">{movie.title}</span>
                          <span className="text-xs text-zinc-400">
                            {(movie.releaseDate || "").slice(0, 4) || "Sin año"}
                            {movie.voteAverage != null ? ` · ${Number(movie.voteAverage).toFixed(1)}` : ""}
                          </span>
                        </span>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </form>
          <form onSubmit={submitMovie} className="space-y-3 rounded-2xl border border-white/10 bg-cinema-panel p-6">
            <h2 className="font-display text-2xl">
              {editingMovieId ? "Editar película" : "Nueva Descripción-Película"}
            </h2>
            <Field label="Título" value={movieForm.title} onChange={(title) => setMovieForm({ ...movieForm, title })} required />
            <Field
              label="URL del póster"
              value={movieForm.posterUrl}
              onChange={(posterUrl) => setMovieForm({ ...movieForm, posterUrl })}
            />
            <label className="block text-sm text-zinc-300">
              Sinopsis
              <textarea
                required
                value={movieForm.description}
                onChange={(event) => setMovieForm({ ...movieForm, description: event.target.value })}
                className="mt-1 h-28 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
              />
            </label>
            <div className="grid grid-cols-2 gap-3">
              <label className="block text-sm text-zinc-300">
                Edad
                <select
                  value={movieForm.ageRating}
                  onChange={(event) => setMovieForm({ ...movieForm, ageRating: event.target.value })}
                  className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
                >
                  {["G", "PG", "PG-13", "R", "NC-17"].map((rating) => (
                    <option key={rating} value={rating} className="text-zinc-900">
                      {rating}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm text-zinc-300">
                Formato
                <select
                  value={movieForm.format}
                  onChange={(event) => setMovieForm({ ...movieForm, format: event.target.value })}
                  className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
                >
                  {["2D", "3D", "XD"].map((format) => (
                    <option key={format} value={format} className="text-zinc-900">
                      {format}
                    </option>
                  ))}
                </select>
              </label>
              <Field
                label="Estreno"
                type="date"
                value={movieForm.releaseDate}
                onChange={(releaseDate) => setMovieForm({ ...movieForm, releaseDate })}
                required
              />
              <Field
                label="Duración (min)"
                type="number"
                value={movieForm.durationMinutes}
                onChange={(durationMinutes) => setMovieForm({ ...movieForm, durationMinutes })}
                required
              />
            </div>
            <Field label="Género" value={movieForm.genre} onChange={(genre) => setMovieForm({ ...movieForm, genre })} />
            <button disabled={saving} className="w-full rounded-xl bg-gold py-3 font-semibold text-cinema">
              {saving ? "Guardando..." : editingMovieId ? "Actualizar" : "Crear película"}
            </button>
            {editingMovieId && (
              <button
                type="button"
                onClick={() => {
                  setEditingMovieId(null);
                  setMovieForm(emptyMovie);
                }}
                className="w-full text-sm text-zinc-400"
              >
                Cancelar edición
              </button>
            )}
          </form>
          </div>
          <div className="space-y-3">
            {movies.map((movie) => (
              <article key={movie.id} className="flex gap-4 rounded-2xl border border-white/10 bg-cinema-panel p-4">
                <img src={movie.posterUrl} alt="" className="h-24 w-16 rounded object-cover" />
                <div className="flex-1">
                  <p className="font-semibold">{movie.title}</p>
                  <p className="text-xs text-zinc-400">
                    {movie.ageRating} · {movie.format} · {movie.releaseDate}
                  </p>
                  <p className="mt-1 line-clamp-2 text-sm text-zinc-500">{movie.description}</p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setEditingMovieId(movie.id);
                    setMovieForm({
                      title: movie.title || "",
                      description: movie.description || "",
                      posterUrl: movie.posterUrl || "",
                      ageRating: movie.ageRating || "PG-13",
                      format: movie.format || "2D",
                      releaseDate: movie.releaseDate || "",
                      durationMinutes: movie.durationMinutes || 120,
                      genre: movie.genre || "",
                      status: movie.status || "COMING_SOON",
                    });
                    setSection("movies");
                  }}
                  className="text-sm text-gold"
                >
                  Editar
                </button>
              </article>
            ))}
          </div>
        </div>
      )}

      {section === "screenings" && (
        <div className="grid grid-cols-[0.9fr_1.1fr] gap-8">
          <form onSubmit={submitScreening} className="space-y-3 rounded-2xl border border-white/10 bg-cinema-panel p-6">
            <h2 className="font-display text-2xl">Programar función</h2>
            <label className="block text-sm text-zinc-300">
              Película
              <select
                required
                value={screeningForm.movieId}
                onChange={(event) => setScreeningForm({ ...screeningForm, movieId: event.target.value })}
                className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
              >
                <option value="">Selecciona</option>
                {movies.map((movie) => (
                  <option key={movie.id} value={movie.id} className="text-zinc-900">
                    {movie.title}
                  </option>
                ))}
              </select>
            </label>
            <label className="block text-sm text-zinc-300">
              Sala
              <select
                required
                value={screeningForm.hallId}
                onChange={(event) => setScreeningForm({ ...screeningForm, hallId: event.target.value })}
                className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
              >
                <option value="">Selecciona</option>
                {Object.entries(hallsByTheater).map(([theater, items]) => (
                  <optgroup key={theater} label={theater}>
                    {items.map((hall) => (
                      <option key={hall.id} value={hall.id} className="text-zinc-900">
                        {hall.name} · {hall.hallType}
                      </option>
                    ))}
                  </optgroup>
                ))}
              </select>
            </label>
            <label className="flex items-start gap-3 rounded-xl border border-white/10 bg-cinema px-3 py-3 text-sm text-zinc-300">
              <input
                type="checkbox"
                checked={screeningForm.repeat}
                onChange={(event) =>
                  setScreeningForm({
                    ...screeningForm,
                    repeat: event.target.checked,
                    startDate: event.target.checked ? screeningForm.startDate : "",
                    endDate: event.target.checked ? screeningForm.endDate : "",
                  })
                }
                className="mt-0.5"
              />
              <span>
                Programar varios días a la misma hora
                <span className="mt-1 block text-xs text-zinc-500">
                  Crea una función diaria en la misma sala. Puede ser 1, 2 o más días seguidos, no
                  tienen que ser 10.
                </span>
              </span>
            </label>
            {screeningForm.repeat ? (
              <>
                <div className="grid grid-cols-2 gap-3">
                  <Field
                    label="Desde"
                    type="date"
                    value={screeningForm.startDate}
                    onChange={(startDate) =>
                      setScreeningForm({
                        ...screeningForm,
                        startDate,
                        endDate:
                          screeningForm.endDate && screeningForm.endDate < startDate
                            ? startDate
                            : screeningForm.endDate,
                      })
                    }
                    required
                  />
                  <Field
                    label="Hasta"
                    type="date"
                    value={screeningForm.endDate}
                    onChange={(endDate) => setScreeningForm({ ...screeningForm, endDate })}
                    required
                  />
                </div>
                <Field
                  label="Hora"
                  type="time"
                  value={screeningForm.time}
                  onChange={(time) => setScreeningForm({ ...screeningForm, time })}
                  required
                />
                <button
                  type="button"
                  disabled={!screeningForm.startDate}
                  onClick={() =>
                    setScreeningForm({
                      ...screeningForm,
                      endDate: addDays(screeningForm.startDate, 9),
                    })
                  }
                  className="text-left text-xs text-gold hover:underline disabled:text-zinc-500"
                >
                  Completar 10 días seguidos desde la fecha inicial
                </button>
                {datesInclusive(screeningForm.startDate, screeningForm.endDate).length > 0 && (
                  <p className="text-sm text-zinc-400">
                    Se crearán {datesInclusive(screeningForm.startDate, screeningForm.endDate).length}{" "}
                    función(es) a las {screeningForm.time || "--:--"}.
                  </p>
                )}
              </>
            ) : (
              <Field
                label="Horario"
                type="datetime-local"
                value={screeningForm.startTime}
                onChange={(startTime) => setScreeningForm({ ...screeningForm, startTime })}
                required
              />
            )}
            <div className="grid grid-cols-2 gap-3">
              <Field
                label="Precio"
                type="number"
                value={screeningForm.ticketPrice}
                onChange={(ticketPrice) => setScreeningForm({ ...screeningForm, ticketPrice })}
                required
              />
              <label className="block text-sm text-zinc-300">
                Formato función
                <select
                  value={screeningForm.format}
                  onChange={(event) => setScreeningForm({ ...screeningForm, format: event.target.value })}
                  className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
                >
                  {["STANDARD_2D", "STANDARD_3D", "XD_2D", "XD_3D", "D_BOX"].map((format) => (
                    <option key={format} value={format} className="text-zinc-900">
                      {format}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            <button disabled={saving} className="w-full rounded-xl bg-gold py-3 font-semibold text-cinema">
              {saving
                ? "Programando..."
                : screeningForm.repeat
                  ? "Crear funciones"
                  : "Crear función"}
            </button>
          </form>
          <div className="space-y-3">
            {screenings.map((item) => (
              <article key={item.id} className="rounded-2xl border border-white/10 bg-cinema-panel p-4">
                <p className="font-semibold">{item.movie?.title}</p>
                <p className="text-sm text-zinc-400">
                  {item.theaterName} · {item.hallName} · {formatDateTime(item.startTime)}
                </p>
                <p className="text-sm text-gold">{formatCOP(item.ticketPrice)}</p>
                <button
                  type="button"
                  onClick={async () => {
                    if (!window.confirm("¿Eliminar esta función?")) return;
                    try {
                      await adminApi.deleteScreening(item.id);
                      flash("Función eliminada.");
                      await load();
                    } catch (err) {
                      flash(getApiError(err, "No se pudo eliminar."), true);
                    }
                  }}
                  className="mt-2 text-xs text-rose-400"
                >
                  Eliminar
                </button>
              </article>
            ))}
          </div>
        </div>
      )}

      {section === "products" && (
        <div className="grid grid-cols-[0.9fr_1.1fr] gap-8">
          <form onSubmit={submitProduct} className="space-y-3 rounded-2xl border border-white/10 bg-cinema-panel p-6">
            <h2 className="font-display text-2xl">
              {editingProductId ? "Editar producto" : "Nuevo producto"}
            </h2>
            <Field label="Nombre" value={productForm.name} onChange={(name) => setProductForm({ ...productForm, name })} required />
            <Field
              label="Descripción"
              value={productForm.description}
              onChange={(description) => setProductForm({ ...productForm, description })}
            />
            <div className="grid grid-cols-2 gap-3">
              <Field
                label="Precio"
                type="number"
                value={productForm.price}
                onChange={(price) => setProductForm({ ...productForm, price })}
                required
              />
              <Field
                label="Stock"
                type="number"
                value={productForm.stock}
                onChange={(stock) => setProductForm({ ...productForm, stock })}
                required
              />
            </div>
            <label className="block text-sm text-zinc-300">
              Categoría
              <select
                value={productForm.category}
                onChange={(event) => setProductForm({ ...productForm, category: event.target.value })}
                className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2"
              >
                {["POPCORN", "BEVERAGE", "COMBO", "CANDY"].map((category) => (
                  <option key={category} value={category} className="text-zinc-900">
                    {category}
                  </option>
                ))}
              </select>
            </label>
            <Field
              label="Imagen URL"
              value={productForm.imageUrl}
              onChange={(imageUrl) => setProductForm({ ...productForm, imageUrl })}
            />
            <button disabled={saving} className="w-full rounded-xl bg-gold py-3 font-semibold text-cinema">
              {saving ? "Guardando..." : editingProductId ? "Actualizar inventario" : "Crear producto"}
            </button>
          </form>
          <div className="space-y-3">
            {products.map((product) => (
              <article key={product.id} className="flex items-center justify-between rounded-2xl border border-white/10 bg-cinema-panel p-4">
                <div>
                  <p className="font-semibold">{product.name}</p>
                  <p className="text-sm text-zinc-400">
                    {formatCOP(product.price)} · stock {product.stock}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setEditingProductId(product.id);
                    setProductForm({
                      name: product.name,
                      description: product.description || "",
                      price: product.price,
                      category: product.category,
                      stock: product.stock,
                      imageUrl: product.imageUrl || "",
                    });
                  }}
                  className="text-sm text-gold"
                >
                  Editar
                </button>
              </article>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function Field({ label, value, onChange, type = "text", required = false }) {
  return (
    <label className="block text-sm text-zinc-300">
      {label}
      <input
        required={required}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="mt-1 w-full rounded-xl border border-white/10 bg-cinema px-3 py-2 outline-none focus:border-gold"
      />
    </label>
  );
}
