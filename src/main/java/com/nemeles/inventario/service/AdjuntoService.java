package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.AdjuntoMovimiento;
import com.nemeles.inventario.repo.AdjuntoRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Guarda y sirve adjuntos (fotos/firmas) de movimientos: binario en disco, metadatos en BD. */
@Service
public class AdjuntoService {

    private static final Set<String> TIPOS_OK = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 8L * 1024 * 1024;
    private static final int MAX_ADJUNTOS_POR_MOV = 10;

    private final AdjuntoRepository repo;
    private final AuditoriaService auditoria;
    private final Path carpeta;

    public AdjuntoService(AdjuntoRepository repo, AuditoriaService auditoria,
                          @Value("${app.data.dir:./data}") String dataDir) {
        this.repo = repo;
        this.auditoria = auditoria;
        this.carpeta = Paths.get(dataDir, "adjuntos");
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(carpeta);
    }

    @Transactional(readOnly = true)
    public List<AdjuntoMovimiento> listar(Long movId) {
        return repo.findByMovimientoIdOrderByFechaAsc(movId);
    }

    /** Obtiene un adjunto verificando que pertenece al movimiento de la ruta (evita IDOR). */
    @Transactional(readOnly = true)
    public AdjuntoMovimiento obtener(Long movId, Long id) {
        return repo.findByIdAndMovimientoId(id, movId)
                .orElseThrow(() -> new NotFoundException("No existe el adjunto " + id + " en el movimiento " + movId));
    }

    @Transactional
    public AdjuntoMovimiento guardar(Long movId, Long productoId, MultipartFile file, AdjuntoMovimiento.Tipo tipo) {
        if (file == null || file.isEmpty()) {
            throw new ConflictException("El archivo está vacío.");
        }
        String ct = file.getContentType();
        if (ct == null || !TIPOS_OK.contains(ct)) {
            throw new ConflictException("Tipo de imagen no permitido. Usa JPG, PNG o WEBP.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ConflictException("La imagen supera el límite de 8 MB.");
        }
        if (repo.countByMovimientoId(movId) >= MAX_ADJUNTOS_POR_MOV) {
            throw new ConflictException("Se alcanzó el máximo de " + MAX_ADJUNTOS_POR_MOV + " adjuntos para este movimiento.");
        }
        // Validar el tipo REAL por los bytes mágicos del contenido, NO por el Content-Type que declara el cliente.
        byte[] cabecera;
        try (InputStream in = file.getInputStream()) {
            cabecera = in.readNBytes(16);
        } catch (IOException e) {
            throw new ConflictException("No se pudo leer el archivo.");
        }
        String real = detectarImagen(cabecera);
        if (real == null) {
            throw new ConflictException("El archivo no es una imagen válida (JPG, PNG o WEBP).");
        }
        String ext = real.equals("image/png") ? ".png" : real.equals("image/webp") ? ".webp" : ".jpg";
        String nombre = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Files.copy(file.getInputStream(), carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo.", e);
        }
        // Se guarda el tipo REAL detectado (no el declarado), para servirlo de forma consistente.
        AdjuntoMovimiento a = repo.save(new AdjuntoMovimiento(
                movId, productoId, tipo, nombre, file.getOriginalFilename(), real, file.getSize()));
        auditoria.registrar("ADJUNTAR", "MOVIMIENTO", movId,
                (tipo == AdjuntoMovimiento.Tipo.FIRMA ? "Firma" : "Foto") + " adjuntada al movimiento #" + movId);
        return a;
    }

    /** Firma de imagen real por magic bytes; devuelve el MIME detectado o null si no es una imagen permitida. */
    private static String detectarImagen(byte[] b) {
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A) {
            return "image/png";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public byte[] leerBytes(AdjuntoMovimiento a) {
        try {
            return Files.readAllBytes(carpeta.resolve(a.getNombreAlmacenado()));
        } catch (IOException e) {
            throw new NotFoundException("No se encontró el archivo del adjunto.");
        }
    }

    @Transactional
    public void eliminar(Long movId, Long id) {
        AdjuntoMovimiento a = obtener(movId, id);
        try {
            Files.deleteIfExists(carpeta.resolve(a.getNombreAlmacenado()));
        } catch (IOException ignored) {
            // si el archivo ya no está, igual quitamos el registro
        }
        repo.delete(a);
        auditoria.registrar("ELIMINAR", "MOVIMIENTO", a.getMovimientoId(),
                "Eliminó un adjunto del movimiento #" + a.getMovimientoId());
    }
}
