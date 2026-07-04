import { useAuth } from '../../context/AuthContext';
import SidebarLink from './SidebarLink';
import { LogOut } from 'lucide-react';

interface NavItemsProps {
  collapse?: boolean;
}

export default function NavItems({ collapse }: NavItemsProps) {
  const { logout } = useAuth();

  return (
    <nav className="flex flex-col flex-1">
      {!collapse && (
        <p className="px-5 pt-2 pb-1 text-[10px] font-semibold uppercase tracking-widest text-slate-600">Gestão</p>
      )}

      <div className="flex flex-col gap-0.5 px-2">
        <SidebarLink to="/dashboard" icon="grid" label="Dashboard" collapse={collapse} end />
        <SidebarLink to="/dashboard/campeonatos" icon="trophy" label="Campeonatos" collapse={collapse} />
        <SidebarLink to="/dashboard/times" icon="shield" label="Times" collapse={collapse} />
        <SidebarLink to="/dashboard/jogadores" icon="user" label="Jogadores" collapse={collapse} />
        <SidebarLink to="/dashboard/partidas" icon="calendar-event" label="Partidas" collapse={collapse} />
      </div>

      {!collapse && (
        <p className="px-5 pt-5 pb-1 text-[10px] font-semibold uppercase tracking-widest text-slate-600">Análise</p>
      )}
      {collapse && <div className="pt-4" />}

      <div className="flex flex-col gap-0.5 px-2">
        <SidebarLink to="/dashboard/estatisticas" icon="chart-bar" label="Estatísticas" collapse={collapse} />
        <SidebarLink to="/dashboard/fases" icon="tournament" label="Fases" collapse={collapse} />
        <SidebarLink to="/dashboard/importacao" icon="cloud-download" label="Importação" collapse={collapse} />
      </div>

      <div className="flex-1" />

      <div className="px-2 pb-3 border-t border-white/[0.06] pt-3">
        <button
          onClick={logout}
          className={`flex items-center gap-3 w-full rounded-lg px-3 py-2.5 text-slate-500 hover:bg-danger-bg hover:text-danger transition-all duration-200 ${
            collapse ? 'justify-center' : ''
          }`}
          title={collapse ? 'Sair' : undefined}
        >
          <LogOut size={18} />
          {!collapse && <span className="text-sm">Sair</span>}
        </button>
      </div>
    </nav>
  );
}
