package com.tutorapp.tutorapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tutorapp.tutorapp.model.Curso;
import com.tutorapp.tutorapp.model.ServicioTutor;
import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.service.CursoService;
import com.tutorapp.tutorapp.service.ServicioTutorService;
import com.tutorapp.tutorapp.service.TutorService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Validator;

@Controller
public class ServicioTutorController {

    @Autowired
    private ServicioTutorService servicioTutorService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private Validator validator;

    @PostMapping("/tutor/servicios/agregar")
    public String agregar(@RequestParam String nombreCurso,
            @RequestParam Double precio,
            @RequestParam(required = false) String descripcion,
            HttpSession session,
            RedirectAttributes ra) {
        Long idTutor = (Long) session.getAttribute("usuarioId");
        if (idTutor == null) return "redirect:/login";

        Tutor tutor = tutorService.buscarPorId(idTutor).orElse(null);
        if (tutor == null) return "redirect:/login";

        if (nombreCurso == null || nombreCurso.isBlank()) {
            ra.addFlashAttribute("error", "El nombre del curso no puede estar vacío.");
            return "redirect:/tutor/panel";
        }

        Curso curso = cursoService.buscarOCrear(nombreCurso.trim());
        // Todas las tutorías son virtuales (videollamada integrada de la página)
        ServicioTutor servicio = new ServicioTutor(tutor, curso, precio, "Virtual", descripcion);

        // Valida el servicio con las anotaciones de la entity (@Positive, @Pattern...)
        var errores = validator.validate(servicio);
        if (!errores.isEmpty()) {
            ra.addFlashAttribute("error", errores.iterator().next().getMessage());
            return "redirect:/tutor/panel";
        }

        servicioTutorService.guardar(servicio);
        ra.addFlashAttribute("mensaje", "Servicio agregado correctamente.");
        return "redirect:/tutor/panel";
    }

    @PostMapping("/tutor/servicios/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        servicioTutorService.eliminar(id);
        ra.addFlashAttribute("mensaje", "Servicio eliminado.");
        return "redirect:/tutor/panel";
    }
}
