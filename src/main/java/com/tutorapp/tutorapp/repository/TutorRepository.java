package com.tutorapp.tutorapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);
}
