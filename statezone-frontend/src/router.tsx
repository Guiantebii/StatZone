import { createBrowserRouter } from 'react-router-dom';
import { LazyPage, LoginPage, RegisterPage, DashboardPage, CampeonatosPage, TimesPage, TimeDetalhePage, JogadoresPage, PartidasPage, PartidaDetalhePage, EstatisticasPage, FasesPage, ImportacaoPage, PublicHomePage, PublicCampeonatosPage, PublicTimesPage, CampeonatoDetalhePage, JogadorDetalhePage, NotFoundPage } from './components/LazyPage';
import Layout from './components/Layout';
import PublicLayout from './components/PublicLayout';
import ProtectedRoute from './components/ProtectedRoute';

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
