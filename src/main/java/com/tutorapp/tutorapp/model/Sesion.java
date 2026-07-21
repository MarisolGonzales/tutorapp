package com.tutorapp.tutorapp.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sesion")
public class Sesion {

    public enum EstadoSesion {
        Pendiente, Confirmada, Cancelada, Completada
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_alumno", nullable = false)
    @NotNull
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "id_servicio_tutor", nullable = false)
    @NotNull(message = "El servicio no puede estar vacío")
    private ServicioTutor servicioTutor;

    @Column(nullable = false)
    @NotNull(message = "La fecha no puede estar vacía")
    private LocalDate fecha;

    @Column(nullable = false)
    @NotNull(message = "La hora no puede estar vacía")
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private EstadoSesion estado;

    // Nombre de la sala Jitsi para la videollamada integrada.
    // Se genera cuando el tutor acepta la solicitud.
    @Column(name = "sala_video", length = 100)
    private String salaVideo;

    public Sesion() {
    }

    public Sesion(Alumno alumno, ServicioTutor servicioTutor, LocalDate fecha, LocalTime hora, EstadoSesion estado) {
        this.alumno = alumno;
        this.servicioTutor = servicioTutor;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public ServicioTutor getServicioTutor() {
        return servicioTutor;
    }

    public void setServicioTutor(ServicioTutor servicioTutor) {
        this.servicioTutor = servicioTutor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public EstadoSesion getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesion estado) {
        this.estado = estado;
    }

    public String getSalaVideo() {
        return salaVideo;
    }

    public void setSalaVideo(String salaVideo) {
        this.salaVideo = salaVideo;
    }
}
