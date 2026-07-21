package com.tutorapp.tutorapp.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tutorapp.tutorapp.model.Sesion;
import com.tutorapp.tutorapp.model.Sesion.EstadoSesion;
import com.tutorapp.tutorapp.service.PagoService;
import com.tutorapp.tutorapp.service.ResenaService;
import com.tutorapp.tutorapp.service.RetiroService;
import com.tutorapp.tutorapp.service.SesionService;
import com.tutorapp.tutorapp.service.ServicioTutorService;
import com.tutorapp.tutorapp.service.TutorService;

import jakarta.servlet.http.HttpSession;

@Controller
public class SesionController {

    @Autowired
    private SesionService sesionService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private ServicioTutorService servicioTutorService;

    @Autowired
    private ResenaService resenaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private RetiroService retiroService;

    @GetMapping("/tutor/panel")
    public String panelTutor(HttpSession session, Model model) {
        Long idTutor = (Long) session.getAttribute("usuarioId");
        if (idTutor == null) return "redirect:/login";

        model.addAttribute("sesionesConfirmadas", sesionService.confirmadas(idTutor));
        model.addAttribute("sesionesPendientes", sesionService.pendientes(idTutor));
        model.addAttribute("tutor", tutorService.buscarPorId(idTutor).orElse(null));
        model.addAttribute("servicios", servicioTutorService.listarPorTutor(idTutor));
        model.addAttribute("historial", sesionService.historialTutor(idTutor));
        model.addAttribute("promedio", resenaService.promedioTutor(idTutor));
        model.addAttribute("totalResenas", resenaService.totalResenasTutor(idTutor));
        // Se calcula una sola vez: el saldo disponible lo reutiliza
        double totalLiberado = pagoService.totalLiberado(idTutor);
        model.addAttribute("pagos", pagoService.pagosDeTutor(idTutor));
        model.addAttribute("totalLiberado", totalLiberado);
        model.addAttribute("totalRetenido", pagoService.totalRetenido(idTutor));
        model.addAttribute("retiros", retiroService.retirosDeTutor(idTutor));
        model.addAttribute("saldoDisponible", totalLiberado - retiroService.totalRetirado(idTutor));
        return "panel-tutor";
    }

    @PostMapping("/tutor/aceptar/{id}")
    public String aceptar(@PathVariable Long id, RedirectAttributes ra) {
        sesionService.aceptar(id);
        ra.addFlashAttribute("mensaje", "Sesión aceptada.");
        return "redirect:/tutor/panel";
    }

    @PostMapping("/tutor/completar/{id}")
    public String completar(@PathVariable Long id, RedirectAttributes ra) {
        var sesion = sesionService.buscarPorId(id).orElse(null);
        if (sesion == null) {
            ra.addFlashAttribute("error", "No se encontró la sesión.");
            return "redirect:/tutor/panel";
        }

        // No se puede completar una sesión que aún no ocurre
        LocalDateTime inicioSesion = LocalDateTime.of(sesion.getFecha(), sesion.getHora());
        if (inicioSesion.isAfter(LocalDateTime.now())) {
            ra.addFlashAttribute("error", "Aún no puedes completar esta sesión: está programada para el "
                    + sesion.getFecha() + " a las " + sesion.getHora() + ".");
            return "redirect:/tutor/panel";
        }

        sesionService.completar(id);
        ra.addFlashAttribute("mensaje", "Sesión marcada como completada.");
        return "redirect:/tutor/panel";
    }

    @PostMapping("/tutor/cancelar/{id}")
    public String cancelarTutor(@PathVariable Long id, RedirectAttributes ra) {
        sesionService.cancelarComoTutor(id);
        ra.addFlashAttribute("mensaje", "Sesión cancelada.");
        return "redirect:/tutor/panel";
    }

    @PostMapping("/tutor/eliminar/{id}")
    public String eliminarTutor(@PathVariable Long id, RedirectAttributes ra) {
        sesionService.eliminar(id);
        ra.addFlashAttribute("mensaje", "Sesión eliminada.");
        return "redirect:/tutor/panel";
    }

    /**
     * Videollamada integrada (Jitsi). Solo pueden entrar el tutor y el alumno
     * de la sesión, y solo si la sesión está confirmada.
     */
    @GetMapping("/sesion/{id}/llamada")
    public String videollamada(@PathVariable Long id, HttpSession session,
            Model model, RedirectAttributes ra) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String tipo = (String) session.getAttribute("usuarioTipo");
        if (usuarioId == null) return "redirect:/login";

        String panel = "TUTOR".equals(tipo) ? "redirect:/tutor/panel" : "redirect:/alumno/panel";

        Sesion sesion = sesionService.buscarPorId(id).orElse(null);
        if (sesion == null || sesion.getSalaVideo() == null
                || sesion.getEstado() != EstadoSesion.Confirmada) {
            ra.addFlashAttribute("error", "La videollamada no está disponible para esta sesión.");
            return panel;
        }

        boolean esElTutor = "TUTOR".equals(tipo)
                && sesion.getServicioTutor().getTutor().getId().equals(usuarioId);
        boolean esElAlumno = "ALUMNO".equals(tipo)
                && sesion.getAlumno().getId().equals(usuarioId);
        if (!esElTutor && !esElAlumno) {
            ra.addFlashAttribute("error", "No tienes acceso a esta videollamada.");
            return panel;
        }

        model.addAttribute("sesion", sesion);
        model.addAttribute("urlPanel", esElTutor ? "/tutor/panel" : "/alumno/panel");
        return "videollamada";
    }
}
