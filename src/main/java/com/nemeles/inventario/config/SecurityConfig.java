package com.nemeles.inventario.config;

import com.nemeles.inventario.service.LoginAttemptService;
import com.nemeles.inventario.web.BruteForceGuardFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Seguridad de la app.
 *  - Estático + health + login: ABIERTOS (si no, ni la página ni el health cargan).
 *  - /api/usuarios/**: solo ADMINISTRADOR.   /api/**: requiere sesión iniciada.
 *  - Swagger/OpenAPI: por defecto solo ADMINISTRADOR; público SOLO si
 *    app.docs.public=true (para la demo de Render, vía variable DOCS_PUBLIC).
 *  - Anti fuerza bruta: tras varios fallos se bloquea el usuario un rato
 *    (LoginAttemptService + BruteForceGuardFilter -> 429).
 *  - Cabeceras de seguridad (HSTS, Referrer-Policy) y, en el perfil https,
 *    cookie de sesión Secure + SameSite=Strict.
 *  CSRF deshabilitado a propósito: app de mismo origen; la cookie SameSite=Strict
 *  es la mitigación de CSRF para el login por cookie.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            LoginAttemptService loginAttempts,
            @Value("${app.docs.public:false}") boolean docsPublic) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new BruteForceGuardFilter(loginAttempts), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/index.html", "/favicon.ico", "/og.png",
                            "/css/**", "/js/**", "/fonts/**", "/vendor/**",
                            "/actuator/health", "/api/auth/login").permitAll();
                    if (docsPublic) {
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    } else {
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
                                .hasRole("ADMINISTRADOR");
                    }
                    auth.requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                            .requestMatchers("/api/**").authenticated()
                            .anyRequest().permitAll();
                })
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((req, res, a) -> {
                            loginAttempts.reset(req.getParameter("username"));
                            res.setStatus(HttpStatus.OK.value());
                        })
                        .failureHandler((req, res, e) -> {
                            loginAttempts.registrarFallo(req.getParameter("username"));
                            res.setStatus(HttpStatus.UNAUTHORIZED.value());
                        }))
                .logout(out -> out
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpStatus.OK.value())))
                .headers(h -> h
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
