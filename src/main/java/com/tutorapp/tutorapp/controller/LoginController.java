package com.tutorapp.tutorapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Solo muestra la vista de login. El POST /login lo procesa Spring Security
 * (formLogin + JwtAuthenticationSuccessHandler, que genera la cookie JWT) y
 * el /logout lo procesa JwtLogoutSuccessHandler, que la elimina.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
