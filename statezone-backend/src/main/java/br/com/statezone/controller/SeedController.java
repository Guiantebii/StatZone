package br.com.statezone.controller;

import br.com.statezone.config.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                e.printStackTrace();
            }
        }).start();
        return ResponseEntity.ok("Seed iniciado em background");
    }
}
