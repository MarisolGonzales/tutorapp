package com.tutorapp.tutorapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findByNombreContainingIgnoreCase(String nombre);

    Optional<Curso> findByNombreIgnoreCase(String nombre);
}
