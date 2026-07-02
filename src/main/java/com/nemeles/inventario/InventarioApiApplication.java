package com.nemeles.inventario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
public class InventarioApiApplication {

	public static void main(String[] args) {
		prepararCarpetaDeDatosEscritorio();
		SpringApplication.run(InventarioApiApplication.class, args);
	}

	/**
	 * Modo escritorio (.exe): si nadie fijó {@code app.data.dir}, guarda la base H2 y los
	 * adjuntos en una carpeta escribible por el usuario ({@code %LOCALAPPDATA%\Stockly\data}),
	 * no junto al ejecutable (que suele estar en Archivos de Programa, de solo lectura).
	 * En el servidor (Render) {@code app.desktop} no está definido, así que no cambia nada.
	 */
	private static void prepararCarpetaDeDatosEscritorio() {
		if (!Boolean.getBoolean("app.desktop") || System.getProperty("app.data.dir") != null) {
			return;
		}
		String base = System.getenv("LOCALAPPDATA");
		if (base == null || base.isBlank()) {
			base = System.getProperty("user.home");
		}
		Path dir = Paths.get(base, "Stockly", "data");
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			// Si no se puede crear, se cae al valor por defecto (./data) del application.properties.
			return;
		}
		System.setProperty("app.data.dir", dir.toString());
	}

}
