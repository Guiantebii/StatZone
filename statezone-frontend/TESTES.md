# Roteiro de Teste — StateZone

> **Pré-requisito:** Antes de testar rankings, chame `POST /campeonatos/1/reprocessar-estatisticas` para popular as estatísticas.

---

## 1. Páginas Públicas (sem login)

- [ ] 1.1 Home carrega — Acessar `/`, ver cards de partidas ao vivo + artilharia top 5
- [ ] 1.2 Partidas públicas — Clicar "Partidas" no header, ver lista de partidas
- [ ] 1.3 Detalhe da partida — Clicar em uma partida (`/partidas/{id}`), ver timeline, escalação, estatísticas
- [ ] 1.4 Campeonatos públicos — Clicar "Campeonatos" (`/campeonatos`), ver lista
- [ ] 1.5 Detalhe do campeonato — Clicar em um (`/campeonatos/{id}`), testar abas: Classificação, Partidas, Artilharia, Seleção, MVP
- [ ] 1.6 Times públicos — Clicar "Times" (`/times`), ver grid
- [ ] 1.7 Detalhe do time — Clicar em um (`/times/{id}`), ver info, forma, jogadores, estatísticas
- [ ] 1.8 Detalhe do jogador — Clicar em um jogador (`/jogadores/{id}`), ver perfil + estatísticas carreira
- [ ] 1.9 Estatísticas públicas — Clicar "Estatísticas" (`/estatisticas`), testar **todas as 7 abas**
- [ ] 1.10 Paginação rankings — Abas Artilharia/Assistências/Cartões/Goleiros, clicar "Próximo" e "Anterior"
- [ ] 1.11 404 — Acessar rota inexistente, ver página 404

---

## 2. Autenticação

- [ ] 2.1 Tela de login — Acessar `/login`, ver formulário
- [ ] 2.2 Login inválido — Email/senha errados, ver toast de erro
- [ ] 2.3 Login válido — `admin@statezone.com` / `admin123`, redireciona pro dashboard
- [ ] 2.4 Sessão persiste — F5 na página do dashboard, ainda logado
- [ ] 2.5 Logout — Clicar "Sair" na sidebar, volta pra home pública
- [ ] 2.6 Tela de registro — Acessar `/registro`, ver formulário
- [ ] 2.7 Registro senha fraca — Senha < 8 chars ou sem complexidade, ver erro
- [ ] 2.8 Registro válido — Criar novo usuário, redireciona pro dashboard
- [ ] 2.9 Proteção de rota — Acessar `/dashboard` deslogado, redireciona pra `/login`
- [ ] 2.10 Rate limit — 6+ POSTs rápidos em `/api/auth/login`, 429 Too Many Requests

---

## 3. Dashboard Admin (navegação)

- [ ] 3.1 Dashboard home — `/dashboard`, ver cards de resumo + artilharia top 5 + próximas partidas
- [ ] 3.2 Sidebar colapsa — Clicar no toggle, menu recolhe/expande
- [ ] 3.3 Todos os links — Navegar por: Dashboard, Campeonatos, Times, Jogadores, Partidas, Estatísticas, Fases, Importação
- [ ] 3.4 Breadcrumbs — Navegar fundo e voltar com "Voltar"

---

## 4. Campeonatos (Admin)

- [ ] 4.1 Listar — `/dashboard/campeonatos`, ver tabela
- [ ] 4.2 Criar — Clicar "Novo Campeonato", preencher formulário, salvar
- [ ] 4.3 Editar — Clicar editar, alterar nome, salvar
- [ ] 4.4 Excluir — Excluir campeonato sem partidas/times, confirmar
- [ ] 4.5 Excluir com dependências — Tentar excluir campeonato com partidas, ver erro
- [ ] 4.6 Gerenciar times — Abrir "Gerenciar Times", adicionar/remover times
- [ ] 4.7 Gerar fixtures — Clicar "Gerar Partidas", tabela gerada
- [ ] 4.8 Ver detalhes — Clicar em um campeonato, testar todas as abas

---

## 5. Times (Admin)

- [ ] 5.1 Listar — `/dashboard/times`, ver tabela
- [ ] 5.2 Criar (Clube) — Tipo CLUBE, campos: nome, sigla, país, cidade, estádio, técnico, escudo
- [ ] 5.3 Criar (Seleção) — Tipo SELECAO, cidade/estádio/técnico somem do form
- [ ] 5.4 Editar — Alterar nome/sigla/tipo, salvar
- [ ] 5.5 Excluir — Excluir time sem vínculos, confirmar
- [ ] 5.6 Detalhes — Clicar em um time, ver info, forma, jogadores, estatísticas
- [ ] 5.7 Buscar — Digitar na SearchBar, resultados em dropdown

---

## 6. Jogadores (Admin)

- [ ] 6.1 Listar — `/dashboard/jogadores`, ver tabela
- [ ] 6.2 Criar — Novo jogador com todos os campos
- [ ] 6.3 Campos opcionais — Criar sem altura/peso/valor, deve funcionar
- [ ] 6.4 Editar — Alterar dados, salvar
- [ ] 6.5 Excluir — Excluir jogador, confirmar
- [ ] 6.6 Detalhes — Clicar em um jogador, ver perfil + estatísticas

