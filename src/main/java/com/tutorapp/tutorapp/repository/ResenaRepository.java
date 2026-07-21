package com.tutorapp.tutorapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findBySesion_ServicioTutor_Tutor_Id(Long idTutor);

    boolean existsBySesion_Id(Long idSesion);

    long countBySesion_ServicioTutor_Tutor_Id(Long idTutor);
}
