package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.AdjuntoMovimiento;
import com.nemeles.inventario.repo.AdjuntoRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
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

    @Transactional(readOnly = true)
    public AdjuntoMovimiento obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe el adjunto con id " + id));
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
        String ext = ct.equals("image/png") ? ".png" : ct.equals("image/webp") ? ".webp" : ".jpg";
        String nombre = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Files.copy(file.getInputStream(), carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo.", e);
        }
        AdjuntoMovimiento a = repo.save(new AdjuntoMovimiento(
                movId, productoId, tipo, nombre, file.getOriginalFilename(), ct, file.getSize()));
        auditoria.registrar("ADJUNTAR", "MOVIMIENTO", movId,
                (tipo == AdjuntoMovimiento.Tipo.FIRMA ? "Firma" : "Foto") + " adjuntada al movimiento #" + movId);
        return a;
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
    public void eliminar(Long id) {
        AdjuntoMovimiento a = obtener(id);
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
