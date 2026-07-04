/* eslint-disable react-refresh/only-export-components */
import { createBrowserRouter } from 'react-router-dom';
import React, { lazy, Suspense, type ComponentType } from 'react';
import Layout from './components/Layout';
import PublicLayout from './components/PublicLayout';
import ProtectedRoute from './components/ProtectedRoute';

const LoginPage = lazy(() => import('./pages/LoginPage'));
const RegisterPage = lazy(() => import('./pages/RegisterPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const CampeonatosPage = lazy(() => import('./pages/CampeonatosPage'));
const TimesPage = lazy(() => import('./pages/TimesPage'));
const TimeDetalhePage = lazy(() => import('./pages/TimeDetalhePage'));
const JogadoresPage = lazy(() => import('./pages/JogadoresPage'));
const PartidasPage = lazy(() => import('./pages/PartidasPage'));
const PartidaDetalhePage = lazy(() => import('./pages/PartidaDetalhePage'));
const EstatisticasPage = lazy(() => import('./pages/EstatisticasPage'));
const FasesPage = lazy(() => import('./pages/FasesPage'));
const ImportacaoPage = lazy(() => import('./pages/ImportacaoPage'));
const PublicHomePage = lazy(() => import('./pages/PublicHomePage'));
const PublicCampeonatosPage = lazy(() => import('./pages/PublicCampeonatosPage'));
const PublicTimesPage = lazy(() => import('./pages/PublicTimesPage'));
const CampeonatoDetalhePage = lazy(() => import('./pages/CampeonatoDetalhePage'));
const JogadorDetalhePage = lazy(() => import('./pages/JogadorDetalhePage'));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'));

function LazyPage({ Component }: { Component: React.LazyExoticComponent<ComponentType> }) {
  return (
    <Suspense fallback={
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-accent/30 border-t-accent rounded-full animate-spin" />
          <span className="text-xs text-slate-500">Carregando...</span>
        </div>
      </div>
    }>
      <ErrorBoundary fallback={
        <div className="flex flex-col items-center justify-center min-h-[50vh] text-center px-6">
          <h2 className="text-lg font-bold text-slate-100 mb-2">Erro ao carregar página</h2>
          <p className="text-sm text-slate-500 mb-4">N&atilde;o foi poss&iacute;vel carregar este m&oacute;dulo. Tente recarregar a p&aacute;gina.</p>
          <button
            onClick={() => window.location.reload()}
            className="px-5 py-2 bg-accent text-primary-dark rounded-lg text-sm font-semibold"
          >
            Recarregar
          </button>
        </div>
      }>
        <Component />
      </ErrorBoundary>
    </Suspense>
  );
}

class ErrorBoundary extends React.Component<{ children: React.ReactNode; fallback: React.ReactNode }, { hasError: boolean }> {
  constructor(props: { children: React.ReactNode; fallback: React.ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }
  static getDerivedStateFromError() { return { hasError: true }; }
  render() { return this.state.hasError ? this.props.fallback : this.props.children; }
}

export const router = createBrowserRouter([

  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <LazyPage Component={PublicHomePage} /> },
      { path: '/partidas', element: <LazyPage Component={PartidasPage} /> },
      { path: '/partidas/:id', element: <LazyPage Component={PartidaDetalhePage} /> },
      { path: '/campeonatos', element: <LazyPage Component={PublicCampeonatosPage} /> },
      { path: '/campeonatos/:id', element: <LazyPage Component={CampeonatoDetalhePage} /> },
      { path: '/times', element: <LazyPage Component={PublicTimesPage} /> },
      { path: '/times/:id', element: <LazyPage Component={TimeDetalhePage} /> },
      { path: '/jogadores/:id', element: <LazyPage Component={JogadorDetalhePage} /> },
      { path: '/estatisticas', element: <LazyPage Component={EstatisticasPage} /> },
    ],
  },

  { path: '/login', element: <LazyPage Component={LoginPage} /> },
  { path: '/registro', element: <LazyPage Component={RegisterPage} /> },

  {
    path: '/dashboard',
    element: <ProtectedRoute />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <LazyPage Component={DashboardPage} /> },
          { path: 'campeonatos', element: <LazyPage Component={CampeonatosPage} /> },
          { path: 'campeonatos/:id', element: <LazyPage Component={CampeonatoDetalhePage} /> },
          { path: 'times', element: <LazyPage Component={TimesPage} /> },
          { path: 'times/:id', element: <LazyPage Component={TimeDetalhePage} /> },
          { path: 'jogadores', element: <LazyPage Component={JogadoresPage} /> },
          { path: 'jogadores/:id', element: <LazyPage Component={JogadorDetalhePage} /> },
          { path: 'partidas', element: <LazyPage Component={PartidasPage} /> },
          { path: 'partidas/:id', element: <LazyPage Component={PartidaDetalhePage} /> },
          { path: 'estatisticas', element: <LazyPage Component={EstatisticasPage} /> },
          { path: 'fases', element: <LazyPage Component={FasesPage} /> },
          { path: 'importacao', element: <LazyPage Component={ImportacaoPage} /> },
        ],
      },
    ],
  },
  { path: '*', element: <LazyPage Component={NotFoundPage} /> },
]);
