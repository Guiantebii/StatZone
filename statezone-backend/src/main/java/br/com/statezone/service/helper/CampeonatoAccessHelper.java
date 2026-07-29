package br.com.statezone.service.helper;

import br.com.statezone.enums.StatusCampeonato;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.model.Campeonato;
import br.com.statezone.model.Partida;
import br.com.statezone.repository.CampeonatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CampeonatoAccessHelper {

    private final CampeonatoRepository campeonatoRepository;

    public Campeonato carregarEValidarVisibilidade(Long campeonatoId) {
        Campeonato campeonato = campeonatoRepository.findById(campeonatoId)
                .orElseThrow(() -> new ResourceNotFoundException("Campeonato não encontrado"));
        validarVisibilidade(campeonato);
        return campeonato;
    }

    public void validarVisibilidade(Campeonato campeonato) {
        if (campeonato == null) {
            throw new ResourceNotFoundException("Campeonato não encontrado");
        }

        if (campeonato.getStatus() == StatusCampeonato.RASCUNHO && !podeVerRascunho()) {
            throw new AccessDeniedException("Campeonato indisponível para acesso público");
        }
    }

    public void validarVisibilidade(Partida partida) {
        if (partida == null) {
            throw new ResourceNotFoundException("Partida não encontrada");
        }

        validarVisibilidade(partida.getCampeonato());
    }

    public boolean podeVerRascunho() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
