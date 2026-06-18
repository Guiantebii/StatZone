package br.com.statezone.support;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.enums.TipoEvento;
import br.com.statezone.enums.FaseEnum;
import br.com.statezone.enums.FuncaoEscalacao;
import br.com.statezone.enums.MotivoSuspensao;
import br.com.statezone.enums.Posicao;
import br.com.statezone.enums.StatusConfronto;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.ConfrontoEliminatorio;
import br.com.statezone.model.EscalacaoPartida;
import br.com.statezone.model.EstatisticasJogador;
import br.com.statezone.model.EstatisticasJogadorCampeonato;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.model.EventoPartida;
import br.com.statezone.model.FaseEliminatoria;
import br.com.statezone.model.Jogador;
import br.com.statezone.model.Partida;
import br.com.statezone.model.Suspensao;
import br.com.statezone.model.Time;

import java.util.ArrayList;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Campeonato campeonato(Long id, Integer amarelosParaSuspensao) {
        Campeonato campeonato = new Campeonato();
        campeonato.setId(id);
        campeonato.setNome("Campeonato " + id);
        campeonato.setPais("Brasil");
        campeonato.setTemporada("2026");
        campeonato.setLogoUrl("https://example.com/campeonato-" + id + ".png");
        campeonato.setAmarelosParaSuspensao(amarelosParaSuspensao);
        campeonato.setTimes(new ArrayList<>());
        campeonato.setPartidas(new ArrayList<>());
        return campeonato;
    }

    public static Time time(Long id, String nome) {
        Time time = new Time();
        time.setId(id);
        time.setNome(nome);
        time.setSigla(nome == null ? null : nome.substring(0, Math.min(3, nome.length())).toUpperCase());
        time.setCidade("Cidade " + id);
        time.setPais("Brasil");
        time.setEscudoUrl("https://example.com/" + id + ".png");
        time.setSeed(id == null ? null : id.intValue());
        return time;
    }

    public static Jogador jogador(Long id, Time time, String nome) {
        return jogador(id, time, nome, null, null);
    }

    public static Jogador jogador(Long id, Time time, String nome, Posicao posicao, Integer numeroCamisa) {
        Jogador jogador = new Jogador();
        jogador.setId(id);
        jogador.setNome(nome);
        jogador.setPosicao(posicao);
        jogador.setNumeroCamisa(numeroCamisa);
        jogador.setNacionalidade("Brasil");
        jogador.setTime(time);
        jogador.setFotoUrl("https://example.com/jogador-" + id + ".png");
        return jogador;
    }

    public static Partida partida(
            Long id,
            Campeonato campeonato,
            Time mandante,
            Time visitante
    ) {
        return partida(id, campeonato, mandante, visitante, StatusPartida.AGENDADA, 1, 0, 0);
    }

    public static Partida partida(
            Long id,
            Campeonato campeonato,
            Time mandante,
            Time visitante,
            StatusPartida status,
            Integer rodada,
            Integer golsMandante,
            Integer golsVisitante
    ) {
        Partida partida = new Partida();
        partida.setId(id);
        partida.setCampeonato(campeonato);
        partida.setTimeMandante(mandante);
        partida.setTimeVisitante(visitante);
        partida.setRodada(rodada);
        partida.setStatus(status);
        partida.setGolsMandante(golsMandante);
        partida.setGolsVisitante(golsVisitante);
        partida.setEventos(new ArrayList<>());
        return partida;
    }

    public static EventoPartida evento(
            Long id,
            TipoEvento tipoEvento,
            Jogador jogador,
            Time time,
            boolean anulado
    ) {
        return evento(id, tipoEvento, 1, null, tipoEvento.name(), jogador, null, time, anulado);
    }

    public static EventoPartida evento(
            Long id,
            TipoEvento tipoEvento,
            Integer minuto,
            Integer minutoExtra,
            String descricao,
            Jogador jogador,
            Jogador jogadorSecundario,
            Time time,
            boolean anulado
    ) {
        EventoPartida evento = new EventoPartida();
        evento.setId(id);
        evento.setTipoEvento(tipoEvento);
        evento.setMinuto(minuto);
        evento.setMinutoExtra(minutoExtra);
        evento.setDescricao(descricao);
        evento.setJogador(jogador);
        evento.setJogadorSecundario(jogadorSecundario);
        evento.setTime(time);
        evento.setAnulado(anulado);
        return evento;
    }

    public static EstatisticasJogador estatisticasJogador(Long id, Jogador jogador) {
        EstatisticasJogador estatisticas = new EstatisticasJogador();
        estatisticas.setId(id);
        estatisticas.setJogador(jogador);
        return estatisticas;
    }

    public static EstatisticasJogadorCampeonato estatisticasJogadorCampeonato(
            Long id,
            Jogador jogador,
            Campeonato campeonato
    ) {
        EstatisticasJogadorCampeonato estatisticas = new EstatisticasJogadorCampeonato();
        estatisticas.setId(id);
        estatisticas.setJogador(jogador);
        estatisticas.setCampeonato(campeonato);
        return estatisticas;
    }

    public static EscalacaoPartida escalacaoPartida(
            Long id,
            Partida partida,
            Jogador jogador,
            FuncaoEscalacao funcao,
            Posicao posicao,
            Integer numeroCamisa,
            Boolean ativo
    ) {
        EscalacaoPartida escalacao = new EscalacaoPartida();
        escalacao.setId(id);
        escalacao.setPartida(partida);
        escalacao.setJogador(jogador);
        escalacao.setFuncao(funcao);
        escalacao.setPosicao(posicao);
        escalacao.setNumeroCamisa(numeroCamisa);
        escalacao.setAtivo(ativo);
        return escalacao;
    }

    public static EstatisticasPartida estatisticasPartida(Long id, Partida partida) {
        EstatisticasPartida estatisticas = new EstatisticasPartida();
        estatisticas.setId(id);
        estatisticas.setPartida(partida);
        return estatisticas;
    }

    public static FaseEliminatoria faseEliminatoria(Long id, Campeonato campeonato, FaseEnum fase) {
        FaseEliminatoria faseEliminatoria = new FaseEliminatoria();
        faseEliminatoria.setId(id);
        faseEliminatoria.setCampeonato(campeonato);
        faseEliminatoria.setFase(fase);
        faseEliminatoria.setJogoUnico(true);
        faseEliminatoria.setConfrontos(new ArrayList<>());
        return faseEliminatoria;
    }

    public static ConfrontoEliminatorio confrontoEliminatorio(
            Long id,
            FaseEliminatoria fase,
            Time timeA,
            Time timeB
    ) {
        ConfrontoEliminatorio confronto = new ConfrontoEliminatorio();
        confronto.setId(id);
        confronto.setFaseEliminatoria(fase);
        confronto.setTimeA(timeA);
        confronto.setTimeB(timeB);
        confronto.setStatusConfronto(StatusConfronto.PENDENTE);
        confronto.setJogoUnico(true);
        confronto.setBracketIndex(0);
        confronto.setRoundIndex(1);
        return confronto;
    }

    public static Suspensao suspensao(
            Long id,
            Jogador jogador,
            Campeonato campeonato,
            MotivoSuspensao motivo,
            Integer rodadaSuspensao,
            Partida partidaAlvo
    ) {
        Suspensao suspensao = new Suspensao();
        suspensao.setId(id);
        suspensao.setJogador(jogador);
        suspensao.setCampeonato(campeonato);
        suspensao.setMotivo(motivo);
        suspensao.setRodadaSuspensao(rodadaSuspensao);
        suspensao.setPartidaAlvo(partidaAlvo);
        return suspensao;
    }
}
