package br.com.statezone.dto.usuario;

import br.com.statezone.enums.Role;

public record UsuarioResponse(Long id, String email, Role role) {
}
