package com.tutorapp.tutorapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tutorapp.tutorapp.security.JwtAuthenticationFilter;
import com.tutorapp.tutorapp.security.JwtAuthenticationSuccessHandler;
import com.tutorapp.tutorapp.security.JwtLogoutSuccessHandler;
import com.tutorapp.tutorapp.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationSuccessHandler jwtAuthenticationSuccessHandler;

    @Autowired
    private JwtLogoutSuccessHandler jwtLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // La autenticación viaja en la cookie JWT, no en la sesión.
            // (La sesión HTTP se sigue usando solo para datos de la vista.)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                // Recursos estáticos (todos pueden acceder)
                .requestMatchers("/css/**", "/js/**", "/img/**", "/images/**", "/webjars/**").permitAll()
                // Páginas públicas (sin autenticación)
                .requestMatchers("/", "/login", "/registro-alumno", "/registro-tutor", "/error").permitAll()
                .requestMatchers("/tutores", "/busqueda-tutores", "/tutor/perfil/**").permitAll()

                // Rutas del tutor y del alumno según su rol
                .requestMatchers("/tutor/**").hasRole("TUTOR")
                .requestMatchers("/alumno/**").hasRole("ALUMNO")

                // Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService)
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("contrasena")
                .successHandler(jwtAuthenticationSuccessHandler)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(jwtLogoutSuccessHandler)
                .permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // static: permite crear este bean sin instanciar SecurityConfig,
    // evitando el ciclo SecurityConfig -> SuccessHandler -> TutorService -> PasswordEncoder
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
