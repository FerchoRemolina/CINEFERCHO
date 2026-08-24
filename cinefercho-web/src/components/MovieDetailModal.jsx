import { formatDate } from "../utils/format.js";

export default function MovieDetailModal({ movie, onClose }) {
  if (!movie) return null;

  const halls = movie.halls || [];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-6 backdrop-blur-sm">
      <div className="grid max-h-[90vh] w-full max-w-4xl overflow-hidden rounded-3xl border border-white/10 bg-cinema-panel shadow-2xl md:grid-cols-[0.9fr_1.1fr]">
        <img
          src={movie.posterUrl}
          alt={movie.title}
          className="h-full min-h-[420px] w-full object-cover"
        />
        <div className="overflow-y-auto p-8">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs uppercase tracking-[0.3em] text-gold">Descripción-Película</p>
              <h2 className="font-display mt-2 text-3xl text-white">{movie.title}</h2>
            </div>
            <button type="button" onClick={onClose} className="text-zinc-400 hover:text-white">
              ✕
            </button>
          </div>
          <p className="mt-4 text-sm leading-relaxed text-zinc-300">{movie.description}</p>
          <dl className="mt-6 grid grid-cols-2 gap-4 text-sm">
            <Info label="Clasificación" value={movie.ageRating} />
            <Info label="Formato" value={movie.format} />
            <Info label="Estreno" value={formatDate(movie.releaseDate)} />
            <Info label="Duración" value={movie.durationMinutes ? `${movie.durationMinutes} min` : ""} />
            <Info label="Género" value={movie.genre} />
            <Info label="Preventa" value={movie.ticketsEnabled ? "Habilitada" : "Aún no disponible"} />
          </dl>
          <div className="mt-6">
            <p className="text-xs uppercase tracking-[0.25em] text-zinc-500">Salas asociadas</p>
            {halls.length === 0 ? (
              <p className="mt-2 text-sm text-zinc-400">Todavía no hay salas asignadas.</p>
            ) : (
              <ul className="mt-3 space-y-2 text-sm text-zinc-300">
                {halls.map((hall) => (
                  <li key={hall.id} className="rounded-xl border border-white/10 px-3 py-2">
                    {hall.theaterName} · {hall.name} · {hall.hallType}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div>
      <dt className="text-zinc-500">{label}</dt>
      <dd className="font-medium text-white">{value || "TBA"}</dd>
    </div>
  );
}
