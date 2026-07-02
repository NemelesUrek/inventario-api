package com.nemeles.inventario.service;

import com.nemeles.inventario.domain.Producto;
import com.nemeles.inventario.repo.ProductoRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Foto del producto: binario en disco (data/productos), metadatos en la entidad. */
@Service
public class ProductoFotoService {

    private static final Set<String> TIPOS_OK = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private final ProductoRepository productos;
    private final AuditoriaService auditoria;
    private final Path carpeta;

    public ProductoFotoService(ProductoRepository productos, AuditoriaService auditoria,
                               @Value("${app.data.dir:./data}") String dataDir) {
        this.productos = productos;
        this.auditoria = auditoria;
        this.carpeta = Paths.get(dataDir, "productos");
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(carpeta);
    }

    @Transactional
    public Producto guardar(Producto p, MultipartFile file) {
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
        byte[] cabecera;
        try (InputStream in = file.getInputStream()) {
            cabecera = in.readNBytes(16);
        } catch (IOException e) {
            throw new ConflictException("No se pudo leer el archivo.");
        }
        String real = ImagenUtil.detectarImagen(cabecera);
        if (real == null) {
            throw new ConflictException("El archivo no es una imagen válida (JPG, PNG o WEBP).");
        }
        String ext = real.equals("image/png") ? ".png" : real.equals("image/webp") ? ".webp" : ".jpg";
        String nombre = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Files.copy(file.getInputStream(), carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la foto.", e);
        }
        borrarArchivo(p.getFotoNombre()); // reemplaza la anterior si había
        p.setFotoNombre(nombre);
        Producto guardado = productos.save(p);
        auditoria.registrar("FOTO", "PRODUCTO", p.getId(), "Foto actualizada de " + p.getNombre());
        return guardado;
    }

    /** El MIME se deduce de la extensión del nombre almacenado (la extensión la fijó el detector). */
    public String contentType(Producto p) {
        String n = p.getFotoNombre() == null ? "" : p.getFotoNombre();
        return n.endsWith(".png") ? "image/png" : n.endsWith(".webp") ? "image/webp" : "image/jpeg";
    }

    @Transactional(readOnly = true)
    public byte[] leerBytes(Producto p) {
        if (p.getFotoNombre() == null) {
            throw new NotFoundException("El producto no tiene foto.");
        }
        try {
            return Files.readAllBytes(carpeta.resolve(p.getFotoNombre()));
        } catch (IOException e) {
            throw new NotFoundException("No se encontró el archivo de la foto.");
        }
    }

    @Transactional
    public Producto eliminar(Producto p) {
        borrarArchivo(p.getFotoNombre());
        p.setFotoNombre(null);
        Producto guardado = productos.save(p);
        auditoria.registrar("FOTO", "PRODUCTO", p.getId(), "Foto eliminada de " + p.getNombre());
        return guardado;
    }

    /** Limpieza silenciosa del binario (al reemplazar la foto o borrar el producto). */
    public void borrarArchivo(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
        } catch (IOException ignored) {
            // si el archivo ya no está, no pasa nada
        }
    }
}
