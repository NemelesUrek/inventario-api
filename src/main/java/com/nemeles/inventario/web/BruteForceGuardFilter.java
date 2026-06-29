package com.nemeles.inventario.web;

import com.nemeles.inventario.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Corta los POST a /api/auth/login cuando el usuario está bloqueado por exceso
 * de intentos fallidos (ver {@link LoginAttemptService}). Responde 429 con los
 * segundos para reintentar, sin llegar a comprobar la contraseña.
 */
public class BruteForceGuardFilter extends OncePerRequestFilter {

    private final LoginAttemptService intentos;

    public BruteForceGuardFilter(LoginAttemptService intentos) {
        this.intentos = intentos;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String uri = req.getRequestURI();
        if ("POST".equalsIgnoreCase(req.getMethod()) && uri != null && uri.endsWith("/api/auth/login")) {
            String usuario = req.getParameter("username");
            if (usuario != null && intentos.estaBloqueado(usuario)) {
                res.setStatus(429); // Too Many Requests
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write(
                        "{\"error\":\"demasiados_intentos\",\"reintentar_en_segundos\":"
                        + intentos.segundosRestantes(usuario) + "}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
