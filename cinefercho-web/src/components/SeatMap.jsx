export default function SeatMap({ seatMap, selectedIds, onToggle }) {
  const selected = new Set(selectedIds);

  if (!seatMap?.rows?.length) {
    return (
      <div className="rounded-2xl border border-white/10 bg-cinema-panel p-10 text-center text-zinc-400">
        Selecciona una función para ver el mapa de asientos.
      </div>
    );
  }

  return (
    <div className="rounded-2xl border border-white/10 bg-cinema-panel p-8">
      <div className="mx-auto mb-8 max-w-xl">
        <div className="screen-curve" />
        <p className="mt-3 text-center text-xs uppercase tracking-[0.4em] text-zinc-500">Pantalla</p>
      </div>
      <div className="mx-auto w-fit space-y-3">
        {seatMap.rows.map((row) => (
          <div key={row.rowLetter} className="flex items-center gap-4">
            <span className="w-6 text-center text-sm font-semibold text-gold">{row.rowLetter}</span>
            <div className="flex gap-2">
              {row.seats.map((seat) => {
                const occupied = seat.availability === "ocupado";
                const isSelected = selected.has(seat.id);
                let classes = "h-10 w-10 rounded-md text-xs font-bold transition";
                if (occupied) {
                  classes += " cursor-not-allowed bg-seat-taken text-white";
                } else if (isSelected) {
                  classes += " bg-seat-pick text-white shadow-lg shadow-emerald-500/30";
                } else {
                  classes += " bg-seat-free text-zinc-900 hover:brightness-110";
                }
                if (seat.vip && !occupied) {
                  classes += " ring-2 ring-gold";
                }
                return (
                  <button
                    key={seat.id}
                    type="button"
                    disabled={occupied}
                    title={`${seat.rowLetter}${seat.seatNumber}${seat.vip ? " VIP" : ""}`}
                    className={classes}
                    onClick={() => onToggle(seat)}
                  >
                    {seat.seatNumber}
                  </button>
                );
              })}
            </div>
          </div>
        ))}
      </div>
      <div className="mt-8 flex justify-center gap-6 text-xs text-zinc-400">
        <Legend color="bg-seat-free" label="Libre" />
        <Legend color="bg-seat-taken" label="Ocupado" />
        <Legend color="bg-seat-pick" label="Seleccionado" />
        <Legend color="bg-seat-free ring-2 ring-gold" label="VIP" />
      </div>
    </div>
  );
}

function Legend({ color, label }) {
  return (
    <span className="flex items-center gap-2">
      <span className={`h-4 w-4 rounded ${color}`} />
      {label}
    </span>
  );
}
