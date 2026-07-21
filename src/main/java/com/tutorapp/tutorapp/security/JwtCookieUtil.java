package com.tutorapp.tutorapp.security;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Encapsula la lectura/escritura del JWT en una cookie HttpOnly.
 *
 * Se usa una cookie (en vez de localStorage + header Authorization) porque
 * el entorno gráfico es Thymeleaf con navegación tradicional (GET/POST y
 * redirects de servidor), así el navegador envía el token automáticamente
 * en cada request sin necesidad de JavaScript adicional.
 */
@Component
public class JwtCookieUtil {

    public static final String COOKIE_NAME = "jwt_token";

    /**
     * Crea la cookie que contiene el token.
     * secure=false para desarrollo local (http). En producción con HTTPS debe ser true.
     */
    public Cookie crearCookie(String token, int maxAgeSegundos) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSegundos);
        return cookie;
    }

    /**
     * Cookie "vacía" con maxAge 0 para forzar al navegador a eliminarla (logout).
     */
    public Cookie crearCookieExpirada() {
        Cookie cookie = new Cookie(COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public String extraerToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
