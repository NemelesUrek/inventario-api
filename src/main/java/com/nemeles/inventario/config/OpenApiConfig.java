package com.nemeles.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventarioOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Control de Inventario")
                .version("1.0.0")
                .description("""
                        Demo de backend en **Java 17 + Spring Boot** por **Nemeles**.

                        Gestiona productos, controla el stock con entradas/salidas, deja un \
                        registro de auditoría de cada movimiento y avisa cuándo reponer. \
                        El dinero se maneja en centavos (sin errores de redondeo).

                        Prueba cualquier endpoint con el botón **Try it out**.""")
                .contact(new Contact().name("Nemeles — Backend Engineer").url("https://github.com/NemelesUrek"))
                .license(new License().name("MIT")));
    }
}
