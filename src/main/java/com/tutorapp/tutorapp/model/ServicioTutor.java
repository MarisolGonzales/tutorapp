package com.tutorapp.tutorapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "servicio_tutor")
public class ServicioTutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tutor", nullable = false)
    @NotNull
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "id_curso", nullable = false)
    @NotNull(message = "El curso no puede estar vacío")
    private Curso curso;

    @Column(nullable = false)
    @NotNull(message = "El precio no puede estar vacío")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    // Todas las tutorías son virtuales
    @Column(nullable = false, length = 20)
    @NotBlank(message = "La modalidad no puede estar vacía")
    @Pattern(
        regexp = "^Virtual$",
        message = "La modalidad debe ser Virtual"
    )
    private String modalidad;

    @Column(length = 500)
    private String descripcion;

    public ServicioTutor() {
    }

    public ServicioTutor(Tutor tutor, Curso curso, Double precio, String modalidad, String descripcion) {
        this.tutor = tutor;
        this.curso = curso;
        this.precio = precio;
        this.modalidad = modalidad;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
