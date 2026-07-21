package com.tutorapp.tutorapp.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Alumno;
import com.tutorapp.tutorapp.repository.AlumnoRepository;

@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<Alumno> buscarPorId(Long id) {
        return alumnoRepository.findById(id);
    }

    public Optional<Alumno> buscarPorEmail(String email) {
        return alumnoRepository.findByEmail(email);
    }

    public boolean existeEmail(String email) {
        return alumnoRepository.existsByEmailIgnoreCase(email);
    }

    public void registrar(Alumno alumno) {
        alumno.setContrasena(passwordEncoder.encode(alumno.getContrasena()));
        alumnoRepository.save(alumno);
    }

    public void actualizar(Alumno alumno) {
        alumnoRepository.save(alumno);
    }
}
