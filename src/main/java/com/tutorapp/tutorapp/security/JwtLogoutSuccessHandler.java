package com.tutorapp.tutorapp.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Al cerrar sesión ya no hay HttpSession que invalidar (estamos en modo
 * STATELESS); lo único que hay que hacer es borrar la cookie que contiene
 * el JWT y redirigir al login, igual que el comportamiento anterior.
 */
@Component
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private JwtCookieUtil cookieUtil;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException, ServletException {
        response.addCookie(cookieUtil.crearCookieExpirada());
        response.sendRedirect("/login?logout");
    }
}
