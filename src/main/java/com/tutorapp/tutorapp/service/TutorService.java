package com.tutorapp.tutorapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.repository.TutorRepository;

@Service
public class TutorService {

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Tutor> listarTodos() {
        return tutorRepository.findAll();
    }

    public Optional<Tutor> buscarPorEmail(String email) {
        return tutorRepository.findByEmail(email);
    }

    public Optional<Tutor> buscarPorId(Long id) {
        return tutorRepository.findById(id);
    }

    public boolean existeEmail(String email) {
        return tutorRepository.existsByEmailIgnoreCase(email);
    }

    public void registrar(Tutor tutor) {
        tutor.setContrasena(passwordEncoder.encode(tutor.getContrasena()));
        tutorRepository.save(tutor);
    }

    public void actualizar(Tutor tutor) {
        tutorRepository.save(tutor);
    }
}
