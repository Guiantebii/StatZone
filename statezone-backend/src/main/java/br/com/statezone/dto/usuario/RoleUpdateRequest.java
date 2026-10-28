package br.com.statezone.dto.usuario;

import br.com.statezone.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull Role role) {
}
