package com.nemeles.inventario;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Adjuntos (fotos) de un movimiento: subir multipart, listar, servir la imagen y eliminar. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:adjtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.data.dir=target/test-data-adj"
})
@AutoConfigureMockMvc
@WithMockUser(username = "tester", roles = "ADMINISTRADOR")
class AdjuntoApiTest {

    @Autowired
    private MockMvc mvc;

    /** Crear un producto genera su movimiento "Alta inicial"; devolvemos el id de ese movimiento. */
    private int crearMovimiento(String sku) throws Exception {
        String body = """
                {"sku":"%s","nombre":"Producto con foto","precioCentavos":1000,"stockInicial":7,"stockMinimo":1}"""
                .formatted(sku);
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        String movs = mvc.perform(get("/api/movimientos"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(movs, "$[0].id"); // el más reciente
    }

    @Test
    void subir_listar_servir_y_eliminar_foto() throws Exception {
        int movId = crearMovimiento("ADJ-OK");
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};
        MockMultipartFile foto = new MockMultipartFile("file", "mercancia.png", "image/png", png);

        String resp = mvc.perform(multipart("/api/movimientos/" + movId + "/adjuntos").file(foto).param("tipo", "FOTO"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.tipo").value("FOTO"))
                .andExpect(jsonPath("$.contentType").value("image/png"))
                .andExpect(jsonPath("$.url").exists())
                .andReturn().getResponse().getContentAsString();
        int adjId = JsonPath.read(resp, "$.id");

        mvc.perform(get("/api/movimientos/" + movId + "/adjuntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(adjId));

        mvc.perform(get("/api/movimientos/" + movId + "/adjuntos/" + adjId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));

        mvc.perform(delete("/api/movimientos/" + movId + "/adjuntos/" + adjId))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/movimientos/" + movId + "/adjuntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rechaza_tipo_no_imagen_con_409() throws Exception {
        int movId = crearMovimiento("ADJ-BAD");
        MockMultipartFile txt = new MockMultipartFile("file", "nota.txt", "text/plain", "hola".getBytes());
        mvc.perform(multipart("/api/movimientos/" + movId + "/adjuntos").file(txt).param("tipo", "FOTO"))
                .andExpect(status().isConflict());
    }
}