---

## 7. Partidas (Admin) — Ciclo de Vida

- [ ] 7.1 Listar — `/dashboard/partidas`, ver tabela
- [ ] 7.2 Criar — Nova partida (campeonato, times, data, estádio, árbitro, rodada)
- [ ] 7.3 Iniciar — Partida AGENDADA → AO_VIVO
- [ ] 7.4 Registrar evento — Gol, cartão, falta, substituição na timeline
- [ ] 7.5 Intervalo — AO_VIVO → INTERVALO
- [ ] 7.6 Segundo tempo — INTERVALO → AO_VIVO
- [ ] 7.7 Encerrar — AO_VIVO → ENCERRADA, stats processadas
- [ ] 7.8 WO Mandante — AGENDADA → WO_MANDANTE (0x3)
- [ ] 7.9 WO Visitante — AGENDADA → WO_VISITANTE (3x0)
- [ ] 7.10 Adiar — AGENDADA → ADIADA
- [ ] 7.11 Cancelar — AGENDADA → CANCELADA (bloqueado se AO_VIVO/ENCERRADA)
- [ ] 7.12 Pênaltis — Iniciar → encerrar com pênaltis
- [ ] 7.13 Escalação — Adicionar jogadores (titular/reserva)
- [ ] 7.14 Estatísticas da partida — Ver posse, finalizações, etc. após encerrar
- [ ] 7.15 WebSocket ao vivo — 2 abas na mesma partida AO_VIVO, eventos sincronizam

---

## 8. Grupos e Fases (Mata-Mata)

- [ ] 8.1 Criar grupos — Em campeonato GRUPOS_E_MATA_MATA, criar grupos A, B
- [ ] 8.2 Adicionar times — Alocar times nos grupos
- [ ] 8.3 Gerar fixtures do grupo — Partidas do grupo criadas
- [ ] 8.4 Classificação do grupo — Tabela do grupo com pontos
- [ ] 8.5 Criar fase eliminatória — Em `/dashboard/fases`, criar fase (Oitavas, Quartas, etc.)
- [ ] 8.6 Gerar confrontos — "Gerar primeira fase" a partir da classificação dos grupos
- [ ] 8.7 Encerrar confronto — Vencedor avança, bracket atualiza
- [ ] 8.8 Bracket — Visualizar chaveamento

---

## 9. Estatísticas e Rankings

- [ ] 9.1 Reprocessar — `POST /campeonatos/1/reprocessar-estatisticas` → 200 OK
- [ ] 9.2 Classificação — Times ordenados por pontos, SG
- [ ] 9.3 Artilharia — Jogadores com gols, paginação
- [ ] 9.4 Assistências — Jogadores com assists, paginação
- [ ] 9.5 Cartões — Amarelos e vermelhos lado a lado
- [ ] 9.6 Goleiros — Clean sheets, defesas, pênaltis, paginação
- [ ] 9.7 Seleção — Melhores por posição
- [ ] 9.8 MVP — Craque com score e estatísticas
- [ ] 9.9 Paginação — Navegar páginas, dados carregam corretamente

---

## 10. Suspensões

- [ ] 10.1 Listar suspensões — `GET /campeonatos/1/suspensoes`
- [ ] 10.2 Suspensão por rodada — `GET /campeonatos/1/suspensoes/rodada/2`

---

## 11. Importação (API Externa)

- [ ] 11.1 Importar times — `POST /api/importacao/times`
- [ ] 11.2 Importar jogadores — `POST /api/importacao/jogadores/{timeId}`
- [ ] 11.3 Importar todos — `POST /api/importacao/importar-jogadores-todos`
- [ ] 11.4 Teste fixtures — `GET /api/importacao/teste-fixtures`

---

## 12. WebSocket

- [ ] 12.1 Conexão — Abrir partida AO_VIVO, WebSocket conecta sem erro no console
- [ ] 12.2 Eventos em tempo real — Registrar evento em outra aba, primeira aba atualiza
- [ ] 12.3 Reconexão — Fechar/reabrir WebSocket, reconecta

---

## 13. Busca (SearchBar)

- [ ] 13.1 Buscar times — Digitar "Flamengo", aparece no dropdown
- [ ] 13.2 Buscar jogadores — Digitar "Gabriel", jogadores aparecem
- [ ] 13.3 Buscar vazio — Digitar "zzzzz", "Nenhum resultado"
- [ ] 13.4 Debounce — Digitar rápido, só 1 chamada (300ms)

---

## 14. Regressão / Geral

- [ ] 14.1 Responsivo — Redimensionar pra mobile, layout adapta
- [ ] 14.2 Loading states — Navegar entre páginas, ver shimmer/skeleton
- [ ] 14.3 Erros de rede — Desligar backend, toasts sem crash
- [ ] 14.4 Modal fecha — ESC ou clique fora fecha o modal
- [ ] 14.5 Scroll lock — Modal aberto, body não scrolla
- [ ] 14.6 Modal no topo — Modal aparece no topo da viewport
- [ ] 14.7 Rotas admin vs public — Mesma rota em `/dashboard` e sem, comportamentos diferentes
