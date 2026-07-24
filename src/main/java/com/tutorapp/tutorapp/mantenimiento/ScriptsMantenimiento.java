package com.tutorapp.tutorapp.mantenimiento;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tutorapp.tutorapp.model.Pago;
import com.tutorapp.tutorapp.model.Pago.EstadoPago;
import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.model.Sesion.EstadoSesion;
import com.tutorapp.tutorapp.repository.PagoRepository;
import com.tutorapp.tutorapp.repository.SesionRepository;

/**
 * Scripts automatizados de mantenimiento del sistema TutorApp.
 *
 * Las tareas se ejecutan de forma programada mediante la anotación
 * {@code @Scheduled}, sin intervención manual, y registran su resultado
 * a través de Logback. Corresponden al mantenimiento preventivo y
 * predictivo descrito en el plan de mantenimiento del sistema.
 */
@Component
public class ScriptsMantenimiento {

    private static final Logger log = LoggerFactory.getLogger(ScriptsMantenimiento.class);

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private PagoRepository pagoRepository;

    /**
     * Resumen diario del estado del sistema.
     * Ejecuta: todos los días a las 02:30 (hora de Perú).
     */
    @Scheduled(cron = "0 30 2 * * *", zone = "America/Lima")
    public void resumenDiarioDelSistema() {
        List<Sesion> sesiones = sesionRepository.findAll();
        long pendientes = sesiones.stream().filter(s -> s.getEstado() == EstadoSesion.Pendiente).count();
        long confirmadas = sesiones.stream().filter(s -> s.getEstado() == EstadoSesion.Confirmada).count();
        long completadas = sesiones.stream().filter(s -> s.getEstado() == EstadoSesion.Completada).count();

        log.info("[MANTENIMIENTO] Resumen diario -> sesiones: {} (pendientes: {}, confirmadas: {}, completadas: {}); pagos registrados: {}",
                sesiones.size(), pendientes, confirmadas, completadas, pagoRepository.count());
    }

    /**
     * Revisión semanal de pagos retenidos en sesiones ya finalizadas.
     * Detecta posibles inconsistencias, ya que un pago debería liberarse
     * o reembolsarse cuando la sesión se completa o se cancela.
     * Ejecuta: los domingos a las 03:00 (hora de Perú).
     */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "America/Lima")
    public void revisionSemanalDePagos() {
        List<Pago> retenidosInconsistentes = pagoRepository.findAll().stream()
                .filter(p -> p.getEstado() == EstadoPago.Retenido)
                .filter(p -> p.getSesion() != null
                        && (p.getSesion().getEstado() == EstadoSesion.Completada
                        || p.getSesion().getEstado() == EstadoSesion.Cancelada))
                .toList();

        if (retenidosInconsistentes.isEmpty()) {
            log.info("[MANTENIMIENTO] Revisión semanal de pagos: sin inconsistencias.");
        } else {
            log.warn("[MANTENIMIENTO] Revisión semanal de pagos: {} pago(s) retenido(s) en sesiones ya finalizadas requieren revisión.",
                    retenidosInconsistentes.size());
        }
    }

    /**
     * Registro periódico de actividad (heartbeat) que confirma que el
     * sistema se encuentra operativo.
     * Ejecuta: cada hora en punto.
     */
    @Scheduled(cron = "0 0 * * * *", zone = "America/Lima")
    public void registrarLatidoDelSistema() {
        log.info("[MANTENIMIENTO] El sistema TutorApp se encuentra operativo.");
    }
}
