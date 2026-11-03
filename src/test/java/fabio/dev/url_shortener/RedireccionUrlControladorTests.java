package fabio.dev.url_shortener;

import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public class RedireccionUrlControladorTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepositorio urlRepositorio;

    @Test
    @DisplayName("POST /acortador/ - debe crear una URL y retornar 201")
    void incrementarClick_debeRetornar201() throws Exception {

        Url url = new Url();
        url.setSlug("1sol4h");
        url.setOriginalUrl("https://ejemplo.com");
        url.setClicks(0L);
        url.setFechaRegistro(LocalDate.now());
        url.setFechaModificacion(LocalDate.now());

        urlRepositorio.save(url);

        mockMvc.perform(get("/1sol4h/"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://ejemplo.com"));

        assertEquals(1L, urlRepositorio.findBySlug("1sol4h").orElseThrow().getClicks());

    }

    @Test
    void redireccionar_slugInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/noexiste/"))
                .andExpect(status().isNotFound());
    }

}