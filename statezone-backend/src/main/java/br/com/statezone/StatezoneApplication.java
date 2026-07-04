package br.com.statezone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StatezoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatezoneApplication.class, args);
	}

}
