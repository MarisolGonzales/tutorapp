package com.tutorapp.tutorapp.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.tutorapp.tutorapp.model.Alumno;
import com.tutorapp.tutorapp.model.Curso;
import com.tutorapp.tutorapp.model.Tutor;
import com.tutorapp.tutorapp.service.CursoService;
import com.tutorapp.tutorapp.service.ResenaService;
import com.tutorapp.tutorapp.service.ServicioTutorService;
import com.tutorapp.tutorapp.service.TutorService;

@Controller
public class PaginaController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private ServicioTutorService servicioTutorService;

    @Autowired
    private CursoService cursoService;

    @Autowired
    private ResenaService resenaService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/registro-alumno")
    public String registroAlumno(Model model) {
        if (!model.containsAttribute("alumno")) {
            model.addAttribute("alumno", new Alumno());
        }
        return "registro-alumno";
    }

    @GetMapping("/registro-tutor")
    public String registroTutor(Model model) {
        if (!model.containsAttribute("tutor")) {
            model.addAttribute("tutor", new Tutor());
        }
        return "registro-tutor";
    }

    @GetMapping("/tutores")
    public String tutores(Model model) {
        List<Tutor> tutores = tutorService.listarTodos();
        model.addAttribute("tutores", tutores);
        model.addAttribute("promediosPorTutor", promediosMap(tutores));
        model.addAttribute("cantResenPorTutor", cantResenasMap(tutores));
        return "tutores";
    }

    @GetMapping("/busqueda-tutores")
    public String busquedaTutores(@RequestParam(required = false) String curso,
            Model model) {
        List<Tutor> lista = tutorService.listarTodos();

        if (curso != null && !curso.isBlank()) {
            List<Long> idCursos = cursoService.buscarPorNombre(curso)
                    .stream().map(Curso::getId).collect(Collectors.toList());
            List<Long> idTutores = servicioTutorService.listarPorCursos(idCursos)
                    .stream().map(st -> st.getTutor().getId()).distinct().collect(Collectors.toList());
            lista = lista.stream()
                    .filter(t -> idTutores.contains(t.getId()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Map<String, Object>>> serviciosPorTutor = new LinkedHashMap<>();
        for (Tutor t : lista) {
            List<Map<String, Object>> serviciosList = servicioTutorService.listarPorTutor(t.getId())
                    .stream().map(st -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", st.getId());
                        m.put("idCurso", st.getCurso().getId());
                        m.put("precio", st.getPrecio());
                        return m;
                    }).collect(Collectors.toList());
            serviciosPorTutor.put(String.valueOf(t.getId()), serviciosList);
        }

        Map<String, String> nombresCurso = new LinkedHashMap<>();
        cursoService.listarTodos().forEach(c -> nombresCurso.put(String.valueOf(c.getId()), c.getNombre()));

        model.addAttribute("tutores", lista);
        model.addAttribute("cursoBusqueda", curso);
        model.addAttribute("serviciosPorTutor", serviciosPorTutor);
        model.addAttribute("nombresCurso", nombresCurso);
        model.addAttribute("promediosPorTutor", promediosMap(lista));
        model.addAttribute("cantResenPorTutor", cantResenasMap(lista));
        return "busqueda-tutores";
    }

    @GetMapping("/tutor/perfil/{id}")
    public String perfilTutor(@PathVariable Long id, Model model) {
        Tutor tutor = tutorService.buscarPorId(id).orElse(null);
        if (tutor == null) return "redirect:/tutores";
        model.addAttribute("tutor", tutor);
        model.addAttribute("servicios", servicioTutorService.listarPorTutor(id));
        model.addAttribute("promedio", resenaService.promedioTutor(id));
        model.addAttribute("totalResenas", resenaService.totalResenasTutor(id));
        return "perfil-tutor";
    }

    private Map<Long, Double> promediosMap(List<Tutor> tutores) {
        Map<Long, Double> map = new HashMap<>();
        tutores.forEach(t -> map.put(t.getId(), resenaService.promedioTutor(t.getId())));
        return map;
    }

    private Map<Long, Integer> cantResenasMap(List<Tutor> tutores) {
        Map<Long, Integer> map = new HashMap<>();
        tutores.forEach(t -> map.put(t.getId(), resenaService.totalResenasTutor(t.getId())));
        return map;
    }
}
