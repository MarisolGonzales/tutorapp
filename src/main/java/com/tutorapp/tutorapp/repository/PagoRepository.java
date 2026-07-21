package com.tutorapp.tutorapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.tutorapp.model.Pago;
import com.tutorapp.tutorapp.model.Pago.EstadoPago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findBySesion_Id(Long idSesion);

    List<Pago> findBySesion_Alumno_Id(Long idAlumno);

    List<Pago> findBySesion_ServicioTutor_Tutor_IdOrderByFechaPagoDesc(Long idTutor);

    List<Pago> findBySesion_ServicioTutor_Tutor_IdAndEstado(Long idTutor, EstadoPago estado);
}
