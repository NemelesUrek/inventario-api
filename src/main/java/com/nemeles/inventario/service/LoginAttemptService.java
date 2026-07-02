package com.nemeles.inventario.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Protección anti fuerza bruta para el login. Cuenta los intentos fallidos por
 * usuario y, tras {@link #MAX_INTENTOS} fallos, bloquea ese usuario durante
 * {@link #BLOQUEO}. En memoria (suficiente para una app local/single-node);
 * para un clúster se cambiaría por un store compartido (Redis).
 */
@Service
public class LoginAttemptService {

    private static final int MAX_INTENTOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private static final class Registro {
        int fallos;
        Instant bloqueadoHasta;
    }

    private final Map<String, Registro> cache = new ConcurrentHashMap<>();

    private static String norm(String usuario) {
        return usuario == null ? "" : usuario.trim().toLowerCase();
    }

    /** ¿Este usuario está bloqueado ahora mismo? Limpia el registro si ya expiró. */
    public boolean estaBloqueado(String usuario) {
        Registro r = cache.get(norm(usuario));
        if (r == null || r.bloqueadoHasta == null) {
            return false;
        }
        if (Instant.now().isAfter(r.bloqueadoHasta)) {
            cache.remove(norm(usuario)); // expiró: ventana nueva
            return false;
        }
        return r.fallos >= MAX_INTENTOS;
    }

    /** Registra un intento fallido; al alcanzar el máximo, activa el bloqueo temporal. */
    public void registrarFallo(String usuario) {
        Registro r = cache.computeIfAbsent(norm(usuario), k -> new Registro());
        r.fallos++;
        if (r.fallos >= MAX_INTENTOS) {
            r.bloqueadoHasta = Instant.now().plus(BLOQUEO);
        }
    }

    /** Login correcto: borra el historial de fallos de ese usuario. */
    public void reset(String usuario) {
        cache.remove(norm(usuario));
    }

    /** Segundos que faltan para poder reintentar (0 si no está bloqueado). */
    public long segundosRestantes(String usuario) {
        Registro r = cache.get(norm(usuario));
        if (r == null || r.bloqueadoHasta == null) {
            return 0;
        }
        long s = Duration.between(Instant.now(), r.bloqueadoHasta).getSeconds();
        return Math.max(0, s);
    }
}
