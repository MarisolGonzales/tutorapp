package com.tutorapp.tutorapp.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tutorapp.tutorapp.model.Alumno;
import com.tutorapp.tutorapp.model.Pago;
import com.tutorapp.tutorapp.model.Resena;
import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.model.Sesion.EstadoSesion;
import com.tutorapp.tutorapp.model.ServicioTutor;
import com.tutorapp.tutorapp.service.AlumnoService;
import com.tutorapp.tutorapp.service.FotoPerfilService;
import com.tutorapp.tutorapp.service.PagoService;
import com.tutorapp.tutorapp.service.ReciboPdfService;
import com.tutorapp.tutorapp.service.ResenaService;
import com.tutorapp.tutorapp.service.SesionService;
import com.tutorapp.tutorapp.service.ServicioTutorService;
import com.tutorapp.tutorapp.service.TutorService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@Controller
public class AlumnoController {

    private static final Logger log = LoggerFactory.getLogger(AlumnoController.class);

    @Autowired
    private ReciboPdfService reciboPdfService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private SesionService sesionService;

    @Autowired
    private ServicioTutorService servicioTutorService;

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private FotoPerfilService fotoPerfilService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private Validator validator;

    @GetMapping("/alumno/panel")
    public String panelAlumno(HttpSession session, Model model) {
        Long idAlumno = (Long) session.getAttribute("usuarioId");
        if (idAlumno == null) return "redirect:/login";

        List<Sesion> historial = sesionService.historialAlumno(idAlumno);
        // Solo las completadas pueden tener reseña: evita consultar por las canceladas
        Set<Long> sesionesConResena = historial.stream()
                .filter(s -> s.getEstado() == EstadoSesion.Completada)
                .map(Sesion::getId)
                .filter(resenaService::existeParaSesion)
                .collect(Collectors.toSet());

        model.addAttribute("alumno", alumnoService.buscarPorId(idAlumno).orElse(null));
        model.addAttribute("sesionesConfirmadas", sesionService.sesionesConfirmadasAlumno(idAlumno));
        model.addAttribute("sesionesPendientes", sesionService.sesionesPendientesAlumno(idAlumno));
        model.addAttribute("historial", historial);
        model.addAttribute("sesionesConResena", sesionesConResena);
        model.addAttribute("pagosPorSesion", pagoService.pagosPorSesionDeAlumno(idAlumno));
        return "panel-alumno";
    }

