package br.com.statezone.repository;

import br.com.statezone.enums.StatusPartida;
import br.com.statezone.model.Partida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PartidaRepositoryCustom {

    Page<Partida> buscarFiltradas(StatusPartida status, Long campeonatoId, boolean incluirRascunho, Pageable pageable);
}
