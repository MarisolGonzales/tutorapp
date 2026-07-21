package com.tutorapp.tutorapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Alumno;
import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.repository.AlumnoRepository;
import com.tutorapp.tutorapp.repository.TutorRepository;

/**
 * Carga el usuario para Spring Security. El "username" es el email:
 * primero se busca entre los tutores y luego entre los alumnos,
 * asignando el rol TUTOR o ALUMNO según dónde se encuentre.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Tutor tutor = tutorRepository.findByEmail(email).orElse(null);
        if (tutor != null) {
            return new User(tutor.getEmail(), tutor.getContrasena(),
                    List.of(new SimpleGrantedAuthority("ROLE_TUTOR")));
        }

        Alumno alumno = alumnoRepository.findByEmail(email).orElse(null);
        if (alumno != null) {
            return new User(alumno.getEmail(), alumno.getContrasena(),
                    List.of(new SimpleGrantedAuthority("ROLE_ALUMNO")));
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }
}
