package com.tutorapp.tutorapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.ServicioTutor;

public interface ServicioTutorRepository extends JpaRepository<ServicioTutor, Long> {

    List<ServicioTutor> findByTutor_Id(Long idTutor);


    List<ServicioTutor> findByCurso_IdIn(List<Long> idCursos);
}
