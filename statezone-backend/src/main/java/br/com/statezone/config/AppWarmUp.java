package br.com.statezone.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AppWarmUp {

    private static final Logger log = LoggerFactory.getLogger(AppWarmUp.class);

    @EventListener
    public void warmUp(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Thread thread = new Thread(() -> {
            log.info("Warm-up: inicializando beans em segundo plano...");
            for (String name : context.getBeanDefinitionNames()) {
                try {
                    context.getBean(name);
                } catch (Exception e) {
                    log.debug("Warm-up ignorou bean {}: {}", name, e.getMessage());
                }
            }
            log.info("Warm-up concluído");
        }, "app-warmup");
        thread.setDaemon(true);
        thread.start();
    }
}
