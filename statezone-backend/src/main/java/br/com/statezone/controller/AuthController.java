package br.com.statezone.controller;


import br.com.statezone.dto.security.LoginRequest;
import br.com.statezone.dto.security.LoginResponse;
import br.com.statezone.dto.security.RegistroRequest;
import br.com.statezone.enums.Role;
import br.com.statezone.exception.ConflictException;
import br.com.statezone.model.Usuario;
import br.com.statezone.repository.UsuarioRepository;
import br.com.statezone.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.gerarToken(user);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/registro")
    public ResponseEntity<Void> registro(@RequestBody @Valid RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email já cadastrado");
        }
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .senha(passwordEncoder.encode(request.senha()))
                .role(Role.USER)
                .build();
        usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}