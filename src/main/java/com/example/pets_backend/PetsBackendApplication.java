package com.example.pets_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetsBackendApplication.class, args);
	}
}
