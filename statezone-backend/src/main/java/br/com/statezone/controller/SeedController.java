package br.com.statezone.controller;

import br.com.statezone.config.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SeedController {

    private final DataSeeder dataSeeder;
    private static final AtomicBoolean running = new AtomicBoolean(false);
    private static String status = "Aguardando";

    @PostMapping("/seed")
    public ResponseEntity<String> seed() {
        if (running.compareAndSet(false, true)) {
            status = "Executando...";
            new Thread(() -> {
                try {
                    dataSeeder.run();
                    status = "Seed concluído!";
                } catch (Exception e) {
                    status = "Erro: " + e.getMessage();
                    e.printStackTrace();
                } finally {
                    running.set(false);
                }
            }).start();
            return ResponseEntity.ok("Seed iniciado");
        }
        return ResponseEntity.ok("Já está rodando: " + status);
    }

    @GetMapping("/seed-status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(status);
    }
}
