import { useState } from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import { ChevronRight, Menu, X } from 'lucide-react';
import Logo from './Logo';
import NavItems from './navigation/NavItems';
import SearchBar from './SearchBar';
import { useAuth } from '../context/AuthContext';

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [sidebarCollapse, setSidebarCollapse] = useState(false);
  const location = useLocation();
  const { userEmail } = useAuth();

  const breadcrumbs = location.pathname
    .split('/')
    .filter(Boolean)
    .map((s) => s.charAt(0).toUpperCase() + s.slice(1));

  return (
    <div className="min-h-screen bg-primary-dark flex">
      {/* Sidebar para desktop */}
      <aside
        className={`hidden md:flex flex-col bg-primary border-r border-white/[0.04] transition-all duration-300 ${
          sidebarCollapse ? 'w-20' : 'w-64'
        }`}
      >
        <div className="p-4 flex items-center justify-between h-16 border-b border-white/[0.04]">
          <Logo collapse={sidebarCollapse} to="/dashboard" />
          <button
            onClick={() => setSidebarCollapse(!sidebarCollapse)}
            className="text-slate-600 hover:text-slate-300 p-1.5 rounded-lg hover:bg-white/[0.05] transition-colors"
            title={sidebarCollapse ? 'Expandir menu' : 'Recolher menu'}
            aria-label={sidebarCollapse ? 'Expandir menu' : 'Recolher menu'}
          >
            {sidebarCollapse ? (
              <ChevronRight size={16} />
            ) : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
              </svg>
            )}
          </button>
        </div>
        <div className="flex-1 overflow-y-auto py-3">
          <NavItems collapse={sidebarCollapse} />
        </div>
      </aside>

      {/* Sidebar móvel (drawer) */}
      {sidebarOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setSidebarOpen(false)} />
          <aside className="relative w-72 glass-strong p-4 animate-slide-in">
            <div className="flex justify-between items-center mb-6 pb-4 border-b border-white/[0.06]">
              <Logo to="/dashboard" />
              <button onClick={() => setSidebarOpen(false)} className="text-slate-400 hover:text-white p-1" aria-label="Fechar menu">
                <X size={20} />
              </button>
            </div>
            <NavItems />
          </aside>
        </div>
      )}

      {/* Conteúdo principal */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* Navbar superior (mobile) */}
        <header className="md:hidden glass border-b border-white/[0.04] text-white px-4 h-14 flex items-center gap-3 sticky top-0 z-40">
          <button onClick={() => setSidebarOpen(true)} className="text-slate-300 hover:text-white p-1" aria-label="Abrir menu">
            <Menu size={20} />
          </button>
          <Logo to="/dashboard" />
        </header>

        {/* Topbar desktop */}
        <header className="hidden md:flex items-center justify-between px-6 h-16 border-b border-white/[0.04] glass-strong sticky top-0 z-40">
          <div className="flex items-center gap-3">
            {breadcrumbs.map((crumb, i) => (
              <span key={crumb} className="flex items-center gap-3">
                {i > 0 && <ChevronRight size={12} className="text-slate-600" />}
                <span className={`text-sm ${i === breadcrumbs.length - 1 ? 'text-slate-200 font-medium' : 'text-slate-500'}`}>
                  {crumb}
                </span>
              </span>
            ))}
          </div>

          <div className="flex items-center gap-4">
            <SearchBar placeholder="Buscar..." className="w-52" navigatePrefix="/dashboard" />

            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-accent-hover flex items-center justify-center text-xs font-bold text-primary-dark shadow-lg shadow-accent/20">
              {userEmail ? userEmail.charAt(0).toUpperCase() : '?'}
            </div>
          </div>
        </header>

        {/* Área de conteúdo */}
        <main className="flex-1 p-4 md:p-6 lg:p-8 overflow-y-auto">
          <div className="max-w-7xl mx-auto animate-fade-in-up">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
