package br.com.statezone.listeners;

import br.com.statezone.events.EventoPartidaCriadaEvent;
import br.com.statezone.model.EstatisticasPartida;
import br.com.statezone.repository.EstatisticasPartidaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EstatisticasEventListener {

    private final EstatisticasPartidaRepository repository;

    @EventListener
    @Transactional
    public void onEvento(EventoPartidaCriadaEvent event) {

        var e = event.evento();
        var partida = event.partida();

        boolean mandante =
                e.getTime().getId().equals(partida.getTimeMandante().getId());

        EstatisticasPartida stats = repository.findByPartidaId(partida.getId())
                .orElseGet(() -> {
                    EstatisticasPartida s = new EstatisticasPartida();
                    s.setPartida(partida);
                    return repository.saveAndFlush(s);
                });

        switch (e.getTipoEvento()) {

            case FINALIZACAO -> {
                if (mandante)
                    stats.setFinalizacoesMandante(stats.getFinalizacoesMandante() + 1);
                else
                    stats.setFinalizacoesVisitante(stats.getFinalizacoesVisitante() + 1);
            }

            case GOL, PENALTI_GOL -> {
                if (mandante) {
                    stats.setFinalizacoesMandante(stats.getFinalizacoesMandante() + 1);
                    stats.setFinalizacoesGolMandante(stats.getFinalizacoesGolMandante() + 1);
                } else {
                    stats.setFinalizacoesVisitante(stats.getFinalizacoesVisitante() + 1);
                    stats.setFinalizacoesGolVisitante(stats.getFinalizacoesGolVisitante() + 1);
                }
            }

            case GOL_CONTRA -> {
                if (mandante)
                    stats.setFinalizacoesGolVisitante(stats.getFinalizacoesGolVisitante() + 1);
                else
                    stats.setFinalizacoesGolMandante(stats.getFinalizacoesGolMandante() + 1);
            }

            case FALTA -> {
                if (mandante)
                    stats.setFaltasMandante(stats.getFaltasMandante() + 1);
                else
                    stats.setFaltasVisitante(stats.getFaltasVisitante() + 1);
            }

            case CARTAO_AMARELO -> {
                if (mandante)
                    stats.setCartoesAmarelosMandante(stats.getCartoesAmarelosMandante() + 1);
                else
                    stats.setCartoesAmarelosVisitante(stats.getCartoesAmarelosVisitante() + 1);
            }

            case CARTAO_VERMELHO -> {
                if (mandante)
                    stats.setCartoesVermelhosMandante(stats.getCartoesVermelhosMandante() + 1);
                else
                    stats.setCartoesVermelhosVisitante(stats.getCartoesVermelhosVisitante() + 1);
            }

            case ESCANTEIO -> {
                if (mandante)
                    stats.setEscanteiosMandante(stats.getEscanteiosMandante() + 1);
                else
                    stats.setEscanteiosVisitante(stats.getEscanteiosVisitante() + 1);
            }
        }

        repository.save(stats);
    }
}