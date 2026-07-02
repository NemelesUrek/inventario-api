package com.nemeles.inventario.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Modo escritorio: además del puerto HTTP normal (para la PC, sin avisos de certificado),
 * abre un conector HTTPS en {@code app.https.port} (8443) con el keystore self-signed que
 * genera el arranque. Es lo que permite usar la CÁMARA del teléfono como escáner por WiFi:
 * los navegadores solo dan acceso a la cámara en un contexto seguro (HTTPS o localhost).
 * En el servidor (Render) {@code app.desktop}/{@code app.https.port} no existen → inactivo.
 */
@Configuration
@ConditionalOnProperty(name = "app.desktop", havingValue = "true")
public class DesktopHttpsConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> conectorHttpsEscritorio() {
        return factory -> {
            String puerto = System.getProperty("app.https.port");
            String keystore = System.getProperty("app.https.keystore");
            if (puerto == null || keystore == null) {
                return; // no se pudo generar el certificado: la app queda solo en HTTP
            }
            Connector c = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            c.setScheme("https");
            c.setSecure(true);
            c.setPort(Integer.parseInt(puerto));
            SSLHostConfig ssl = new SSLHostConfig();
            SSLHostConfigCertificate cert = new SSLHostConfigCertificate(ssl, SSLHostConfigCertificate.Type.RSA);
            cert.setCertificateKeystoreFile(keystore);
            cert.setCertificateKeystorePassword("stockly");
            cert.setCertificateKeyAlias("stockly");
            ssl.addCertificate(cert);
            c.addSslHostConfig(ssl);
            ((AbstractHttp11Protocol<?>) c.getProtocolHandler()).setSSLEnabled(true);
            factory.addAdditionalTomcatConnectors(c);
        };
    }
}
