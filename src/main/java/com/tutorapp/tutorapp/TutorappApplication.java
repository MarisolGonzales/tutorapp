package com.tutorapp.tutorapp;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class TutorappApplication {

	// Zona horaria de Perú
	@PostConstruct
	public void configurarZonaHoraria() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TutorappApplication.class, args);
	}

}