    /**
     * Descarga en PDF el comprobante de pago de una sesión.
     * Solo el alumno dueño de la sesión puede obtener su propio recibo.
     */
    @GetMapping("/alumno/sesion/{idSesion}/recibo")
    public ResponseEntity<byte[]> descargarRecibo(@PathVariable Long idSesion, HttpSession session) {
        Long idAlumno = (Long) session.getAttribute("usuarioId");
        if (idAlumno == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pago pago = pagoService.buscarPorSesion(idSesion).orElse(null);
        // Se verifica que el pago exista y que pertenezca al alumno autenticado
        if (pago == null || !pago.getSesion().getAlumno().getId().equals(idAlumno)) {
            log.warn("Intento de descarga de un recibo ajeno: alumno {} sobre la sesión {}", idAlumno, idSesion);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] archivo = reciboPdfService.generarRecibo(pago);
        log.info("El alumno {} descargó el recibo de la sesión {}", idAlumno, idSesion);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=recibo-tutorapp-" + pago.getId() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(archivo);
    }

    @PostMapping("/registro-alumno")
    public String registrar(@Valid @ModelAttribute Alumno alumno, BindingResult result,
            @RequestParam String confirmarContrasena, RedirectAttributes ra) {
        if (!result.hasFieldErrors("contrasena") && !alumno.getContrasena().equals(confirmarContrasena)) {
            result.rejectValue("contrasena", "error.alumno", "Las contraseñas no coinciden.");
        }
        // El correo debe ser único en TODA la app: ni otro alumno ni un tutor pueden tenerlo
        if (!result.hasFieldErrors("email")
                && (alumnoService.existeEmail(alumno.getEmail()) || tutorService.existeEmail(alumno.getEmail()))) {
            result.rejectValue("email", "error.alumno", "Ya existe una cuenta con ese correo.");
        }
        // Si hay errores se vuelve a la misma vista: el formulario conserva
        // lo escrito y cada campo muestra su mensaje (th:errors).
        if (result.hasErrors()) {
            return "registro-alumno";
        }
        try {
            alumnoService.registrar(alumno);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.alumno", "Ya existe una cuenta con ese correo.");
            return "registro-alumno";
        }
        ra.addFlashAttribute("mensaje", "¡Cuenta creada! Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    @PostMapping("/alumno/editar-perfil")
    public String editarPerfil(@RequestParam String nombre,
            @RequestParam(required = false) String universidad,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) String cicloAcademico,
            @RequestParam(required = false) MultipartFile foto,
            HttpSession session,
            RedirectAttributes ra) {
        Long idAlumno = (Long) session.getAttribute("usuarioId");
        if (idAlumno == null) return "redirect:/login";

        Alumno alumno = alumnoService.buscarPorId(idAlumno).orElse(null);
        if (alumno == null) return "redirect:/login";

        alumno.setNombre(nombre);
        alumno.setUniversidad(universidad);
        alumno.setCarrera(carrera);
        alumno.setCicloAcademico(cicloAcademico);

        // Los errores se agrupan por campo para mostrarlos en rojo debajo de
        // cada input del modal, igual que en los formularios de registro.
        Map<String, String> erroresPerfil = new HashMap<>();

        if (foto != null && !foto.isEmpty()) {
            try {
                alumno.setFotoPerfil(fotoPerfilService.guardar(foto));
            } catch (IllegalArgumentException e) {
                erroresPerfil.put("foto", e.getMessage());
            }
        }

        // Valida los cambios con las anotaciones de la entity
        validator.validate(alumno)
                .forEach(v -> erroresPerfil.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

        if (!erroresPerfil.isEmpty()) {
            ra.addFlashAttribute("erroresPerfil", erroresPerfil);
            return "redirect:/alumno/panel";
        }

        alumnoService.actualizar(alumno);
        session.setAttribute("usuarioNombre", nombre);
        ra.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
        return "redirect:/alumno/panel";
    }

    @PostMapping("/alumno/resena")
    public String dejarResena(@RequestParam Long idSesion,
            @RequestParam Integer calificacion,
            @RequestParam(required = false) String comentario,
            RedirectAttributes ra) {
        if (resenaService.existeParaSesion(idSesion)) {
            ra.addFlashAttribute("error", "Ya calificaste esta sesión.");
            return "redirect:/alumno/panel";
        }

        Sesion sesion = sesionService.buscarPorId(idSesion).orElse(null);
        if (sesion == null) {
            ra.addFlashAttribute("error", "No se encontró la sesión a calificar.");
            return "redirect:/alumno/panel";
        }

        Resena resena = new Resena(sesion, calificacion, comentario);

        // Valida la reseña con las anotaciones de la entity
        var errores = validator.validate(resena);
        if (!errores.isEmpty()) {
            ra.addFlashAttribute("error", errores.iterator().next().getMessage());
            return "redirect:/alumno/panel";
        }

        resenaService.guardar(resena);
        ra.addFlashAttribute("mensaje", "¡Gracias por tu calificación!");
        return "redirect:/alumno/panel";
    }

    @PostMapping("/alumno/agendar")
    public String agendar(@RequestParam(required = false) Long idServicioTutor,
            // ISO: los inputs <date> y <time> del navegador envían yyyy-MM-dd y HH:mm.
            // Sin esto Spring intenta parsear con el formato del idioma del usuario y falla.
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime hora,
            @RequestParam(required = false) String metodoPago,
            @RequestParam(required = false) String celularYape,
            @RequestParam(required = false) String codigoAprobacion,
            @RequestParam(required = false) String numeroTarjeta,
            @RequestParam(required = false) String origen,
            HttpSession session,
            RedirectAttributes ra) {
        Long idAlumno = (Long) session.getAttribute("usuarioId");
        if (idAlumno == null) return "redirect:/login";

        // Página desde la que se reservó (búsqueda o perfil del tutor),
        // para volver ahí y reabrir el modal si hay errores.
        String volver = "redirect:" + (origen != null && origen.startsWith("/") ? origen : "/busqueda-tutores");

        Alumno alumno = alumnoService.buscarPorId(idAlumno).orElse(null);
        if (alumno == null) return "redirect:/login";

        ServicioTutor servicio = idServicioTutor == null
                ? null
                : servicioTutorService.buscarPorId(idServicioTutor).orElse(null);

        Sesion sesion = new Sesion(alumno, servicio, fecha, hora, EstadoSesion.Pendiente);

        // Los errores se agrupan por campo para mostrarlos en rojo debajo de
        // cada input del modal, igual que en los formularios de registro.
        Map<String, String> erroresReserva = new HashMap<>();

        // Valida la sesión con las anotaciones de la entity (@NotNull...)
        validator.validate(sesion)
                .forEach(v -> erroresReserva.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

        if (fecha != null && fecha.isBefore(LocalDate.now())) {
            erroresReserva.put("fecha", "La fecha de la sesión no puede ser pasada");
        } else if (fecha != null && hora != null
                // Se compara solo hasta los minutos: el input no maneja segundos,
                // así reservar en el minuto actual sigue siendo válido.
                && LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
            erroresReserva.put("hora", "La hora de la sesión no puede ser pasada");
        }

        // Valida los datos del pago (método, celular/código de Yape o tarjeta)
        erroresReserva.putAll(pagoService.validar(metodoPago, celularYape, codigoAprobacion, numeroTarjeta));

        if (!erroresReserva.isEmpty()) {
            ra.addFlashAttribute("erroresReserva", erroresReserva);

            // Lo que el alumno había llenado, para no perderlo al reabrir el modal
            Map<String, Object> reservaValores = new HashMap<>();
            reservaValores.put("idTutor", servicio != null ? servicio.getTutor().getId() : null);
            reservaValores.put("idServicioTutor", idServicioTutor);
            reservaValores.put("fecha", fecha != null ? fecha.toString() : "");
            reservaValores.put("hora", hora != null ? hora.toString() : "");
            reservaValores.put("metodoPago", metodoPago != null ? metodoPago : "");
            reservaValores.put("celularYape", celularYape != null ? celularYape : "");
            reservaValores.put("codigoAprobacion", codigoAprobacion != null ? codigoAprobacion : "");
            ra.addFlashAttribute("reservaValores", reservaValores);
            return volver;
        }

        sesionService.agendar(sesion);

        // Pago simulado: el dinero queda retenido por el sistema hasta que
        // la sesión se complete. Si los datos del pago no son válidos, se
        // deshace la reserva para no dejar una sesión sin pagar.
        try {
            pagoService.registrar(sesion, metodoPago, celularYape, codigoAprobacion, numeroTarjeta);
        } catch (IllegalArgumentException e) {
            sesionService.eliminar(sesion.getId());
            ra.addFlashAttribute("error", e.getMessage());
            return volver;
        }

        ra.addFlashAttribute("mensaje",
                "¡Pago recibido y solicitud enviada! El tutor confirmará pronto. "
                + "Si la sesión se cancela, se te reembolsará el monto.");
        return "redirect:/alumno/panel";
    }

    @PostMapping("/alumno/cancelar/{id}")
    public String cancelar(@PathVariable Long id, RedirectAttributes ra) {
        sesionService.cancelarComoAlumno(id);
        ra.addFlashAttribute("mensaje", "Sesión cancelada.");
        return "redirect:/alumno/panel";
    }
}
