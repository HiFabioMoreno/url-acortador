package fabio.dev.url_shortener;

import com.jayway.jsonpath.JsonPath;
import fabio.dev.url_shortener.dtos.ActualizarRespuesta;
import fabio.dev.url_shortener.dtos.UrlSolicitud;
import fabio.dev.url_shortener.modelos.Url;
import fabio.dev.url_shortener.repositorios.UrlRepositorio;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UrlControladorIntegracionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepositorio urlRepositorio;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /acortador/ - debe crear una URL y retornar 201")
    void registrarUrl_debeRetornar201() throws Exception {

        UrlSolicitud solicitud = new UrlSolicitud("https://www.google.com");

        mockMvc.perform(post("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.slug").exists())
                .andExpect(jsonPath("$.vecesAccedido").value(0));
    }

    @Test
    @DisplayName("GET /acortador/ - debe retornar lista de URLs")
    void listarUrls_debeRetornarLista() throws Exception {

        Url url = new Url();
        url.setOriginalUrl("https://ejemplo.com");
        url.setSlug("abXzy");
        url.setFechaRegistro(Url.GenerarTimestamp());
        url.setFechaModificacion(Url.GenerarTimestamp());
        url.setVecesAccedido(0);
        urlRepositorio.save(url);

        mockMvc.perform(get("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].originalUrl").value("https://ejemplo.com"));
    }

    @Test
    @DisplayName("GET /{slug}/ - debe de regresar un url encontrado por su slug")
    void  debe_de_regresar_un_url_encontrado_por_su_slug() throws Exception {

        Url url = new Url();
        url.setOriginalUrl("https://ejemplo.com/");
        url.setSlug("abXzy");
        url.setFechaRegistro(Url.GenerarTimestamp());
        url.setFechaModificacion(Url.GenerarTimestamp());
        url.setVecesAccedido(0);
        urlRepositorio.save(url);

        mockMvc.perform(get("/" + url.getSlug() + "/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(url)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://ejemplo.com/"));

    }

    @Test
    @DisplayName("POST /acortador/ - body inválido debe retornar 400")
    void registrarUrl_conUrlInvalida_debeRetornar400() throws Exception {

        String bodyInvalido = "{\"url\": \"\"}";

        mockMvc.perform(post("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("PATCH /acortador/{id}/ - debe actualizar y retornar 200")
    void actualizarUrl_debeRetornar200() throws Exception {

        Url url = new Url();
        url.setOriginalUrl("https://ejemplo.com");
        url.setSlug("abXzy");
        url.setFechaRegistro(Url.GenerarTimestamp());
        url.setFechaModificacion(Url.GenerarTimestamp());
        url.setVecesAccedido(0);
        Url guardada = urlRepositorio.save(url);

        ActualizarRespuesta dto = new ActualizarRespuesta("https://nueva.com", null, false, false);

        mockMvc.perform(patch("/acortador/" + guardada.getId() + "/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value("https://nueva.com"));
    }


    @Test
    @DisplayName("PATCH /acortador/{id}/ - id inexistente debe retornar 500 o 404")
    void actualizarUrl_conIdInexistente_debeRetornarError() throws Exception {

        ActualizarRespuesta dto = new ActualizarRespuesta("https://nueva.com", null, false, false);

        mockMvc.perform(patch("/acortador/9999/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is5xxServerError());
    }


    @Test
    @DisplayName("DELETE /acortador/{id}/ - debe retornar 204")
    void eliminarUrl_debeRetornar204() throws Exception {

        Url url = new Url();
        url.setOriginalUrl("https://ejemplo.com");
        url.setSlug("abXzy");
        url.setFechaRegistro(Url.GenerarTimestamp());
        url.setFechaModificacion(Url.GenerarTimestamp());
        url.setVecesAccedido(0);
        Url guardada = urlRepositorio.save(url);

        mockMvc.perform(delete("/acortador/" + guardada.getId() + "/"))
                .andExpect(status().isNoContent());

        assertFalse(urlRepositorio.existsById(guardada.getId()));
    }


    @Test
    @DisplayName("DELETE /acortador/{id}/ - id inexistente debe retornar error")
    void eliminarUrl_conIdInexistente_debeRetornarError() throws Exception {
        mockMvc.perform(delete("/acortador/9999/"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Flujo Completo Crud Exitoso")
    void FlujoCompletoCrud_debeFuncionarCorrectamente() throws Exception {

        UrlSolicitud solicitud = new UrlSolicitud("https://www.google.com");
        ActualizarRespuesta actualizarUrl = new ActualizarRespuesta("https://www.instagram.com/", null, false, true);

        MvcResult result = mockMvc.perform(post("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://www.google.com"))
                .andExpect(jsonPath("$.slug").exists())
                .andExpect(jsonPath("$.vecesAccedido").value(0))
                .andReturn();

        String slugAEncontrar = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.slug"
        );

        mockMvc.perform(get("/acortador/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].originalUrl").value("https://www.google.com"));

        mockMvc.perform(get("/"+ slugAEncontrar +"/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));

        mockMvc.perform(patch("/acortador/1/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizarUrl)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUrl").exists())
                .andExpect(jsonPath("$.originalUrl").value("https://www.instagram.com/"))
                .andExpect(jsonPath("$.slug").exists())
                .andExpect(jsonPath("$.fechaModificacion").exists())
                .andExpect(jsonPath("$.vecesAccedido").value(1));

        mockMvc.perform(delete("/acortador/1/"))
                .andExpect(status().isNoContent());

        assertFalse(urlRepositorio.existsById(1));
    }

}