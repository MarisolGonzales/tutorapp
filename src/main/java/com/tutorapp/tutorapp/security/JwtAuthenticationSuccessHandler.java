package com.tutorapp.tutorapp.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.tutorapp.tutorapp.service.AlumnoService;
import com.tutorapp.tutorapp.service.TutorService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Se ejecuta justo después de que Spring Security valida email/contraseña
 * en el formulario de login. Genera un JWT con el rol del usuario (TUTOR o
 * ALUMNO) y lo entrega en una cookie HttpOnly, deja los datos básicos en la
 * sesión (los paneles y las vistas los usan) y redirige al panel según rol.
 */
@Component
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtCookieUtil cookieUtil;

    @Autowired
    private TutorService tutorService;

    @Autowired
    private AlumnoService alumnoService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName();

        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .orElse("ALUMNO");

        Long id;
        String nombre;
        String destino;

        if ("TUTOR".equals(rol)) {
            var tutor = tutorService.buscarPorEmail(email).orElseThrow();
            id = tutor.getId();
            nombre = tutor.getNombre();
            destino = "/tutor/panel";
        } else {
            var alumno = alumnoService.buscarPorEmail(email).orElseThrow();
            id = alumno.getId();
            nombre = alumno.getNombre();
            destino = "/alumno/panel";
        }

        String token = jwtUtil.generarToken(email, rol, id, nombre);
        int maxAgeSegundos = (int) (jwtUtil.getExpirationMs() / 1000);
        response.addCookie(cookieUtil.crearCookie(token, maxAgeSegundos));

        HttpSession session = request.getSession();
        session.setAttribute("usuarioId", id);
        session.setAttribute("usuarioNombre", nombre);
        session.setAttribute("usuarioTipo", rol);

        response.sendRedirect(destino);
    }
}
