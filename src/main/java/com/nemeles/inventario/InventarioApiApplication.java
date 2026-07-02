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
		prepararHttpsEscritorio();
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

	/**
	 * Modo escritorio: prepara el HTTPS local para la cámara del teléfono. Los navegadores
	 * solo permiten la cámara en HTTPS (o localhost), así que el escáner por celular necesita
	 * un certificado. Si no existe el keystore, lo genera UNA vez con el keytool del runtime
	 * embebido (self-signed, uso local). Si queda listo, publica {@code app.https.port} y
	 * {@link com.nemeles.inventario.config.DesktopHttpsConfig} abre el conector 8443.
	 */
	private static void prepararHttpsEscritorio() {
		if (!Boolean.getBoolean("app.desktop")) {
			return;
		}
		Path data = Paths.get(System.getProperty("app.data.dir", "./data"));
		Path ks = data.resolve("stockly-https.p12");
		if (!Files.exists(ks)) {
			boolean win = System.getProperty("os.name", "").toLowerCase().contains("win");
			Path keytool = Paths.get(System.getProperty("java.home"), "bin", win ? "keytool.exe" : "keytool");
			if (!Files.exists(keytool)) {
				return; // sin keytool no hay cert: la app sigue normal por HTTP
			}
			try {
				Process p = new ProcessBuilder(keytool.toString(), "-genkeypair",
						"-alias", "stockly", "-keyalg", "RSA", "-keysize", "2048", "-validity", "3650",
						"-storetype", "PKCS12", "-keystore", ks.toString(),
						"-storepass", "stockly", "-keypass", "stockly",
						"-dname", "CN=Stockly, OU=NemelesRP, O=Nemeles, C=MX",
						"-ext", "SAN=dns:localhost,ip:127.0.0.1")
						.redirectErrorStream(true).start();
				if (!p.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)) {
					p.destroyForcibly();
					return;
				}
			} catch (Exception e) {
				return;
			}
		}
		if (Files.exists(ks)) {
			System.setProperty("app.https.port", "8443");
			System.setProperty("app.https.keystore", ks.toString());
		}
	}

}
