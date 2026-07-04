import React, { lazy, Suspense, type ComponentType } from 'react';
import ErrorBoundary from './ErrorBoundary';

export const LoginPage = lazy(() => import('../pages/LoginPage'));
export const RegisterPage = lazy(() => import('../pages/RegisterPage'));
export const DashboardPage = lazy(() => import('../pages/DashboardPage'));
export const CampeonatosPage = lazy(() => import('../pages/CampeonatosPage'));
export const TimesPage = lazy(() => import('../pages/TimesPage'));
export const TimeDetalhePage = lazy(() => import('../pages/TimeDetalhePage'));
export const JogadoresPage = lazy(() => import('../pages/JogadoresPage'));
export const PartidasPage = lazy(() => import('../pages/PartidasPage'));
export const PartidaDetalhePage = lazy(() => import('../pages/PartidaDetalhePage'));
export const EstatisticasPage = lazy(() => import('../pages/EstatisticasPage'));
export const FasesPage = lazy(() => import('../pages/FasesPage'));
export const ImportacaoPage = lazy(() => import('../pages/ImportacaoPage'));
export const PublicHomePage = lazy(() => import('../pages/PublicHomePage'));
export const PublicCampeonatosPage = lazy(() => import('../pages/PublicCampeonatosPage'));
export const PublicTimesPage = lazy(() => import('../pages/PublicTimesPage'));
export const CampeonatoDetalhePage = lazy(() => import('../pages/CampeonatoDetalhePage'));
export const JogadorDetalhePage = lazy(() => import('../pages/JogadorDetalhePage'));
export const NotFoundPage = lazy(() => import('../pages/NotFoundPage'));

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
