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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Retiro simulado de las ganancias del tutor. El dinero sale de su saldo
 * disponible hacia su Yape
 * o su cuenta bancaria.
 */
@Entity
@Table(name = "retiro")
public class Retiro {

    public enum MetodoRetiro {
        Yape, CuentaBancaria
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tutor", nullable = false)
    @NotNull
    private Tutor tutor;

    @Column(nullable = false)
    @NotNull
    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private MetodoRetiro metodo;

    // Celular Yape o banco + número de cuenta al que se envió el dinero
    @Column(nullable = false, length = 100)
    @NotBlank
    private String destino;

    @Column(nullable = false)
    @NotNull
    private LocalDateTime fechaRetiro;

    public Retiro() {
    }

    public Retiro(Tutor tutor, Double monto, MetodoRetiro metodo, String destino) {
        this.tutor = tutor;
        this.monto = monto;
        this.metodo = metodo;
        this.destino = destino;
        this.fechaRetiro = LocalDateTime.now();
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

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public MetodoRetiro getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoRetiro metodo) {
        this.metodo = metodo;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDateTime getFechaRetiro() {
        return fechaRetiro;
    }

    public void setFechaRetiro(LocalDateTime fechaRetiro) {
        this.fechaRetiro = fechaRetiro;
    }
}
