import { useState } from 'react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'sonner';
import { UserPlus, Mail, Lock, Shield, Zap, Loader2 } from 'lucide-react';

export default function RegisterPage() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const senhaErro =
    senha.length > 0 &&
    (senha.length < 8
      ? 'Mínimo 8 caracteres'
      : !/[A-Z]/.test(senha)
        ? 'Deve conter uma letra maiúscula'
        : !/[a-z]/.test(senha)
          ? 'Deve conter uma letra minúscula'
          : !/[0-9]/.test(senha)
            ? 'Deve conter um número'
            : !/[^A-Za-z0-9]/.test(senha)
              ? 'Deve conter um caractere especial'
              : '');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (senha !== confirmarSenha) {
      toast.error('As senhas não coincidem');
      return;
    }
    if (senhaErro) {
      toast.error(senhaErro);
      return;
    }
    setLoading(true);
    try {
      await api.post('/api/auth/registro', { email, senha });
      toast.success('Conta criada com sucesso! Faça login para continuar.');
      navigate('/login');
    } catch (err) {
      toast.error(getApiError(err, 'Erro ao registrar. Verifique os dados ou tente outro email.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-primary-dark flex">
      <div className="flex-1 flex items-center justify-center px-6 lg:px-16 xl:px-24">
        <div className="w-full max-w-md">
          <div className="lg:hidden flex items-center gap-2 mb-10">
            <div className="w-9 h-9 bg-gradient-to-br from-accent to-accent-hover rounded-xl flex items-center justify-center shadow-lg shadow-accent/20">
              <span className="text-primary-dark font-extrabold text-sm">SZ</span>
            </div>
            <span className="text-slate-200 font-bold text-lg">StateZone</span>
          </div>

          <div className="mb-8">
            <h2 className="text-3xl font-bold text-slate-100 tracking-tight">Crie sua conta</h2>
            <p className="text-sm text-slate-500 mt-1.5">Comece a gerenciar seus campeonatos de futebol.</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label
                htmlFor="email"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                Email
              </label>
              <div className="relative">
                <Mail size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  id="email"
                  type="email"
                  inputMode="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="w-full bg-white/[0.04] border border-white/[0.08] text-slate-200 placeholder-slate-600 rounded-lg pl-9 pr-3 py-2.5 text-sm focus:outline-none focus:border-accent/40 focus:ring-2 focus:ring-accent/10 focus:bg-white/[0.06] transition-all"
                  placeholder="seu@email.com"
                />
              </div>
            </div>

            <div>
              <label
                htmlFor="senha"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                Senha
              </label>
              <div className="relative">
                <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  id="senha"
                  type="password"
                  value={senha}
                  onChange={(e) => setSenha(e.target.value)}
                  required
                  minLength={8}
                  className="w-full bg-white/[0.04] border border-white/[0.08] text-slate-200 placeholder-slate-600 rounded-lg pl-9 pr-3 py-2.5 text-sm focus:outline-none focus:border-accent/40 focus:ring-2 focus:ring-accent/10 focus:bg-white/[0.06] transition-all"
                  placeholder="Mínimo 8 caracteres, maiúscula, número e caractere especial"
                />
              </div>
              {senhaErro && <p className="text-xs text-danger mt-1.5">{senhaErro}</p>}
            </div>

            <div>
              <label
                htmlFor="confirmarSenha"
                className="block text-xs font-semibold uppercase tracking-wider text-slate-400 mb-1.5"
              >
                Confirmar Senha
              </label>
              <div className="relative">
                <Lock size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  id="confirmarSenha"
                  type="password"
                  minLength={8}
                  value={confirmarSenha}
                  onChange={(e) => setConfirmarSenha(e.target.value)}
                  required
                  className="w-full bg-white/[0.04] border border-white/[0.08] text-slate-200 placeholder-slate-600 rounded-lg pl-9 pr-3 py-2.5 text-sm focus:outline-none focus:border-accent/40 focus:ring-2 focus:ring-accent/10 focus:bg-white/[0.06] transition-all"
                  placeholder="Repita a senha"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-accent to-accent-hover text-primary-dark py-2.5 rounded-lg text-sm font-bold hover:shadow-[0_4px_20px_rgba(255,215,0,0.25)] hover:brightness-110 transition-all disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? <Loader2 size={14} className="animate-spin" /> : <UserPlus size={16} />}
              {loading ? 'Criando conta...' : 'Criar conta'}
            </button>
          </form>

          <p className="text-center mt-6 text-sm text-slate-500">
            Já tem uma conta?{' '}
            <Link to="/login" className="text-accent hover:text-accent-hover font-semibold transition-colors">
              Faça login
            </Link>
          </p>
        </div>
      </div>

      <div className="hidden lg:flex flex-1 bg-primary border-l border-white/[0.04] flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-accent/[0.03] to-transparent" />
        <div className="absolute -top-20 -right-20 w-96 h-96 bg-accent/5 rounded-full blur-3xl" />
        <div className="absolute -bottom-20 -left-20 w-80 h-80 bg-blue-500/5 rounded-full blur-3xl" />

        <div className="relative z-10">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-14 h-14 bg-gradient-to-br from-accent to-accent-hover rounded-2xl flex items-center justify-center shadow-xl shadow-accent/20">
              <span className="text-primary-dark font-extrabold text-2xl">SZ</span>
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-100">StateZone</h1>
              <p className="text-xs text-slate-500 font-medium tracking-wider uppercase">Sports Analytics</p>
            </div>
          </div>

          <h2 className="text-3xl font-bold text-slate-100 mb-4 leading-tight tracking-tight">
            Comece a usar gratuitamente
          </h2>
          <p className="text-sm text-slate-400 max-w-md leading-relaxed">
            Registre-se agora e tenha acesso a todas as funcionalidades: cadastro de campeonatos, times, jogadores,
            partidas ao vivo, estatísticas e muito mais.
          </p>

          <div className="mt-12 space-y-5">
            <div className="flex items-start gap-4 group">
              <div className="w-10 h-10 rounded-xl bg-success-bg flex items-center justify-center flex-shrink-0 group-hover:scale-110 transition-transform">
                <UserPlus size={18} className="text-success" />
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-200">Cadastro instantâneo</p>
                <p className="text-xs text-slate-500 mt-0.5">Apenas email e senha. Sem burocracia.</p>
              </div>
            </div>

            <div className="flex items-start gap-4 group">
              <div className="w-10 h-10 rounded-xl bg-info-bg flex items-center justify-center flex-shrink-0 group-hover:scale-110 transition-transform">
                <Shield size={18} className="text-info" />
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-200">Seus dados seguros</p>
                <p className="text-xs text-slate-500 mt-0.5">Senhas criptografadas e autenticação via JWT.</p>
              </div>
            </div>

            <div className="flex items-start gap-4 group">
              <div className="w-10 h-10 rounded-xl bg-warning-bg flex items-center justify-center flex-shrink-0 group-hover:scale-110 transition-transform">
                <Zap size={18} className="text-warning" />
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-200">Acesso total</p>
                <p className="text-xs text-slate-500 mt-0.5">
                  Todas as funcionalidades liberadas para usuários registrados.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="relative z-10">
          <div className="flex items-center gap-4 text-xs text-slate-600 mb-3">
            <a href="/" className="hover:text-slate-400 transition-colors">
              Termos de uso
            </a>
            <span className="text-slate-700">·</span>
            <a href="/" className="hover:text-slate-400 transition-colors">
              Privacidade
            </a>
            <span className="text-slate-700">·</span>
            <a href="/" className="hover:text-slate-400 transition-colors">
              Suporte
            </a>
          </div>
          <p className="text-xs text-slate-600">
            &copy; {new Date().getFullYear()} StateZone. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </div>
  );
}
