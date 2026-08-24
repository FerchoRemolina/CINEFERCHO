export default function MovieCard({
  movie,
  onBuy,
  onOpen,
  buyLabel = "Comprar entradas",
  badge,
}) {
  const description = movie.description || movie.synopsis;
  const ageRating = movie.ageRating || movie.rating;
  const format = movie.format ? ` · ${movie.format}` : "";
  const canBuy = Boolean(onBuy) && movie.ticketsEnabled !== false;

  return (
    <article
      className={`group overflow-hidden rounded-2xl border border-white/10 bg-cinema-panel shadow-xl transition hover:-translate-y-1 hover:border-gold/50 ${
        onOpen ? "cursor-pointer" : ""
      }`}
      onClick={onOpen ? () => onOpen(movie) : undefined}
    >
      <div className="relative h-[380px] overflow-hidden bg-zinc-900">
        <img
          src={movie.posterUrl}
          alt={movie.title}
          className="h-full w-full object-cover transition duration-500 group-hover:scale-105"
          onError={(event) => {
            event.currentTarget.src =
              "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80";
          }}
        />
        <span className="absolute left-3 top-3 rounded-full bg-black/70 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-gold">
          {ageRating || "TBA"}
        </span>
        {badge && (
          <span className="absolute right-3 top-3 rounded-full bg-rose-600 px-3 py-1 text-[10px] font-bold uppercase tracking-wide text-white">
            {badge}
          </span>
        )}
      </div>
      <div className="space-y-3 p-5">
        <h3 className="font-display text-xl text-white">{movie.title}</h3>
        <p className="text-sm text-zinc-400">
          {movie.genre} · {movie.durationMinutes} min{format}
        </p>
        <p className="line-clamp-2 text-sm text-zinc-500">{description}</p>
        {canBuy && (
          <button
            type="button"
            onClick={(event) => {
              event.stopPropagation();
              onBuy(movie);
            }}
            className="w-full rounded-xl bg-rose-600 py-2.5 text-sm font-semibold text-white hover:bg-rose-500"
          >
            {buyLabel}
          </button>
        )}
      </div>
    </article>
  );
}
