package com.nemeles.inventario;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void lista_los_productos_sembrados() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(6)));
    }

    @Test
    void crear_devuelve_201_con_location() throws Exception {
        String body = """
                {"sku":"TST-201","nombre":"Producto de prueba","precioCentavos":1000,"stockInicial":5,"stockMinimo":1}""";
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.sku").value("TST-201"))
                .andExpect(jsonPath("$.stock").value(5))
                .andExpect(jsonPath("$.precio").value("$10.00"));
    }

    @Test
    void obtener_inexistente_devuelve_404() throws Exception {
        mvc.perform(get("/api/productos/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_con_sku_vacio_devuelve_400() throws Exception {
        String body = """
                {"sku":"","nombre":"x","precioCentavos":0,"stockInicial":0,"stockMinimo":0}""";
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.sku").exists());
    }

    @Test
    void salida_mayor_al_stock_devuelve_409() throws Exception {
        String body = """
                {"sku":"TST-409","nombre":"Pocas unidades","precioCentavos":500,"stockInicial":2,"stockMinimo":0}""";
        String resp = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(resp, "$.id");

        mvc.perform(post("/api/productos/" + id + "/salida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidad":5,"motivo":"Venta"}"""))
                .andExpect(status().isConflict());
    }

    @Test
    void feed_global_de_movimientos_devuelve_auditoria() throws Exception {
        // El alta inicial de los productos sembrados genera movimientos de tipo ENTRADA.
        mvc.perform(get("/api/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tipo").exists())
                .andExpect(jsonPath("$[0].stockResultante").exists());
    }

    @Test
    void reset_reinicia_los_datos_de_demo() throws Exception {
        mvc.perform(post("/api/demo/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.productos").value(6));
    }

    @Test
    void stats_devuelve_resumen_agregado() throws Exception {
        mvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos", greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$.entradas", greaterThanOrEqualTo(6)))
                .andExpect(jsonPath("$.valorInventario").value(org.hamcrest.Matchers.startsWith("$")));
    }

    @Test
    void actualizar_y_eliminar_producto() throws Exception {
        String body = """
                {"sku":"TST-PUT","nombre":"Nombre viejo","precioCentavos":1500,"stockInicial":3,"stockMinimo":1}""";
        String resp = mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(resp, "$.id");

        mvc.perform(put("/api/productos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Nombre nuevo","precioCentavos":2000,"stockMinimo":2}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre nuevo"))
                .andExpect(jsonPath("$.precio").value("$20.00"));

        mvc.perform(delete("/api/productos/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/productos/" + id)).andExpect(status().isNotFound());
    }
}
