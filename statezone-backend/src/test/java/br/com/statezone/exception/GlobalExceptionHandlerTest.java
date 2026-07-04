package br.com.statezone.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFound_deveRetornar404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Time não encontrado");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("message")).isEqualTo("Time não encontrado");
        assertThat(response.getBody().get("status")).isEqualTo(404);
    }

    @Test
    void handleConflict_deveRetornar409() {
        ConflictException ex = new ConflictException("Email já cadastrado");

        ResponseEntity<Map<String, Object>> response = handler.handleConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Email já cadastrado");
    }

    @Test
    void handleUnauthorized_deveRetornar401() {
        UnauthorizedException ex = new UnauthorizedException("Usuário não autenticado");

        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Usuário não autenticado");
    }

    @Test
    void handleBusiness_deveRetornar400() {
        BusinessException ex = new BusinessException("Times não podem ser iguais");

        ResponseEntity<Map<String, Object>> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Times não podem ser iguais");
    }

    @Test
    void handleGeneric_deveRetornar500SemVazarDetalhes() {
        Exception ex = new RuntimeException("Erro interno detalhado");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Erro interno no servidor");
        assertThat(response.getBody().get("message")).isNotEqualTo("Erro interno detalhado");
    }

    @Test
    void responseDeveConterTimestampEStatus() {
        BusinessException ex = new BusinessException("Erro de negócio");

        ResponseEntity<Map<String, Object>> response = handler.handleBusiness(ex);

        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody()).containsKey("message");
    }
}
