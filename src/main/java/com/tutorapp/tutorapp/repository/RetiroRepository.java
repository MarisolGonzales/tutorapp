package com.tutorapp.tutorapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tutorapp.tutorapp.model.Retiro;

@Repository
public interface RetiroRepository extends JpaRepository<Retiro, Long> {

    List<Retiro> findByTutor_IdOrderByFechaRetiroDesc(Long idTutor);

    // Suma en BD
    @Query("SELECT COALESCE(SUM(r.monto), 0) FROM Retiro r WHERE r.tutor.id = :idTutor")
    double sumMontoByTutorId(Long idTutor);
}

