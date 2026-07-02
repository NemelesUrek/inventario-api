package com.nemeles.inventario.config;

import java.awt.Desktop;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * En modo escritorio (.exe, con {@code app.desktop=true}) abre el navegador por defecto en la
 * app en cuanto el servidor embebido está escuchando, usando el puerto REAL que tomó Tomcat.
 * En el servidor (Render) esta clase no se activa, así que no intenta abrir ningún navegador.
 */
@Component
@ConditionalOnProperty(name = "app.desktop", havingValue = "true")
public class DesktopLauncher implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int puerto = event.getWebServer().getPort();
        String url = "http://localhost:" + puerto + "/";
        System.out.println();
        System.out.println("  Stockly esta corriendo. Abre esta direccion en tu navegador:");
        System.out.println("      " + url);
        System.out.println("  (cierra esta ventana para apagar Stockly)");
        System.out.println();
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            // Si no se puede abrir el navegador solo, el usuario ve la URL impresa arriba.
        }
    }
}
