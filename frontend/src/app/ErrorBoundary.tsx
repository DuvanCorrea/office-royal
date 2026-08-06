import { Component, type ErrorInfo, type ReactNode } from "react";

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

/**
 * Sin esto, cualquier excepción durante el render (p. ej. al desmontar la escena 3D al salir
 * de una partida) tumba TODO el árbol de React y deja la pantalla en negro sin ningún mensaje.
 */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("[ErrorBoundary]", error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <div className="home">
          <div className="home-card card">
            <div className="home-brand">
              Office <span>Wars</span> <span className="home-emoji">⚠️</span>
            </div>
            <p className="home-tag">Algo se rompió. Recarga la página para seguir jugando.</p>
            <p className="hint" style={{ wordBreak: "break-word" }}>
              {this.state.error.message}
            </p>
            <button className="btn primary big" onClick={() => window.location.reload()}>
              Recargar
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
