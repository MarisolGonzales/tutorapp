package com.tutorapp.tutorapp.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Curso;
import com.tutorapp.tutorapp.repository.CursoRepository;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    public List<Curso> buscarPorNombre(String nombre) {
        String termino = normalizar(nombre);
        return cursoRepository.findAll().stream()
                .filter(c -> normalizar(c.getNombre()).contains(termino))
                .collect(Collectors.toList());
    }

    public Curso buscarOCrear(String nombre) {
        String termino = normalizar(nombre.trim());
        return cursoRepository.findAll().stream()
                .filter(c -> normalizar(c.getNombre()).equals(termino))
                .findFirst()
                .orElseGet(() -> {
                    Curso c = new Curso();
                    c.setNombre(nombre.trim());
                    return cursoRepository.save(c);
                });
    }

    private String normalizar(String s) {
        return Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
    }

    public Map<Long, String> nombresPorIds(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        Map<Long, String> mapa = new HashMap<>();
        cursoRepository.findAllById(ids).forEach(c -> mapa.put(c.getId(), c.getNombre()));
        return mapa;
    }

    public void guardar(Curso curso) {
        cursoRepository.save(curso);
    }
}
