package com.tutorapp.tutorapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Retiro;
import com.tutorapp.tutorapp.model.Retiro.MetodoRetiro;
import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.repository.RetiroRepository;

import jakarta.validation.Validator;

@Service
public class RetiroService {

    private static final Logger log = LoggerFactory.getLogger(RetiroService.class);

    @Autowired
    private RetiroRepository retiroRepository;

    @Autowired
    private Validator validator;

    /**
     * Valida los datos del retiro y devuelve los errores agrupados por campo
     * (para mostrarlos en rojo debajo de cada input del modal).
     * Devuelve un mapa vacío si todo es válido.
     */
    public Map<String, String> validar(Double monto, double saldoDisponible, String metodoRetiro,
            String celularYape, String banco, String cuentaBancaria) {
        Map<String, String> errores = new HashMap<>();

        if (monto == null) {
            errores.put("monto", "El monto no puede estar vacío");
        } else if (monto <= 0) {
            errores.put("monto", "El monto debe ser mayor a 0");
        } else if (monto > saldoDisponible) {
            errores.put("monto", "El monto supera tu saldo disponible (S/. "
                    + String.format("%.2f", saldoDisponible) + ")");
        }

        if ("Yape".equalsIgnoreCase(metodoRetiro)) {
            // StringUtils.isBlank (Commons): cubre null, cadena vacía y solo espacios
            if (StringUtils.isBlank(celularYape)) {
                errores.put("celularYape", "El número de celular no puede estar vacío");
            } else if (!celularYape.trim().matches("9\\d{8}")) {
                errores.put("celularYape", "El número de celular de Yape debe tener 9 dígitos y empezar con 9");
            }
        } else if ("CuentaBancaria".equalsIgnoreCase(metodoRetiro)) {
            if (StringUtils.isBlank(banco)) {
                errores.put("banco", "El banco no puede estar vacío");
            }
            String digitos = StringUtils.defaultString(cuentaBancaria).replaceAll("[\\s-]", "");
            if (digitos.isEmpty()) {
                errores.put("cuentaBancaria", "El número de cuenta no puede estar vacío");
            } else if (!digitos.matches("\\d{10,20}")) {
                errores.put("cuentaBancaria", "El número de cuenta debe tener entre 10 y 20 dígitos");
            }
        } else {
            errores.put("metodoRetiro", "Selecciona un método de retiro (Yape o Cuenta bancaria)");
        }

        return errores;
    }

    /**
     * Registra el retiro simulado. Los datos deben venir ya validados con
     * validar(); si no lo están, lanza IllegalArgumentException.
     */
    public Retiro registrar(Tutor tutor, Double monto, double saldoDisponible, String metodoRetiro,
            String celularYape, String banco, String cuentaBancaria) {
        Map<String, String> erroresRetiro = validar(monto, saldoDisponible, metodoRetiro,
                celularYape, banco, cuentaBancaria);
        if (!erroresRetiro.isEmpty()) {
            throw new IllegalArgumentException(erroresRetiro.values().iterator().next());
        }

        MetodoRetiro metodo;
        String destino;
        if ("Yape".equalsIgnoreCase(metodoRetiro)) {
            metodo = MetodoRetiro.Yape;
            destino = "Yape " + celularYape.trim();
        } else {
            metodo = MetodoRetiro.CuentaBancaria;
            // Por seguridad no se guarda la cuenta completa: solo los últimos 4 dígitos (****5432)
            String digitos = cuentaBancaria.replaceAll("[\\s-]", "");
            destino = banco.trim() + " · Cta. " + enmascararCuenta(digitos);
        }

        Retiro retiro = new Retiro(tutor, monto, metodo, destino);

        // Valida el retiro con las anotaciones de la entity
        var errores = validator.validate(retiro);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(errores.iterator().next().getMessage());
        }

        // El destino ya viaja enmascarado: nunca se registra la cuenta completa
        log.info("Retiro registrado por {} para el tutor {} (método: {})",
                monto, tutor.getId(), metodo);
        return retiroRepository.save(retiro);
    }

    /** Deja visibles solo los últimos 4 dígitos de la cuenta (ej. ****5432). */
    private String enmascararCuenta(String digitos) {
        // StringUtils.right (Commons) evita calcular índices y no falla con cadenas cortas
        return "****" + StringUtils.right(digitos, 4);
    }

    public List<Retiro> retirosDeTutor(Long idTutor) {
        return retiroRepository.findByTutor_IdOrderByFechaRetiroDesc(idTutor);
    }

    /** Total ya retirado por el tutor (para calcular su saldo disponible). */
    public double totalRetirado(Long idTutor) {
        return retiroRepository.sumMontoByTutorId(idTutor);
    }
}
