export function HowToPlay({ onClose }: { onClose: () => void }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal card" onClick={(e) => e.stopPropagation()}>
        <h2>Cómo jugar 🎮</h2>
        <ol className="rules">
          <li>
            <strong>Arma tu oficina.</strong> Esconde tu avatar 🧑 y coloca tus objetos (escritorio,
            planta, cafetera…). Nadie ve dónde están.
          </li>
          <li>
            <strong>Por turnos, dispara.</strong> Verás <em>tu oficina</em> a la izquierda y la del{" "}
            <em>rival</em> (oculta) a la derecha. Haz clic en una celda del rival para atacar.
          </li>
          <li>
            <strong>💥 Destruye objetos</strong> para sumar puntos. <strong>🎯 Encuentra el avatar</strong>{" "}
            del rival para quitarle una vida (¡se esconde en otra celda al ser golpeado!).
          </li>
          <li>
            <strong>Sin vidas = eliminado.</strong> El último jugador en pie gana la partida.
          </li>
          <li>
            <strong>Juega cuando quieras.</strong> Es por turnos y asíncrono: haz tu jugada y vuelve
            más tarde.
          </li>
        </ol>
        <button className="btn primary" onClick={onClose}>
          ¡Entendido!
        </button>
      </div>
    </div>
  );
}
