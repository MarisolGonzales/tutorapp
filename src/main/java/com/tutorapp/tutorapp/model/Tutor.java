package com.tutorapp.tutorapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tutor")
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Las regex permiten "" para que el campo vacío solo dispare @NotBlank
    // y el formato solo se valide cuando el usuario escribió algo.
    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "La longitud máxima es de 100 caracteres")
    @Pattern(
        regexp = "^$|^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    private String nombre;

    @Column(nullable = false, length = 100, unique = true)
    @NotBlank(message = "El email no puede estar vacío")
    @Pattern(
        regexp = "^$|^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$",
        message = "El email debe tener un dominio válido (ej: ejemplo@correo.com)"
    )
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña no puede estar vacía")
    @Pattern(regexp = "^$|^.{8,}$", message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La universidad no puede estar vacía")
    private String universidad;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La carrera no puede estar vacía")
    private String carrera;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Ingresa el ciclo académico en el que te encuentras")
    private String cicloAcademico;

    // URL pública de la foto de perfil en Cloudinary (https://res.cloudinary.com/...)
    @Column(length = 300)
    private String fotoPerfil;

    // Descripción personal del tutor ("Sobre mí"), opcional
    @Column(length = 1000)
    private String descripcion;

    public Tutor() {
    }

    public Tutor(String nombre, String email, String contrasena,
            String universidad, String carrera, String cicloAcademico) {
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.universidad = universidad;
        this.carrera = carrera;
        this.cicloAcademico = cicloAcademico;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public String getCarrera() {
        return carrera;
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
    }

    public String getCicloAcademico() {
        return cicloAcademico;
    }

    public void setCicloAcademico(String cicloAcademico) {
        this.cicloAcademico = cicloAcademico;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
