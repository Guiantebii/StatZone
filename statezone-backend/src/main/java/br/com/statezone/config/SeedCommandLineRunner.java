package br.com.statezone.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
@RequiredArgsConstructor
public class SeedCommandLineRunner implements CommandLineRunner {

    private final DataSeeder dataSeeder;

    @Override
    public void run(String... args) throws Exception {
        dataSeeder.run();
    }
}
