package br.com.statezone.controller;

import br.com.statezone.config.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class SeedController {

    private final DataSeeder dataSeeder;

    @PostMapping("/seed")
    public ResponseEntity<String> seed() {
        new Thread(() -> {
            try {
                dataSeeder.run();
            } catch (Exception e) {
                System.err.println("Seed failed: " + e.getMessage());
            }
        }).start();
        return ResponseEntity.ok("Seed iniciado em background");
    }
}
