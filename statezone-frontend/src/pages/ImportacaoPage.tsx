import { useEffect, useState } from 'react';
import { Database, Users, Shield, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import { toast } from 'sonner';

type ImportStatus = 'idle' | 'loading' | 'success' | 'error';

interface ImportCardProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  status: ImportStatus;
  message?: string;
  onImport: () => void;
  children?: React.ReactNode;
}

function ImportCard({ icon, title, description, status, message, onImport, children }: ImportCardProps) {
  return (
    <Card className="p-6">
      <div className="flex items-start gap-4">
        <div
          className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${
            status === 'success'
              ? 'bg-success-bg text-success'
              : status === 'error'
                ? 'bg-danger-bg text-danger'
                : 'bg-accent/5 text-accent'
          }`}
        >
          {status === 'loading' ? (
            <Loader2 size={22} className="animate-spin" />
          ) : status === 'success' ? (
            <CheckCircle2 size={22} />
          ) : status === 'error' ? (
            <AlertCircle size={22} />
          ) : (
            icon
          )}
        </div>
        <div className="flex-1 min-w-0">
          <h3 className="text-sm font-bold text-slate-200">{title}</h3>
          <p className="text-xs text-slate-500 mt-1">{description}</p>
          {message && (
            <p
              className={`text-xs mt-2 ${
                status === 'success' ? 'text-success' : status === 'error' ? 'text-danger' : 'text-slate-400'
              }`}
            >
              {message}
            </p>
          )}
          <div className="mt-4">
            {children}
            <Button size="sm" onClick={onImport} disabled={status === 'loading'}>
              <Database size={13} />
              {status === 'loading' ? 'Importando...' : 'Importar'}
            </Button>
          </div>
        </div>
      </div>
    </Card>
  );
}

export default function ImportacaoPage() {
  const [times, setTimes] = useState<{ id: number; nome: string }[]>([]);
  const [selectedTimeId, setSelectedTimeId] = useState<number | ''>('');
  const [statusTimes, setStatusTimes] = useState<ImportStatus>('idle');
  const [statusJogadoresTodos, setStatusJogadoresTodos] = useState<ImportStatus>('idle');
  const [statusJogadoresTime, setStatusJogadoresTime] = useState<ImportStatus>('idle');
  const [msgTimes, setMsgTimes] = useState('');
  const [msgJogadoresTodos, setMsgJogadoresTodos] = useState('');
  const [msgJogadoresTime, setMsgJogadoresTime] = useState('');
  const [loadingTimes, setLoadingTimes] = useState(true);

  useEffect(() => {
    let isMounted = true;
    api
      .get('/times')
      .then((res) => {
        if (isMounted) setTimes(res.data);
      })
      .catch(() => {
        toast.error('Erro ao carregar times');
      })
      .finally(() => {
        if (isMounted) setLoadingTimes(false);
      });
    return () => {
      isMounted = false;
    };
  }, []);

  const importarTimes = async () => {
    setStatusTimes('loading');
    setMsgTimes('');
    try {
      await api.post('/importacao/times');
      setStatusTimes('success');
      setMsgTimes('Times importados com sucesso!');
      const res = await api.get('/times');
      setTimes(res.data);
    } catch (err) {
      setStatusTimes('error');
      setMsgTimes(getApiError(err, 'Erro ao importar times. Verifique a chave da API.'));
    }
  };

  const importarJogadoresTodos = async () => {
    setStatusJogadoresTodos('loading');
    setMsgJogadoresTodos('');
    try {
      await api.post('/importacao/importar-jogadores-todos');
      setStatusJogadoresTodos('success');
      setMsgJogadoresTodos('Jogadores importados com sucesso!');
    } catch (err) {
      setStatusJogadoresTodos('error');
      setMsgJogadoresTodos(getApiError(err, 'Erro ao importar jogadores.'));
    }
  };

  const importarJogadoresTime = async () => {
    if (!selectedTimeId) return;
    setStatusJogadoresTime('loading');
    setMsgJogadoresTime('');
    try {
      await api.post(`/importacao/jogadores/${selectedTimeId}`);
      setStatusJogadoresTime('success');
      setMsgJogadoresTime('Jogadores importados com sucesso!');
    } catch (err) {
      setStatusJogadoresTime('error');
      setMsgJogadoresTime(getApiError(err, 'Erro ao importar jogadores.'));
    }
  };

  return (
    <div className="space-y-6 animate-fade-in-up">
      <PageHeader title="Importação" description="Importe times e jogadores via API-Football" />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <ImportCard
          icon={<Shield size={22} />}
          title="Importar Times"
          description="Busca todos os times do Brasileirão Série A via API-Football"
          status={statusTimes}
          message={msgTimes}
          onImport={importarTimes}
        />

        <ImportCard
          icon={<Users size={22} />}
          title="Importar Jogadores (todos os times)"
          description="Importa o elenco de todos os times cadastrados"
          status={statusJogadoresTodos}
          message={msgJogadoresTodos}
          onImport={importarJogadoresTodos}
        />
      </div>

      <Card className="p-6">
        <div className="flex items-start gap-4">
          <div
            className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 ${
              statusJogadoresTime === 'success'
                ? 'bg-success-bg text-success'
                : statusJogadoresTime === 'error'
                  ? 'bg-danger-bg text-danger'
                  : 'bg-accent/5 text-accent'
            }`}
          >
            {statusJogadoresTime === 'loading' ? (
              <Loader2 size={22} className="animate-spin" />
            ) : statusJogadoresTime === 'success' ? (
              <CheckCircle2 size={22} />
            ) : statusJogadoresTime === 'error' ? (
              <AlertCircle size={22} />
            ) : (
              <Users size={22} />
            )}
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-sm font-bold text-slate-200">Importar Jogadores (time específico)</h3>
            <p className="text-xs text-slate-500 mt-1">Selecione um time para importar o elenco</p>
            {msgJogadoresTime && (
              <p
                className={`text-xs mt-2 ${
                  statusJogadoresTime === 'success'
                    ? 'text-success'
                    : statusJogadoresTime === 'error'
                      ? 'text-danger'
                      : 'text-slate-400'
                }`}
              >
                {msgJogadoresTime}
              </p>
            )}
            <div className="mt-4 flex items-center gap-3">
              {loadingTimes ? (
                <div className="min-w-[200px] h-9 animate-shimmer rounded-lg" />
              ) : (
                <select
                  value={selectedTimeId}
                  onChange={(e) => setSelectedTimeId(e.target.value ? Number(e.target.value) : '')}
                  className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-2 text-xs text-slate-300 focus:outline-none focus:border-accent/40 min-w-[200px]"
                >
                  <option value="">Selecione um time</option>
                  {times.length === 0 && (
                    <option value="" disabled>
                      Nenhum time disponível
                    </option>
                  )}
                  {times.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.nome}
                    </option>
                  ))}
                </select>
              )}
              <Button
                size="sm"
                onClick={importarJogadoresTime}
                disabled={!selectedTimeId || statusJogadoresTime === 'loading'}
              >
                <Database size={13} />
                {statusJogadoresTime === 'loading' ? 'Importando...' : 'Importar'}
              </Button>
            </div>
          </div>
        </div>
      </Card>

      <Card className="p-5">
        <div className="flex items-start gap-3">
          <AlertCircle size={16} className="text-info shrink-0 mt-0.5" />
          <div className="text-xs text-slate-500">
            <p className="font-medium text-slate-400 mb-1">Sobre a importação</p>
            <p>
              A API-Football tem limites de requisições por dia (100 no plano gratuito). A importação de jogadores de
              todos os times pode levar alguns minutos devido ao delay de 8s entre cada requisição para respeitar o rate
              limit.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
