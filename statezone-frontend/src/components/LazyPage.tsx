import React, { Suspense, type ComponentType } from 'react';
import ErrorBoundary from './ErrorBoundary';

export function LazyPage({ Component }: { Component: React.LazyExoticComponent<ComponentType> }) {
  return (
    <Suspense
      fallback={
        <div className="flex items-center justify-center min-h-[50vh]">
          <div className="flex flex-col items-center gap-3">
            <div className="w-8 h-8 border-2 border-accent/30 border-t-accent rounded-full animate-spin" />
            <span className="text-xs text-slate-500">Carregando...</span>
          </div>
        </div>
      }
    >
      <ErrorBoundary
        fallback={
          <div className="flex flex-col items-center justify-center min-h-[50vh] text-center px-6">
            <h2 className="text-lg font-bold text-slate-100 mb-2">Erro ao carregar página</h2>
            <p className="text-sm text-slate-500 mb-4">
              Não foi possível carregar este módulo. Tente recarregar a página.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="px-5 py-2 bg-accent text-primary-dark rounded-lg text-sm font-semibold"
            >
              Recarregar
            </button>
          </div>
        }
      >
        <Component />
      </ErrorBoundary>
    </Suspense>
  );
}
