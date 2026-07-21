package com.tutorapp.tutorapp.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Agrega a todas las vistas los datos del usuario autenticado
 * (id, nombre y tipo TUTOR/ALUMNO), tomados de la sesión que el
 * JwtAuthenticationFilter mantiene sincronizada con el token.
 * Así los templates también pueden usar ${usuarioNombre}, ${esTutor}
 * o ${esAlumno} directamente, además de ${session.*}.
 */
@Component
public class UserInterceptor implements HandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        // Solo si hay una vista y no es una redirección
        if (modelAndView == null || modelAndView.getViewName() == null
                || modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("usuarioId") != null) {
            String tipo = (String) session.getAttribute("usuarioTipo");
            modelAndView.addObject("usuarioId", session.getAttribute("usuarioId"));
            modelAndView.addObject("usuarioNombre", session.getAttribute("usuarioNombre"));
            modelAndView.addObject("usuarioTipo", tipo);
            modelAndView.addObject("esTutor", "TUTOR".equals(tipo));
            modelAndView.addObject("esAlumno", "ALUMNO".equals(tipo));
        }
    }
}
