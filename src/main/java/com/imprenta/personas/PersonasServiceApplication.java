package com.imprenta.personas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PersonasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonasServiceApplication.class, args);
	}

}
