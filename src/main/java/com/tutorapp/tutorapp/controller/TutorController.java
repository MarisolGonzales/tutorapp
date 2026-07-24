package com.tutorapp.tutorapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.service.AlumnoService;
import com.tutorapp.tutorapp.service.FotoPerfilService;
import com.tutorapp.tutorapp.service.PagoService;
import com.tutorapp.tutorapp.service.ReporteExcelService;
import com.tutorapp.tutorapp.service.RetiroService;
import com.tutorapp.tutorapp.service.TutorService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@Controller
public class TutorController {

    private static final Logger log = LoggerFactory.getLogger(TutorController.class);

    @Autowired
    private ReporteExcelService reporteExcelService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private AlumnoService alumnoService;

    @Autowired
    private FotoPerfilService fotoPerfilService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private RetiroService retiroService;

    @Autowired
    private Validator validator;

    @PostMapping("/registro-tutor")
    public String registrar(@Valid @ModelAttribute Tutor tutor, BindingResult result,
            @RequestParam String confirmarContrasena, RedirectAttributes ra) {
        if (!result.hasFieldErrors("contrasena") && !tutor.getContrasena().equals(confirmarContrasena)) {
            result.rejectValue("contrasena", "error.tutor", "Las contraseñas no coinciden.");
        }
        // El correo debe ser único en TODA la app
        if (!result.hasFieldErrors("email")
                && (tutorService.existeEmail(tutor.getEmail()) || alumnoService.existeEmail(tutor.getEmail()))) {
            result.rejectValue("email", "error.tutor", "Ya existe una cuenta con ese correo.");
        }
        // Si hay errores se vuelve a la misma vista: el formulario conserva
        // lo escrito y cada campo muestra su mensaje
        if (result.hasErrors()) {
            return "registro-tutor";
        }
        try {
            tutorService.registrar(tutor);
        } catch (DataIntegrityViolationException e) {
            result.rejectValue("email", "error.tutor", "Ya existe una cuenta con ese correo.");
            return "registro-tutor";
        }
        ra.addFlashAttribute("mensaje", "¡Cuenta creada! Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    /**
     * Descarga en Excel el detalle de pagos del tutor autenticado.
     * Solo se exportan los pagos del propio tutor, tomados de su sesión activa.
     */
    @GetMapping("/tutor/pagos/excel")
    public ResponseEntity<byte[]> descargarPagosExcel(HttpSession session) {
        Long idTutor = (Long) session.getAttribute("usuarioId");
        if (idTutor == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        byte[] archivo = reporteExcelService.generarReportePagos(pagoService.pagosDeTutor(idTutor));
        log.info("El tutor {} descargó su reporte de pagos en Excel", idTutor);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pagos-tutorapp.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }

    @PostMapping("/tutor/editar-perfil")
    public String editarPerfil(@RequestParam String nombre,
            @RequestParam String universidad,
            @RequestParam String carrera,
            @RequestParam String cicloAcademico,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) MultipartFile foto,
            HttpSession session,
            RedirectAttributes ra) {
        Long idTutor = (Long) session.getAttribute("usuarioId");
        if (idTutor == null) return "redirect:/login";

        Tutor tutor = tutorService.buscarPorId(idTutor).orElse(null);
        if (tutor == null) return "redirect:/login";

        tutor.setNombre(nombre);
        tutor.setUniversidad(universidad);
        tutor.setCarrera(carrera);
        tutor.setCicloAcademico(cicloAcademico);
        // Se limpia a null si viene vacío, para no guardar cadenas en blanco
        tutor.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);

        // Los errores se agrupan por campo para mostrarlos en rojo debajo
        Map<String, String> erroresPerfil = new HashMap<>();

        if (foto != null && !foto.isEmpty()) {
            try {
                tutor.setFotoPerfil(fotoPerfilService.guardar(foto));
            } catch (IllegalArgumentException e) {
                erroresPerfil.put("foto", e.getMessage());
            }
        }

        // Valida los cambios con las anotaciones de la entity
        validator.validate(tutor)
                .forEach(v -> erroresPerfil.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));

        if (!erroresPerfil.isEmpty()) {
            ra.addFlashAttribute("erroresPerfil", erroresPerfil);
            return "redirect:/tutor/panel";
        }

        tutorService.actualizar(tutor);
        session.setAttribute("usuarioNombre", nombre);
        ra.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
        return "redirect:/tutor/panel";
    }

    @PostMapping("/tutor/retirar")
    public String retirar(@RequestParam(required = false) String monto,
            @RequestParam(required = false) String metodoRetiro,
            @RequestParam(required = false) String celularYape,
            @RequestParam(required = false) String banco,
            @RequestParam(required = false) String cuentaBancaria,
            HttpSession session,
            RedirectAttributes ra) {
        Long idTutor = (Long) session.getAttribute("usuarioId");
        if (idTutor == null) return "redirect:/login";

        Tutor tutor = tutorService.buscarPorId(idTutor).orElse(null);
        if (tutor == null) return "redirect:/login";

        // Saldo disponible = pagos liberados por el sistema - retiros anteriores
        double saldoDisponible = pagoService.totalLiberado(idTutor) - retiroService.totalRetirado(idTutor);

        // Los errores se agrupan por campo para mostrarlos en rojo debajo
        Map<String, String> erroresRetiro = new HashMap<>();

        Double montoNum = null;
        if (monto == null || monto.isBlank()) {
            erroresRetiro.put("monto", "El monto no puede estar vacío");
        } else {
            try {
                montoNum = Double.parseDouble(monto.trim());
            } catch (NumberFormatException e) {
                erroresRetiro.put("monto", "El monto debe ser un número válido");
            }
        }

        if (!erroresRetiro.containsKey("monto")) {
            erroresRetiro.putAll(retiroService.validar(montoNum, saldoDisponible, metodoRetiro,
                    celularYape, banco, cuentaBancaria));
        } else {
            // Aunque el monto falle, se validan también los demás campos
            retiroService.validar(1.0, saldoDisponible, metodoRetiro, celularYape, banco, cuentaBancaria)
                    .forEach(erroresRetiro::putIfAbsent);
        }

        if (!erroresRetiro.isEmpty()) {
            ra.addFlashAttribute("erroresRetiro", erroresRetiro);

            // Lo que el tutor había llenado, para no perderlo al reabrir el modal
            Map<String, Object> retiroValores = new HashMap<>();
            retiroValores.put("monto", monto != null ? monto : "");
            retiroValores.put("metodoRetiro", metodoRetiro != null ? metodoRetiro : "");
            retiroValores.put("celularYape", celularYape != null ? celularYape : "");
            retiroValores.put("banco", banco != null ? banco : "");
            retiroValores.put("cuentaBancaria", cuentaBancaria != null ? cuentaBancaria : "");
            ra.addFlashAttribute("retiroValores", retiroValores);
            return "redirect:/tutor/panel";
        }

        retiroService.registrar(tutor, montoNum, saldoDisponible, metodoRetiro,
                celularYape, banco, cuentaBancaria);
        ra.addFlashAttribute("mensaje", "¡Retiro realizado! S/. "
                + String.format("%.2f", montoNum) + " enviados a tu "
                + ("Yape".equalsIgnoreCase(metodoRetiro) ? "Yape" : "cuenta bancaria") + ".");
        return "redirect:/tutor/panel";
    }
}
