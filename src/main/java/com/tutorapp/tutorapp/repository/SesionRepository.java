package com.tutorapp.tutorapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.model.Sesion.EstadoSesion;

public interface SesionRepository extends JpaRepository<Sesion, Long> {

    List<Sesion> findByServicioTutor_Tutor_IdAndEstado(Long idTutor, EstadoSesion estado);

    List<Sesion> findByAlumno_Id(Long idAlumno);

    List<Sesion> findByAlumno_IdAndEstado(Long idAlumno, EstadoSesion estado);

    List<Sesion> findByServicioTutor_Tutor_IdAndEstadoIn(Long idTutor, List<EstadoSesion> estados);

    List<Sesion> findByAlumno_IdAndEstadoIn(Long idAlumno, List<EstadoSesion> estados);
}
