package com.tutorapp.tutorapp.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Filtro que se ejecuta en cada request: lee el JWT de la cookie, lo valida
 * y, si es correcto, autentica al usuario en el SecurityContext con el rol
 * incluido en el token (claim "rol").
 *
 * Con esto la aplicación deja de depender de la HttpSession para saber
 * quién es el usuario autenticado: cada request se autentica de forma
 * independiente a partir del token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtCookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String token = cookieUtil.extraerToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extraerUsername(token);

                if (username != null && jwtUtil.esTokenValido(token, username)) {
                    String rol = jwtUtil.extraerRol(token);

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                    var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Los paneles y las vistas usan los atributos de sesión
                    // (usuarioId, usuarioNombre, usuarioTipo); si la sesión se
                    // perdió pero el token sigue vigente, se restauran desde el JWT.
                    HttpSession session = request.getSession();
                    if (session.getAttribute("usuarioId") == null) {
                        session.setAttribute("usuarioId", jwtUtil.extraerId(token));
                        session.setAttribute("usuarioNombre", jwtUtil.extraerNombre(token));
                        session.setAttribute("usuarioTipo", rol);
                    }
                }
            } catch (Exception e) {
                // Token corrupto, mal firmado o expirado: se ignora y la request
                // sigue como no autenticada. Si la ruta lo requiere, Spring Security
                // se encargará de redirigir a /login.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
