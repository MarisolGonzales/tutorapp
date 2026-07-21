package com.tutorapp.tutorapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.Alumno;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    Optional<Alumno> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}
