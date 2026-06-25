import { createBrowserRouter } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CampeonatosPage from './pages/CampeonatosPage';
import TimesPage from './pages/TimesPage';
import TimeDetalhePage from './pages/TimeDetalhePage';
import JogadoresPage from './pages/JogadoresPage';
import PartidasPage from './pages/PartidasPage';
import PartidaDetalhePage from './pages/PartidaDetalhePage';
import EstatisticasPage from './pages/EstatisticasPage';
import FasesPage from './pages/FasesPage';
import ImportacaoPage from './pages/ImportacaoPage';
import PublicHomePage from './pages/PublicHomePage';
import PublicCampeonatosPage from './pages/PublicCampeonatosPage';
import PublicTimesPage from './pages/PublicTimesPage';
import CampeonatoDetalhePage from './pages/CampeonatoDetalhePage';
import JogadorDetalhePage from './pages/JogadorDetalhePage';
import Layout from './components/Layout';
import PublicLayout from './components/PublicLayout';
import ProtectedRoute from './components/ProtectedRoute';
import NotFoundPage from './pages/NotFoundPage';

export const router = createBrowserRouter([

  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <PublicHomePage /> },
      { path: '/partidas', element: <PartidasPage /> },
      { path: '/partidas/:id', element: <PartidaDetalhePage /> },
      { path: '/campeonatos', element: <PublicCampeonatosPage /> },
      { path: '/campeonatos/:id', element: <CampeonatoDetalhePage /> },
      { path: '/times', element: <PublicTimesPage /> },
      { path: '/times/:id', element: <TimeDetalhePage /> },
      { path: '/jogadores/:id', element: <JogadorDetalhePage /> },
      { path: '/estatisticas', element: <EstatisticasPage /> },
    ],
  },

  { path: '/login', element: <LoginPage /> },
  { path: '/registro', element: <RegisterPage /> },

  {
    path: '/dashboard',
    element: <ProtectedRoute />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <DashboardPage /> },
          { path: 'campeonatos', element: <CampeonatosPage /> },
          { path: 'campeonatos/:id', element: <CampeonatoDetalhePage /> },
          { path: 'times', element: <TimesPage /> },
          { path: 'times/:id', element: <TimeDetalhePage /> },
          { path: 'jogadores', element: <JogadoresPage /> },
          { path: 'jogadores/:id', element: <JogadorDetalhePage /> },
          { path: 'partidas', element: <PartidasPage /> },
          { path: 'partidas/:id', element: <PartidaDetalhePage /> },
          { path: 'estatisticas', element: <EstatisticasPage /> },
          { path: 'fases', element: <FasesPage /> },
          { path: 'importacao', element: <ImportacaoPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <NotFoundPage /> },
]);
