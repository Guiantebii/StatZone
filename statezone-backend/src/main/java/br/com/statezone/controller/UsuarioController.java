package br.com.statezone.controller;

import br.com.statezone.dto.usuario.RoleUpdateRequest;
import br.com.statezone.dto.usuario.UsuarioResponse;
import br.com.statezone.exception.BusinessException;
import br.com.statezone.exception.ResourceNotFoundException;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @PatchMapping("/{id}/role")
    public UsuarioResponse alterarRole(@PathVariable Long id, @RequestBody @Valid RoleUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && usuario.getEmail().equals(auth.getName())) {
            throw new BusinessException("Não é possível alterar o próprio papel");
        }
        usuario.setRole(request.role());
        return toResponse(usuarioRepository.save(usuario));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getRole());
    }
}
