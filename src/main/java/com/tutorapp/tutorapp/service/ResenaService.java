package com.tutorapp.tutorapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Resena;
import com.tutorapp.tutorapp.repository.ResenaRepository;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    public void guardar(Resena resena) {
        resenaRepository.save(resena);
    }

    public boolean existeParaSesion(Long idSesion) {
        return resenaRepository.existsBySesion_Id(idSesion);
    }

    public double promedioTutor(Long idTutor) {
        return resenaRepository.findBySesion_ServicioTutor_Tutor_Id(idTutor).stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
    }

    public int totalResenasTutor(Long idTutor) {
        // count en BD: evita traer todas las filas solo para contarlas
        return (int) resenaRepository.countBySesion_ServicioTutor_Tutor_Id(idTutor);
    }
}
