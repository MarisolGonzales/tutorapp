package com.tutorapp.tutorapp.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.model.Sesion.EstadoSesion;
import com.tutorapp.tutorapp.repository.SesionRepository;

@Service
public class SesionService {

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private PagoService pagoService;

    // tutor

    public List<Sesion> confirmadas(Long idTutor) {
        return sesionRepository.findByServicioTutor_Tutor_IdAndEstado(idTutor, EstadoSesion.Confirmada);
    }

    public List<Sesion> pendientes(Long idTutor) {
        return sesionRepository.findByServicioTutor_Tutor_IdAndEstado(idTutor, EstadoSesion.Pendiente);
    }

    public void aceptar(Long id) {
        sesionRepository.findById(id).ifPresent(s -> {
            s.setEstado(EstadoSesion.Confirmada);
            // Genera la sala de videollamada (id + token aleatorio para que
            // nadie pueda adivinar el nombre de la sala de otra sesión)
            if (s.getSalaVideo() == null) {
                s.setSalaVideo("TutorApp-" + s.getId() + "-"
                        + UUID.randomUUID().toString().substring(0, 8));
            }
            sesionRepository.save(s);
        });
    }

    public void cancelarComoTutor(Long id) {
        sesionRepository.findById(id).ifPresent(s -> {
            s.setEstado(EstadoSesion.Cancelada);
            sesionRepository.save(s);
            // El sistema devuelve el dinero al alumno
            pagoService.reembolsarPorSesion(id);
        });
    }

    // alumno

    public List<Sesion> sesionesDelAlumno(Long idAlumno) {
        return sesionRepository.findByAlumno_Id(idAlumno);
    }

    public List<Sesion> sesionesConfirmadasAlumno(Long idAlumno) {
        return sesionRepository.findByAlumno_IdAndEstado(idAlumno, EstadoSesion.Confirmada);
    }

    public List<Sesion> sesionesPendientesAlumno(Long idAlumno) {
        return sesionRepository.findByAlumno_IdAndEstado(idAlumno, EstadoSesion.Pendiente);
    }

    public void cancelarComoAlumno(Long id) {
        sesionRepository.findById(id).ifPresent(s -> {
            s.setEstado(EstadoSesion.Cancelada);
            sesionRepository.save(s);
            // El sistema devuelve el dinero al alumno
            pagoService.reembolsarPorSesion(id);
        });
    }

    public void completar(Long id) {
        sesionRepository.findById(id).ifPresent(s -> {
            s.setEstado(EstadoSesion.Completada);
            sesionRepository.save(s);
            // El sistema transfiere el dinero retenido al tutor
            pagoService.liberarPorSesion(id);
        });
    }

    public void agendar(Sesion sesion) {
        sesionRepository.save(sesion);
    }

    public Optional<Sesion> buscarPorId(Long id) {
        return sesionRepository.findById(id);
    }

    // historial

    public List<Sesion> historialTutor(Long idTutor) {
        return sesionRepository.findByServicioTutor_Tutor_IdAndEstadoIn(
                idTutor, List.of(EstadoSesion.Completada, EstadoSesion.Cancelada));
    }

    public List<Sesion> historialAlumno(Long idAlumno) {
        return sesionRepository.findByAlumno_IdAndEstadoIn(
                idAlumno, List.of(EstadoSesion.Completada, EstadoSesion.Cancelada));
    }

    // para ambos

    public void eliminar(Long id) {
        // Primero se elimina el pago asociado (si existe) para no violar la FK
        pagoService.eliminarPorSesion(id);
        sesionRepository.deleteById(id);
    }
}
