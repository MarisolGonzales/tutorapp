package com.tutorapp.tutorapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Pago simulado de una sesión. El alumno paga al reservar; el dinero queda
 * RETENIDO por TutorApp y se LIBERA al tutor cuando la sesión se completa.
 * Si la sesión se cancela, el pago se REEMBOLSA al alumno.
 */

@Entity
@Table(name = "pago")
public class Pago {

    public enum MetodoPago {
        Yape, Tarjeta
    }

    public enum EstadoPago {
        Retenido, Liberado, Reembolsado
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_sesion", nullable = false, unique = true)
    @NotNull
    private Sesion sesion;

    @Column(nullable = false)
    @NotNull
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private MetodoPago metodo;

    // Código de aprobación de Yape o últimos 4 dígitos de la tarjeta
    @Column(nullable = false, length = 50)
    @NotBlank
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private EstadoPago estado;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime fechaPago;

    public Pago() {
    }

    public Pago(Sesion sesion, Double monto, MetodoPago metodo, String referencia) {
        this.sesion = sesion;
        this.monto = monto;
        this.metodo = metodo;
        this.referencia = referencia;
        this.estado = EstadoPago.Retenido;
        this.fechaPago = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
}
