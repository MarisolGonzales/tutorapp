package com.tutorapp.tutorapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.tutorapp.tutorapp.model.Pago;
import com.tutorapp.tutorapp.model.Pago.EstadoPago;
import com.tutorapp.tutorapp.model.Pago.MetodoPago;
import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.repository.PagoRepository;

import jakarta.validation.Validator;

@Service
public class PagoService {

    private static final Logger log = LoggerFactory.getLogger(PagoService.class);

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private Validator validator;

    /**
     * Valida los datos del pago y devuelve los errores agrupados por campo
     * Devuelve un mapa vacío si todo es válido.
     */

    public Map<String, String> validar(String metodoPago, String celularYape,
            String codigoAprobacion, String numeroTarjeta) {
        Map<String, String> errores = new HashMap<>();

        if ("Yape".equalsIgnoreCase(metodoPago)) {
            
            if (StringUtils.isBlank(celularYape)) {
                errores.put("celularYape", "El número de celular no puede estar vacío");
            } else if (!celularYape.trim().matches("9\\d{8}")) {
                errores.put("celularYape", "El número de celular de Yape debe tener 9 dígitos y empezar con 9");
            }
            if (StringUtils.isBlank(codigoAprobacion)) {
                errores.put("codigoAprobacion", "El código de aprobación no puede estar vacío");
            } else if (!codigoAprobacion.trim().matches("\\d{6}")) {
                errores.put("codigoAprobacion", "El código de aprobación de Yape debe tener exactamente 6 dígitos");
            }
        } else if ("Tarjeta".equalsIgnoreCase(metodoPago)) {
            
            String digitos = StringUtils.deleteWhitespace(Strings.nullToEmpty(numeroTarjeta));
            if (digitos.isEmpty()) {
                errores.put("numeroTarjeta", "El número de tarjeta no puede estar vacío");
            } else if (!digitos.matches("\\d{16}")) {
                errores.put("numeroTarjeta", "El número de tarjeta debe tener 16 dígitos");
            }
        } else {
            errores.put("metodoPago", "Selecciona un método de pago (Yape o Tarjeta)");
        }

        return errores;
    }

    /**
     * Registra el pago simulado de una sesión y deja el dinero RETENIDO por el
     * sistema hasta que la sesión se complete.
     */

    public Pago registrar(Sesion sesion, String metodoPago, String celularYape,
            String codigoAprobacion, String numeroTarjeta) {
        Map<String, String> erroresPago = validar(metodoPago, celularYape, codigoAprobacion, numeroTarjeta);
        if (!erroresPago.isEmpty()) {
            throw new IllegalArgumentException(erroresPago.values().iterator().next());
        }

        MetodoPago metodo;
        String referencia;
        if ("Yape".equalsIgnoreCase(metodoPago)) {
            metodo = MetodoPago.Yape;
            referencia = "Yape " + celularYape.trim() + " · Cód. " + codigoAprobacion.trim();
        } else {
            metodo = MetodoPago.Tarjeta;
            // Por seguridad solo se guardan los 4 últimos dígitos, nunca la tarjeta completa
            String digitos = StringUtils.deleteWhitespace(numeroTarjeta);
            referencia = "Tarjeta **** " + StringUtils.right(digitos, 4);
        }

        Pago pago = new Pago(sesion, sesion.getServicioTutor().getPrecio(), metodo, referencia);

        // Valida el pago con las anotaciones de la entity 
        var errores = validator.validate(pago);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(errores.iterator().next().getMessage());
        }

        // Se registra el evento sin datos sensibles
        log.info("Pago retenido por {} para la sesión {} (método: {})",
                pago.getMonto(), sesion.getId(), metodo);
        return pagoRepository.save(pago);
    }

    //El tutor completó la sesión: el sistema le transfiere el dinero.
    public void liberarPorSesion(Long idSesion) {
        pagoRepository.findBySesion_Id(idSesion).ifPresent(p -> {
            if (p.getEstado() == EstadoPago.Retenido) {
                p.setEstado(EstadoPago.Liberado);
                pagoRepository.save(p);
                log.info("Pago liberado al tutor por la sesión {}", idSesion);
            }
        });
    }

    // La sesión se canceló o rechazó: el sistema devuelve el dinero al alumno.
    public void reembolsarPorSesion(Long idSesion) {
        pagoRepository.findBySesion_Id(idSesion).ifPresent(p -> {
            if (p.getEstado() == EstadoPago.Retenido) {
                p.setEstado(EstadoPago.Reembolsado);
                pagoRepository.save(p);
                log.info("Pago reembolsado al alumno por la sesión {}", idSesion);
            }
        });
    }

    
    //El tutor rechazó la solicitud): se borra su pago
    
    public void eliminarPorSesion(Long idSesion) {
        pagoRepository.findBySesion_Id(idSesion).ifPresent(pagoRepository::delete);
    }

    public Optional<Pago> buscarPorSesion(Long idSesion) {
        return pagoRepository.findBySesion_Id(idSesion);
    }

    public List<Pago> pagosDeTutor(Long idTutor) {
        return pagoRepository.findBySesion_ServicioTutor_Tutor_IdOrderByFechaPagoDesc(idTutor);
    }

    // Mapa idSesion -> Pago con todos los pagos del alumno
    public Map<Long, Pago> pagosPorSesionDeAlumno(Long idAlumno) {
        Map<Long, Pago> map = new HashMap<>();
        pagoRepository.findBySesion_Alumno_Id(idAlumno)
                .forEach(p -> map.put(p.getSesion().getId(), p));
        return map;
    }

    // Total ya transferido al tutor (sesiones completadas).
    public double totalLiberado(Long idTutor) {
        return sumar(pagoRepository.findBySesion_ServicioTutor_Tutor_IdAndEstado(idTutor, EstadoPago.Liberado));
    }

    // Total retenido por el sistema (sesiones aún no completadas).
    public double totalRetenido(Long idTutor) {
        return sumar(pagoRepository.findBySesion_ServicioTutor_Tutor_IdAndEstado(idTutor, EstadoPago.Retenido));
    }

    private double sumar(List<Pago> pagos) {
        return pagos.stream().mapToDouble(Pago::getMonto).sum();
    }
}
