import { useEffect, useState } from 'react';
import api from '../api/client';
import { getApiError } from '../api/errorHandler';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import { toast } from 'sonner';

type Role = 'USER' | 'OPERADOR' | 'ADMIN';

interface Usuario {
  id: number;
  email: string;
  role: Role;
}

export default function UsuariosPage() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<Usuario[]>('/usuarios')
      .then((res) => setUsuarios(res.data))
      .catch((err) => toast.error(getApiError(err, 'Erro ao carregar usuários')))
      .finally(() => setLoading(false));
  }, []);

  const alterar = (usuario: Usuario, role: Role) => {
    api
      .patch<Usuario>(`/usuarios/${usuario.id}/role`, { role })
      .then((res) => {
        setUsuarios((lista) => lista.map((item) => (item.id === res.data.id ? res.data : item)));
        toast.success('Papel atualizado');
      })
      .catch((err) => toast.error(getApiError(err, 'Erro ao atualizar papel')));
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Usuários" description="Promova um operador para lançar eventos sem administrar o campeonato" />
      <Card className="divide-y divide-white/[0.06]">
        {loading && <p className="p-4 text-sm text-slate-500">Carregando...</p>}
        {!loading && usuarios.length === 0 && <p className="p-4 text-sm text-slate-500">Nenhum usuário</p>}
        {usuarios.map((usuario) => (
          <div key={usuario.id} className="flex items-center justify-between gap-4 p-4">
            <span className="text-sm text-slate-200">{usuario.email}</span>
            <select
              value={usuario.role}
              onChange={(e) => alterar(usuario, e.target.value as Role)}
              className="bg-white/[0.04] border border-white/[0.08] rounded-lg px-3 py-1.5 text-xs text-slate-300"
            >
              <option value="USER">Usuário</option>
              <option value="OPERADOR">Operador</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
        ))}
      </Card>
    </div>
  );
}
