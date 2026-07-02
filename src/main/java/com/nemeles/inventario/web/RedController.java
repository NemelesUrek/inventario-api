package com.nemeles.inventario.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Datos de red local para "Abrir en el teléfono": el celular en la misma WiFi
 * abre la app por la IP LAN y usa su cámara para escanear códigos.
 */
@Tag(name = "Red", description = "Información de red local (conectar el teléfono)")
@RestController
@RequestMapping("/api/red")
public class RedController {

    @GetMapping("/info")
    @Operation(summary = "IPv4 de red local (LAN) para abrir la app desde el teléfono")
    public Map<String, Object> info() {
        List<String> ips = new ArrayList<>();
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                String nombre = (ni.getDisplayName() + " " + ni.getName()).toLowerCase();
                // saltar adaptadores virtuales típicos (VM / hyper-v) que no son la WiFi real
                if (nombre.contains("virtual") || nombre.contains("vmware") || nombre.contains("vethernet")
                        || nombre.contains("loopback") || nombre.contains("docker")) {
                    continue;
                }
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress() && !ips.contains(addr.getHostAddress())) {
                        ips.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException ignored) {
            // sin red: devolvemos lista vacía y el front muestra el aviso
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ips", ips);
        // Modo escritorio con certificado listo: el front arma el enlace https://IP:8443
        // (la cámara del teléfono solo funciona en HTTPS).
        Integer httpsPort = Integer.getInteger("app.https.port");
        if (httpsPort != null) {
            out.put("httpsPort", httpsPort);
        }
        return out;
    }
}
