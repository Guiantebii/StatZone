import { Component, type ReactNode } from 'react';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-primary-dark flex items-center justify-center">
          <div className="text-center max-w-md px-6">
            <div className="w-16 h-16 rounded-2xl bg-danger/10 flex items-center justify-center mx-auto mb-4">
              <span className="text-2xl" role="img" aria-label="Alerta de erro">⚠️</span>
            </div>
            <h1 className="text-xl font-bold text-slate-100 mb-2">Algo deu errado</h1>
            <p className="text-sm text-slate-500 mb-6">Ocorreu um erro inesperado. Tente recarregar a página.</p>
            <button
              onClick={() => window.location.reload()}
              className="px-6 py-2.5 bg-accent text-primary-dark rounded-lg text-sm font-semibold hover:bg-accent-hover transition-colors"
            >
              Recarregar
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
