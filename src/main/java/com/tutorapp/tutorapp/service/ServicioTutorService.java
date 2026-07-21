package com.tutorapp.tutorapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.ServicioTutor;
import com.tutorapp.tutorapp.repository.ServicioTutorRepository;

@Service
public class ServicioTutorService {

    @Autowired
    private ServicioTutorRepository servicioTutorRepository;

    public List<ServicioTutor> listarPorTutor(Long idTutor) {
        return servicioTutorRepository.findByTutor_Id(idTutor);
    }

    public List<ServicioTutor> listarPorCursos(List<Long> idCursos) {
        return servicioTutorRepository.findByCurso_IdIn(idCursos);
    }

    public Optional<ServicioTutor> buscarPorId(Long id) {
        return servicioTutorRepository.findById(id);
    }

    public void guardar(ServicioTutor servicio) {
        servicioTutorRepository.save(servicio);
    }

    public void eliminar(Long id) {
        servicioTutorRepository.deleteById(id);
    }
}
