import { useState } from 'react';
import { Outlet, Link, NavLink } from 'react-router-dom';
import { Menu, X, Zap, Trophy, Shield, Calendar, BarChart3 } from 'lucide-react';
import Logo from './Logo';
import SearchBar from './SearchBar';
import { useAuth } from '../context/AuthContext';

const navLinks = [
  { to: '/', label: 'Ao Vivo', icon: Zap },
  { to: '/partidas', label: 'Partidas', icon: Calendar },
  { to: '/campeonatos', label: 'Campeonatos', icon: Trophy },
  { to: '/times', label: 'Times', icon: Shield },
  { to: '/estatisticas', label: 'Estatísticas', icon: BarChart3 },
];

export default function PublicLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { isAuthenticated, isAdmin, userEmail, logout } = useAuth();

  return (
    <div className="min-h-screen bg-primary-dark">

      <header className="sticky top-0 z-50 bg-primary/90 backdrop-blur-xl border-b border-white/[0.04]">
        <div className="max-w-7xl mx-auto px-4 md:px-6">
          <div className="flex items-center justify-between h-14 md:h-16">

            <Link to="/" className="shrink-0">
              <Logo />
            </Link>


            <nav className="hidden md:flex items-center gap-1">
              {navLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-accent/10 text-accent'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-white/[0.04]'
                    }`
                  }
                >
                  <link.icon size={15} />
                  {link.label}
                </NavLink>
              ))}
            </nav>


            <div className="flex items-center gap-3">

              <div className="hidden md:block">
                <SearchBar
                  placeholder="Buscar times, jogadores..."
                  className="w-48 lg:w-64"
                />
              </div>


              {isAdmin ? (
                <div className="hidden md:flex items-center gap-3">
                  <Link
                    to="/dashboard"
                    className="text-xs text-accent hover:text-accent-hover font-medium"
                  >
                    Painel Admin
                  </Link>
                  <button
                    onClick={logout}
                    className="text-xs text-slate-500 hover:text-slate-300"
                  >
                    Sair
                  </button>
                  <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-accent-hover flex items-center justify-center text-xs font-bold text-primary-dark">
                    {userEmail ? userEmail.charAt(0).toUpperCase() : '?'}
                  </div>
                </div>
              ) : isAuthenticated ? (
                <div className="hidden md:flex items-center gap-2">
                  <button
                    onClick={logout}
                    className="px-4 py-1.5 text-xs font-semibold text-slate-300 hover:text-white transition-colors"
                  >
                    Sair
                  </button>
                </div>
              ) : (
                <div className="hidden md:flex items-center gap-2">
                  <Link
                    to="/login"
                    className="px-4 py-1.5 text-xs font-semibold text-slate-300 hover:text-white transition-colors"
                  >
                    Entrar
                  </Link>
                  <Link
                    to="/registro"
                    className="px-4 py-1.5 text-xs font-semibold bg-accent text-primary-dark rounded-lg hover:bg-accent-hover transition-colors"
                  >
                    Cadastrar
                  </Link>
                </div>
              )}


              <button
                onClick={() => setMobileOpen(true)}
                className="md:hidden p-2 text-slate-400 hover:text-white"
                aria-label="Abrir menu"
              >
                <Menu size={20} />
              </button>
            </div>
          </div>
        </div>
      </header>


      {mobileOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setMobileOpen(false)} />
          <div className="relative w-72 max-w-[85vw] glass-strong p-5 animate-slide-in">
            <div className="flex justify-between items-center mb-6 pb-4 border-b border-white/[0.06]">
              <Logo />
              <button onClick={() => setMobileOpen(false)} className="text-slate-400 hover:text-white p-1" aria-label="Fechar menu">
                <X size={20} />
              </button>
            </div>


            <div className="mb-6">
              <SearchBar placeholder="Buscar times, jogadores..." />
            </div>

            <nav className="flex flex-col gap-1">
              {navLinks.map((link) => (
                <NavLink
                  key={link.to}
                  to={link.to}
                  end={link.to === '/'}
                  onClick={() => setMobileOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                      isActive
                        ? 'bg-accent/10 text-accent'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-white/[0.04]'
                    }`
                  }
                >
                  <link.icon size={18} />
                  {link.label}
                </NavLink>
              ))}
            </nav>

            <div className="mt-6 pt-4 border-t border-white/[0.06]">
              {isAdmin ? (
                <div className="space-y-2">
                  <Link
                    to="/dashboard"
                    onClick={() => setMobileOpen(false)}
                    className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm text-accent hover:bg-accent/5"
                  >
                    <Shield size={18} />
                    Painel Admin
                  </Link>
                  <button
                    onClick={() => { logout(); setMobileOpen(false); }}
                    className="w-full text-left px-3 py-2.5 rounded-lg text-sm text-slate-500 hover:text-slate-300"
                  >
                    Sair
                  </button>
                </div>
              ) : isAuthenticated ? (
                <div className="space-y-2">
                  <button
                    onClick={() => { logout(); setMobileOpen(false); }}
                    className="w-full text-left px-3 py-2.5 rounded-lg text-sm text-slate-500 hover:text-slate-300"
                  >
                    Sair
                  </button>
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  <Link
                    to="/login"
                    onClick={() => setMobileOpen(false)}
                    className="px-3 py-2.5 text-sm text-slate-300 hover:text-white"
                  >
                    Entrar
                  </Link>
                  <Link
                    to="/registro"
                    onClick={() => setMobileOpen(false)}
                    className="px-3 py-2.5 text-sm text-center font-semibold bg-accent text-primary-dark rounded-lg"
                  >
                    Cadastrar
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}


      <main className="max-w-7xl mx-auto px-4 md:px-6 py-6 md:py-8">
        <Outlet />
      </main>
    </div>
  );
}
